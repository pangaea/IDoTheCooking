package com.pangaea.idothecooking.ui.recipe

import android.Manifest
import android.R.id.message
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.ActivityRecipeViewBinding
import com.pangaea.idothecooking.state.RecipeRepository
import com.pangaea.idothecooking.state.db.AppDatabase
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.ui.shared.PicklistDlg
import com.pangaea.idothecooking.utils.extensions.vulgarFraction


class RecipeViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeViewBinding
    private lateinit var viewModel: RecipeViewModel
    private var recipeId: Int = -1
    private lateinit var recipeDetails: RecipeDetails
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecipeViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setBackgroundResource(R.mipmap.tablecloth3)

        val bundle = intent?.extras
        if (bundle != null) {
            recipeId = bundle.getInt("id", -1)
        }

        setSupportActionBar(binding.toolbar)

        val db: AppDatabase = (application as IDoTheCookingApp).getDatabase()
        val recipeRepo = db.recipeDao()?.let {
            db.recipeDirectionDao()
                ?.let { it1 ->
                    db.recipeIngredientDao()?.let { it2 -> db.recipeCategoryLinkDao()
                        ?.let { it3 -> RecipeRepository(it, it1, it2, it3) } }
                }
        }

        viewModel = recipeRepo?.let {
            RecipeViewModelFactory(it, recipeId.toLong())
                .create(RecipeViewModel::class.java)
        }!!

        viewModel.getDetails()?.observe(this) { recipes ->
            recipeDetails = recipes[0]
            title = resources.getString(R.string.title_activity_recipe_name).replace("{0}", recipeDetails.recipe.name)
            val htmlRecipe = renderRecipe(R.string.html_recipe, R.string.html_recipe_ingredient,
                                          R.string.html_recipe_direction)
            binding.viewport.loadDataWithBaseURL(null, htmlRecipe, "text/html", "utf-8", null);
        }
    }

    fun renderRecipe(template: Int, ingredientTemplate: Int, directionTemplate: Int): String {
        val ingredientBuilder = StringBuilder()
        val htmlRecipeIngredient = getString(ingredientTemplate)
        recipeDetails.ingredients.forEach { ingredient: Ingredient ->
            val frac: Pair<String, Double>? = ingredient.amount?.vulgarFraction
            if (frac != null) {
                val amount = frac.first + " " + ingredient.unit
                ingredientBuilder.append(htmlRecipeIngredient.replace("{{amount}}", amount)
                                             .replace("{{name}}", ingredient.name))

            } else {
                ingredientBuilder.append(htmlRecipeIngredient.replace("{{amount}}", "")
                                             .replace("{{name}}", ingredient.name))
            }
        }

        val directionBuilder = StringBuilder()
        val htmlRecipeDirection = getString(directionTemplate)
        recipeDetails.directions.forEachIndexed { index, direction: Direction ->
            directionBuilder.append(htmlRecipeDirection.replace("{{content}}", direction.content)
                                        .replace("{{step}}", (index+1).toString()))
        }

        var imageElem = ""
        if (!recipeDetails.recipe.imageUri.isNullOrEmpty()) {
            imageElem = "<img length=\"100px\" width=\"100px\" src=\""+ recipeDetails.recipe.imageUri.toString() + "\">";
        }

        var htmlRecipe = getString(template)
        htmlRecipe = htmlRecipe.replace("{{title}}", recipeDetails.recipe.name).replace("{{description}}", recipeDetails.recipe.description)
            .replace("{{ingredients}}", ingredientBuilder.toString())
            .replace("{{directions}}", directionBuilder.toString())
            .replace("{{image}}", imageElem)

        if (recipeDetails.recipe.servings > 0) {
            htmlRecipe = htmlRecipe.replace("{{servings}}", recipeDetails.recipe.servings.toString())
        } else {
            htmlRecipe = htmlRecipe.replace("{{servings}}", "-")
        }
        return htmlRecipe
    }

    object RequestCode {
        const val REQUEST_CONTACT = 0
        const val REQUEST_READ_CONTACTS_PERMISSIONS = 1
        const val REQUEST_SEND_SMS_PERMISSIONS = 2
    }

    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun hasSMSPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactsPermission() {
        if (!hasContactsPermission()) {
            ActivityCompat.requestPermissions(this,
                                              arrayOf<String>(Manifest.permission.READ_CONTACTS),
                                              RequestCode.REQUEST_READ_CONTACTS_PERMISSIONS)
        }
    }

    private fun requestSMSPermission() {
        if (!hasSMSPermission()) {
            ActivityCompat.requestPermissions(this,
                                              arrayOf<String>(Manifest.permission.SEND_SMS),
                                              RequestCode.REQUEST_SEND_SMS_PERMISSIONS)
        }
    }

    private var selectedPhoneNumber: String? = null

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestCode.REQUEST_READ_CONTACTS_PERMISSIONS && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openContacts()
            }
        } else if (requestCode == RequestCode.REQUEST_SEND_SMS_PERMISSIONS && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectedPhoneNumber?.let { sendSMS(it) }
            }
        }
    }

    @SuppressLint("Range", "Recycle")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        if (requestCode == RequestCode.REQUEST_CONTACT && data != null) {
            val contactData = data.data
            if (contactData != null) {
                val cursor = contentResolver.query(contactData, null, null, null, null)
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            val id =
                                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                            val hasPhone =
                                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                            if (hasPhone.equals("1", ignoreCase = true)) {
                                val phones = contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null)
                                phones!!.moveToFirst()
                                val phoneNumbers: MutableList<String> = mutableListOf()
                                while (!phones.isAfterLast) {
                                    phoneNumbers.add(phones.getString(phones.getColumnIndex("data1")))
                                    phones.moveToNext()
                                }
                                if (phoneNumbers.isNotEmpty()) {
                                    PicklistDlg("Select phone number",
                                                phoneNumbers) { selectedNum: String ->
                                        selectedPhoneNumber = selectedNum
                                        if (hasSMSPermission()) {
                                            sendSMS(selectedNum)
                                        } else {
                                            selectedPhoneNumber = selectedNum
                                            requestSMSPermission()
                                        }
                                    }.show(this.supportFragmentManager, null)
                                }
                            }
                        }
                    } finally {
                        cursor.close()
                    }
                }
            }
        }
    }

    private fun sendSMS(phoneNumber: String) {
        val textRecipe = renderRecipe(R.string.text_recipe, R.string.text_recipe_ingredient,
                                      R.string.text_recipe_direction)
        try {
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(textRecipe)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts,
                                                null, null)
            val successMsg = getString(R.string.recipe_sent_success).replace("{{0}}", recipeDetails.recipe.name)
            Toast.makeText(applicationContext, successMsg, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            val failedMsg = getString(R.string.recipe_sent_failed).replace("{{0}}", recipeDetails.recipe.name)
            Toast.makeText(applicationContext, failedMsg, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun openContacts() {
        val pickContact = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(pickContact, RequestCode.REQUEST_CONTACT);
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.recipe_view_menu, menu)
        val itemCancel = menu.findItem(R.id.item_cancel)
        itemCancel.setOnMenuItemClickListener { menuItem ->
            onBackPressed()
            false
        }

        val itemEdit = menu.findItem(R.id.item_edit)
        itemEdit.setOnMenuItemClickListener { menuItem ->
            val intent = Intent(this, RecipeActivity::class.java)
            val b = Bundle()
            b.putInt("id", recipeId)
            intent.putExtras(b)
            startActivity(intent)
            false
        }

        val itemShare = menu.findItem(R.id.item_share)
        itemShare.setOnMenuItemClickListener { menuItem ->
            if (hasContactsPermission()) {
                openContacts()
            } else {
                requestContactsPermission()
            }
            false
        }
        return true
    }
}