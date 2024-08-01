package com.pangaea.idothecooking.ui.recipe

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.view.Menu
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
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
import com.pangaea.idothecooking.ui.shared.NumberOnlyDialog
import com.pangaea.idothecooking.ui.shared.PicklistDlg
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModelFactory
import com.pangaea.idothecooking.utils.data.IngredientsMigrationTool
import com.pangaea.idothecooking.utils.extensions.observeOnce
import com.pangaea.idothecooking.utils.extensions.vulgarFraction

class RecipeViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeViewBinding
    private lateinit var viewModel: RecipeViewModel
    private var recipeId: Int = -1
    private lateinit var recipeDetails: RecipeDetails
    private var servingSize: Int = 0;

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

        viewModel = RecipeViewModelFactory(application, recipeId.toLong()).create(RecipeViewModel::class.java)

        viewModel.getDetails()?.observe(this) { recipes ->
            recipeDetails = recipes[0]
            servingSize = recipeDetails.recipe.servings
            if (servingSize == 0) {
                // Hide adjust servings button since we have no way to
            }
            title = resources.getString(R.string.title_activity_recipe_name).replace("{0}", recipeDetails.recipe.name)
            drawRecipe()
        }
    }

    fun drawRecipe() {
        val htmlRecipe = renderRecipe(R.string.html_recipe, R.string.html_recipe_ingredient,
                                      R.string.html_recipe_direction)
        binding.viewport.loadDataWithBaseURL(null, htmlRecipe, "text/html", "utf-8", null);
    }

    fun renderRecipe(template: Int, ingredientTemplate: Int, directionTemplate: Int): String {
        val ingredientBuilder = StringBuilder()
        val htmlRecipeIngredient = getString(ingredientTemplate)
        val ingredients = recipeDetails.ingredients.toMutableList()

        var adjRatio: Double = 1.0
        if (recipeDetails.recipe.servings > 0) {
            // Avoid division by zero
            adjRatio = (servingSize.toDouble() / recipeDetails.recipe.servings)
        }
        ingredients.sortWith { obj1, obj2 ->
            Integer.valueOf(obj1.order).compareTo(Integer.valueOf(obj2.order))
        }
        ingredients.forEach { ingredient: Ingredient ->
            do {
                if (ingredient.amount != null && ingredient.amount!! > 0f) {
                    var adjAmount = ingredient.amount
                    if (adjRatio != 1.0) {
                        adjAmount = ingredient.amount!! * adjRatio
                    }
                    //val frac: Pair<String, Double>? = ingredient.amount?.vulgarFraction
                    val frac: Pair<String, Double>? = adjAmount?.vulgarFraction
                    if (frac != null) {
                        val amount = frac.first + " " + ingredient.unit
                        ingredientBuilder.append(htmlRecipeIngredient.replace("{{amount}}", amount)
                                                     .replace("{{name}}", ingredient.name))
                        break;
                    }
                }

                ingredientBuilder.append(htmlRecipeIngredient.replace("{{amount}}", "")
                                             .replace("{{name}}", ingredient.name))
            } while (false)
        }

        val directionBuilder = StringBuilder()
        val htmlRecipeDirection = getString(directionTemplate)
        val directions = recipeDetails.directions.toMutableList()
        directions.sortWith { obj1, obj2 ->
            Integer.valueOf(obj1.order).compareTo(Integer.valueOf(obj2.order))
        }
        directions.forEachIndexed { index, direction: Direction ->
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
            htmlRecipe = htmlRecipe.replace("{{servings}}", servingSize.toString())
        } else {
            htmlRecipe = htmlRecipe.replace("{{servings}}", "?")
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
                                    PicklistDlg(getString(R.string.select_phone_number),
                                                phoneNumbers.map(){o -> Pair(o, o)}) { selectedNum: Pair<String, String> ->
                                        selectedPhoneNumber = selectedNum.second
                                        if (hasSMSPermission()) {
                                            sendSMS(selectedNum.second)
                                        } else {
                                            selectedPhoneNumber = selectedNum.second
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

        // Close
        val itemCancel = menu.findItem(R.id.item_cancel)
        itemCancel.setOnMenuItemClickListener { menuItem ->
            onBackPressed()
            false
        }

        // Edit recipe
        val itemEdit = menu.findItem(R.id.item_edit)
        itemEdit.setOnMenuItemClickListener { menuItem ->
            val intent = Intent(this, RecipeActivity::class.java)
            val b = Bundle()
            b.putInt("id", recipeId)
            intent.putExtras(b)
            startActivity(intent)
            false
        }

        // Share recipe via SMS
        val itemShare = menu.findItem(R.id.item_share)
        itemShare.setOnMenuItemClickListener { menuItem ->
            if (hasContactsPermission()) {
                openContacts()
            } else {
                requestContactsPermission()
            }
            false
        }

        // Print recipe
        val itemPrint = menu.findItem(R.id.item_print)
        itemPrint.setOnMenuItemClickListener { menuItem ->
            createWebPrintJob(binding.viewport)
            false
        }

        // Adjust serving size
        val itemServSize = menu.findItem(R.id.item_serv_size)
        itemServSize.setOnMenuItemClickListener { menuItem ->
            if (servingSize == 0){
                Toast.makeText(baseContext, getString(R.string.adjust_servings_error), Toast.LENGTH_LONG).show()
            } else {
                NumberOnlyDialog(R.string.adjust_servings_title, servingSize) {
                    servingSize = it
                    drawRecipe()
                }.show(this.supportFragmentManager, null)
            }
            false
        }

        // Export recipe ingredients
        val exportToList = menu.findItem(R.id.export_to_list)
        exportToList.setOnMenuItemClickListener { menuItem ->
            val model = ShoppingListViewModelFactory(application, null).create(ShoppingListViewModel::class.java)
            model.getAllShoppingLists().observeOnce(this) { shoppingLists ->
                PicklistDlg(getString(R.string.export_to_shopping_list),
                            shoppingLists.map() { o ->
                                Pair(o.id.toString(), o.name)
                            }) { shoppingList: Pair<String, String> ->
                    IngredientsMigrationTool(application, this, recipeDetails.recipe.id,
                                             shoppingList.first.toInt()).execute() {
                        Toast.makeText(baseContext, getString(R.string.success_export_to_shopping_list), Toast.LENGTH_LONG).show()
                    }
                }.show(this.supportFragmentManager, null)
            }
            false
        }

        return true
    }

    private fun createWebPrintJob(webView: WebView) {
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        val jobName = recipeDetails.recipe.name
        val printAdapter = webView.createPrintDocumentAdapter(jobName)
        printManager.print(
            jobName, printAdapter,
            PrintAttributes.Builder().build()
        )
    }
}