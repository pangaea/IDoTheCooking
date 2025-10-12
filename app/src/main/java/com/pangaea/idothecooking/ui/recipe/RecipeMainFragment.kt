package com.pangaea.idothecooking.ui.recipe

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.FragmentRecipeMainBinding
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModel
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModelFactory
import com.pangaea.idothecooking.ui.recipe.viewmodels.SelectedRecipeModel
import com.pangaea.idothecooking.ui.shared.ImageAssetsDialog
import com.pangaea.idothecooking.ui.shared.ImageTool
import com.pangaea.idothecooking.utils.ThrottledUpdater
import com.pangaea.idothecooking.utils.extensions.observeOnce

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class RecipeMainFragment : Fragment() {

    private var _binding: FragmentRecipeMainBinding? = null
    private var imageUri = ""
    private lateinit var recipeOrig: Recipe
    private val selectedRecipeModel: SelectedRecipeModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Registers a photo picker activity launcher in single-select mode.
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                // TODO: Remove this when I figure out how to get the URI directly from the TextView
                imageUri = uri.toString()
                binding.editImage.setImageURI(uri)
                requireActivity().contentResolver.takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION)
                fireCallback();
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeMainBinding.inflate(inflater, container, false)
        return binding.root

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundResource(R.mipmap.tablecloth3)

        // Retrieve recipe object from model
        selectedRecipeModel.selectedRecipe.observeOnce(requireActivity()) { recipe ->
            // Perform an action with the latest item data.
            Log.d(ContentValues.TAG, "onRecipeUpdateEvent !!!!!!!!!!!!!")
            recipeOrig = recipe.recipe
            binding.editName.setText(recipe.recipe.name)
//            selectedRecipeModel.selectedRecipe.observe(requireActivity()) { recipeUpdate ->
//                // Listen for name change only due to saveAs
//                binding.editName.setText(recipeUpdate.recipe.name)
//            }
            binding.editDesc.setText(recipe.recipe.description)
            if (recipe.recipe.imageUri == null || recipe.recipe.imageUri!!.isEmpty()) {
                binding.editImage.setImageDrawable(resources.getDrawable(R.mipmap.image_placeholder3))
            } else {
                ImageTool(requireActivity()).display(binding.editImage, recipe.recipe.imageUri!!)
                imageUri = recipe.recipe.imageUri!!
            }

            binding.favorite.isChecked = recipe.recipe.favorite

            val amountView = view.findViewById<NumberPicker>(R.id.editServings)
            amountView.minValue = 0
            amountView.maxValue = 100
            amountView.displayedValues = buildList() {
                for ( index in amountView.minValue until amountView.maxValue + 1){
                    if (index == 0) {
                        add("?")
                    } else {
                        add(index.toString())
                    }
                }
            }.toTypedArray()
            amountView.value = recipe.recipe.servings

            binding.editName.doAfterTextChanged() {
                fireCallback()
            }
            binding.editDesc.doAfterTextChanged() {
                fireCallback();
            }

            binding.editImage.setOnClickListener(){
                val popupMenu = activity?.let { it1 -> PopupMenu(it1, binding.editImage) }
                if (popupMenu != null) {
                    popupMenu.menuInflater.inflate(R.menu.edit_image_menu, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.item_gallery -> {
                                pickMedia?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                            }
                            R.id.item_downloads -> {
                                val openDocumentIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = "*/*"
                                }
                                startActivityForResult(openDocumentIntent, RequestCode.OPEN_DOCUMENT_REQUEST_CODE)
                            }
                            R.id.item_camera -> {
                                if (ContextCompat.checkSelfPermission(requireActivity().application,
                                                                      Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                                    requestPermissions(arrayOf(Manifest.permission.CAMERA),
                                                       RequestCode.CAMERA)
                                } else {
                                    openCamera()
                                }
                            }
                            R.id.item_library -> {
                                ImageAssetsDialog(imageUri) { o ->
                                    imageUri = "asset://image_library/${o}"
                                    ImageTool(requireActivity()).display(binding.editImage, imageUri)
                                    fireCallback();
                                }.show(childFragmentManager, null)
                            }
                            R.id.item_clear -> {
                                imageUri = ""
                                binding.editImage.setImageDrawable(getResources().getDrawable(R.mipmap.image_placeholder3))
                                this.fireCallback()
                            }
                        }
                        true
                    }
                    // Showing the popup menu
                    popupMenu.show()
                }
            }
            binding.editServings.setOnValueChangedListener() {picker: NumberPicker,
                                                              oldVal: Int,
                                                              newVal: Int ->
                fireCallback();
            }


            val viewModel = CategoryViewModelFactory(requireActivity().application, null).create(CategoryViewModel::class.java)
            viewModel.getAllCategories().observe(viewLifecycleOwner) { categories ->
                recipe.let {
                    val linkedCategoryIds = recipe.categories.map { o -> o.category_id }
                    val textView = binding.categoriesView
                    textView.text = categories.filter{o -> linkedCategoryIds.contains(o.id)}
                        .map{o -> o.name}.joinToString(", ")
                    textView.setOnClickListener() {
                        RecipeCategoryDialog(categories, recipe.categories) { selectedCategories ->
                            textView.text = selectedCategories.joinToString(", ") { o -> o.name }
                            //callBackListener?.onRecipeCategories(selectedCategories);
                            selectedRecipeModel.updateRecipeCategories(selectedCategories)
                        }.show(childFragmentManager, null)
                    }
                }
            }
        }

        binding.favorite.setOnClickListener() {
            fireCallback();
        }
    }

    object RequestCode {
        const val CAMERA = 0
        const val OPEN_DOCUMENT_REQUEST_CODE = 1
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, RequestCode.CAMERA)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestCode.CAMERA && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }
        }
    }

    @SuppressLint("Range", "Recycle")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != AppCompatActivity.RESULT_OK) return
        if (requestCode == RequestCode.CAMERA && data != null) {
            val photo: Bitmap = data.extras?.get("data") as Bitmap
            //binding.editImage.setImageBitmap(photo)
            val imageTool = ImageTool(requireActivity())
            imageUri = imageTool.saveImage(photo, "recipe_photos")
            imageTool.display(binding.editImage, imageUri)
            fireCallback();
        }
        if (requestCode == RequestCode.OPEN_DOCUMENT_REQUEST_CODE && data != null) {
            val contentUri = data.data
            if (contentUri != null) {
                try {
                    requireActivity().contentResolver.takePersistableUriPermission(contentUri,
                                                                                   Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val imageTool = ImageTool(requireActivity())
                    imageUri = contentUri.toString()
                    imageTool.display(binding.editImage, imageUri)
                    fireCallback();
                } catch (exception: Exception) {
                    Toast.makeText(requireActivity().applicationContext, exception.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val infoUpdater = ThrottledUpdater()

    private fun fireCallback() {
        infoUpdater.delayedUpdate() {
            //val recipe = Recipe()
            recipeOrig.name = binding.editName.text.toString()
            recipeOrig.description = binding.editDesc.text.toString()
            // TODO: Remove this when I figure out how to get the URI directly from the TextView
            //recipe.imageUri = binding.editImage.toString()
            recipeOrig.imageUri = imageUri
            recipeOrig.servings = binding.editServings.value
            recipeOrig.favorite = binding.favorite.isChecked
            //callBackListener?.onRecipeInfoUpdate(recipe);
            requireActivity().runOnUiThread {
                selectedRecipeModel.updateRecipe(recipeOrig)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param editMode Render in edit mode
         * @return A new instance of fragment RecipeDirectionsFragment.
         */
        @JvmStatic
        fun newInstance() =
            RecipeMainFragment().apply {
                arguments = Bundle().apply {
                    //putString(RECIPE_NAME, recipe.recipe.name)
                    //putString(RECIPE_DESC, recipe.recipe.description)
                }
            }
    }
}