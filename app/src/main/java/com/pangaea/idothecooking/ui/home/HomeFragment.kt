package com.pangaea.idothecooking.ui.home

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.FragmentHomeBinding
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingList
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.ui.shared.NameOnlyDialog
import com.pangaea.idothecooking.ui.recipe.RecipeViewActivity
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.ui.shared.BackDataDlg
import com.pangaea.idothecooking.ui.shared.CreateRecipeDialog
import com.pangaea.idothecooking.ui.shared.ImageTool
import com.pangaea.idothecooking.ui.shared.adapters.CreateRecipeAdapter
import com.pangaea.idothecooking.ui.shoppinglist.ShoppingListActivity
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModelFactory
import com.pangaea.idothecooking.utils.extensions.startActivityWithBundle
import kotlin.random.Random

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var shoppingListViewModel: ShoppingListViewModel
    private var lastModifiedTime: Long = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = drawHomeScreen()

        // User Data Backup Alert ////////////////////////////
        Handler().postDelayed({
              val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
              val lastBackupTime = sharedPreferences.getLong("last_backup_time", 0)
              val timeDelta = 1/*days*/ * 24/*hours*/ * 60/*minutes*/ * 60/*seconds*/ * 1000/*milliseconds*/
              if (lastModifiedTime > lastBackupTime && (lastModifiedTime + timeDelta) < System.currentTimeMillis()) {
                  // Recipe modified since last backup...
                  // Been more than 24 hours since the last update...
                  // 20% chance of showing backup suggestion
                  val randomNumber = Random.nextInt(1, 6)
                  if (randomNumber == 1) {
                      BackDataDlg().show(childFragmentManager, null)
                  }
              }
        }, 3000)
        ///////////////////////////////////////////////////////////////

        return view
    }

    private fun drawHomeScreen() : View {
        val root: View = binding.root

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val itemsCount = sharedPreferences.getInt("recent_items_count2", 3)//!!.toInt()

        recipeViewModel = RecipeViewModelFactory(requireActivity().application, null).create(RecipeViewModel::class.java)

        // Load favorites
        recipeViewModel.getAllFavoriteRecipesWithDetails().observe(viewLifecycleOwner) { recipesDetails ->
            drawRecipes(root, R.id.recipeFavoritesHolder, recipesDetails, 0)
        }

        // Load recently changed
        recipeViewModel.getAllRecipesWithDetails().observe(viewLifecycleOwner) { recipesDetails ->
            drawRecipes(root, R.id.recipeHolder, recipesDetails, itemsCount)
            if (recipesDetails.isNotEmpty()) {
                // Set last modified recipe
                lastModifiedTime = recipesDetails[0].recipe.modifiedAt.time
            }
        }

        shoppingListViewModel = ShoppingListViewModelFactory((activity?.application as IDoTheCookingApp),
                                                             null).create(ShoppingListViewModel::class.java)
        shoppingListViewModel.getAllShoppingListsWithDetails().observe(viewLifecycleOwner) { shoppingLists ->
            val linearLayout = root.findViewById<LinearLayout>(R.id.listsHolder)
            linearLayout.removeAllViews()
            for ((index, shoppingListDetails: ShoppingListDetails) in shoppingLists.withIndex()) {
                val shoppingListLayout: View =
                    requireActivity().layoutInflater.inflate(R.layout.home_recent_shopping_list,
                                                             null,false)!!
                val content = shoppingListLayout.findViewById<TextView>(R.id.content)
                content.text = shoppingListDetails.shoppingList.name

                val description = shoppingListLayout.findViewById<TextView>(R.id.description)
                description.text = shoppingListDetails.shoppingListItems.map { it.name }.joinToString(", ")

                shoppingListLayout.rootView.setOnClickListener{
                    startActivityWithBundle(ShoppingListActivity::class.java, "id",
                                            shoppingListDetails.shoppingList.id)
                }

                var isComplete = true
                shoppingListDetails.shoppingListItems.forEach { item ->
                    isComplete = isComplete && item.checked
                }

                val image = shoppingListLayout.findViewById<ImageView>(R.id.recipeImage)
                if (isComplete) {
                    image.setImageResource(android.R.drawable.checkbox_on_background)
                } else {
                    image.setImageResource(android.R.drawable.checkbox_off_background)
                }

                linearLayout.addView(shoppingListLayout)
                if (index >= (itemsCount-1)) break;
            }
        }

        binding.createNewRecipe.setOnClickListener(){
            CreateRecipeDialog(CreateRecipeAdapter(this.requireActivity(), this.requireContext(),
                                                   this.viewLifecycleOwner, this.childFragmentManager,
                                                   recipeViewModel, null)).show(childFragmentManager, null)
        }

        binding.createNewList.setOnClickListener(){
            NameOnlyDialog(R.string.create_shopping_list_title, null) { name ->
                val shoppingList = ShoppingList()
                shoppingList.name = name
                shoppingList.description = ""
                val details = ShoppingListDetails(shoppingList, emptyList())
                shoppingListViewModel.insert(details) { id: Long ->
                    startActivityWithBundle(ShoppingListActivity::class.java, "id", id.toInt())
                }
            }.show(childFragmentManager, null)
        }

        binding.viewRecipes.setOnClickListener(){
            activity?.findNavController(R.id.nav_host_fragment_content_main)?.navigate(R.id.nav_recipes)
        }
        binding.viewShoppingLists.setOnClickListener(){
            activity?.findNavController(R.id.nav_host_fragment_content_main)?.navigate(R.id.nav_shopping_lists)
        }

        return root
    }

    private fun drawRecipes(root: View, id: Int, recipesDetails: List<RecipeDetails>, itemsCount: Int) {
        val linearLayout = root.findViewById<LinearLayout>(id)
        linearLayout.removeAllViews()
        for ((index, recipeDetails: RecipeDetails) in recipesDetails.withIndex()) {
            val recipe = recipeDetails.recipe
            val recipeLayout: View =
                requireActivity().layoutInflater.inflate(R.layout.home_recent_recipe,
                                                         null,false)!!
            val image = recipeLayout.findViewById<ImageView>(R.id.recipeImage)
            if (recipe.imageUri != null && !recipe.imageUri!!.isEmpty()) {
                ImageTool(requireActivity()).display(image, recipe.imageUri!!)
            } else {
                image.visibility = View.GONE
            }

            val content = recipeLayout.findViewById<TextView>(R.id.content)
            content.text = recipe.name
            val description = recipeLayout.findViewById<TextView>(R.id.description)
            if (recipe.description.isEmpty()) {
                description.text = recipeDetails.ingredients.map { it.name }.joinToString(", ")
            } else {
                description.text = recipe.description
            }
            recipeLayout.rootView.setOnClickListener{
                startActivityWithBundle(RecipeViewActivity::class.java, "id", recipe.id)
            }
            linearLayout.addView(recipeLayout)
            if (itemsCount != 0 && index >= (itemsCount-1)) break;
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
        drawHomeScreen()
    }
}