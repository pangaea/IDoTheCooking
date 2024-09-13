package com.pangaea.idothecooking.ui.recipe

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.FragmentRecipeMainBinding
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModel
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModelFactory
import com.pangaea.idothecooking.ui.shared.ImageAssetsDialog
import com.pangaea.idothecooking.ui.shared.SelectAssetsDialog
import com.pangaea.idothecooking.ui.shared.ImageTool
import com.pangaea.idothecooking.ui.shared.RecipeTemplateAssetsDialog


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class RecipeMainFragment : Fragment() {

    private var _binding: FragmentRecipeMainBinding? = null
    private var callBackListener: RecipeCallBackListener? = null
    private var imageUri = ""

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

        if (activity is RecipeCallBackListener) callBackListener = activity as RecipeCallBackListener?
        val recipe: RecipeDetails? = callBackListener?.getRecipeDetails()
        recipe?.let {
            binding.editName.setText(recipe.recipe.name)
            binding.editDesc.setText(recipe.recipe.description)
            if (recipe.recipe.imageUri == null || recipe.recipe.imageUri!!.isEmpty()) {
                binding.editImage.setImageDrawable(resources.getDrawable(R.mipmap.image_placeholder3))
            } else {
                ImageTool(binding.editImage, requireActivity()).display(recipe.recipe.imageUri!!)
                imageUri = recipe.recipe.imageUri!!
            }

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
        }

        //attachDirtyEvents(view, R.id.editName, R.id.editDesc)
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
                            pickMedia?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                        R.id.item_library -> {
                            ImageAssetsDialog(imageUri) { o ->
                                imageUri = "asset://image_library/${o}"
                                ImageTool(binding.editImage, requireActivity()).display(imageUri)
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
            recipe?.let {
                val linkedCategoryIds = recipe.categories.map { o -> o.category_id }
                val textView = binding.categoriesView
                textView.text = categories.filter{o -> linkedCategoryIds.contains(o.id)}
                    .map{o -> o.name}.joinToString(", ")
                textView.setOnClickListener() {
                    RecipeCategoryDialog(categories, recipe.categories) { selectedCategories ->
                        textView.text = selectedCategories.joinToString(", ") { o -> o.name }
                        callBackListener?.onRecipeCategories(selectedCategories);
                    }.show(childFragmentManager, null)
                }
            }
        }

    }

    fun fireCallback() {
        val recipe = Recipe()
        recipe.name = binding.editName.text.toString()
        recipe.description = binding.editDesc.text.toString()
        // TODO: Remove this when I figure out how to get the URI directly from the TextView
        //recipe.imageUri = binding.editImage.toString()
        recipe.imageUri = imageUri
        recipe.servings = binding.editServings.value
        callBackListener?.onRecipeInfoUpdate(recipe);
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
        fun newInstance(recipe: RecipeDetails) =
            RecipeMainFragment().apply {
                arguments = Bundle().apply {
                    //putString(RECIPE_NAME, recipe.recipe.name)
                    //putString(RECIPE_DESC, recipe.recipe.description)
                }
            }
    }
}