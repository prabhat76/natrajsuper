package com.example.natraj

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.lifecycle.lifecycleScope
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.WooPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.recyclerview.widget.RecyclerView

class CategoriesFragment : Fragment() {
    
    private lateinit var categoriesRecycler: RecyclerView
    private lateinit var searchBar: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_categories, container, false)
        
        initializeViews(view)
        setupCategories()
        
        return view
    }

    private fun initializeViews(view: View) {
        categoriesRecycler = view.findViewById(R.id.categories_grid_recycler)
        searchBar = view.findViewById(R.id.category_search_bar)
    }

    private fun setupCategories() {
        categoriesRecycler.layoutManager = GridLayoutManager(requireContext(), 2)

        val prefs = WooPrefs(requireContext())
        val canUseWoo = !prefs.baseUrl.isNullOrBlank() && !prefs.consumerKey.isNullOrBlank() && !prefs.consumerSecret.isNullOrBlank()

        if (canUseWoo) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val repo = WooRepository(requireContext())
                    val categories = withContext(Dispatchers.IO) { repo.getCategories() }
                    categoriesRecycler.adapter = CategoryAdapter(categories) { category ->
                        showToast("Selected: ${category.name} (${category.productCount} products)")
                        val intent = android.content.Intent(requireContext(), AllProductsActivity::class.java)
                        intent.putExtra("extra_category_id", category.id)
                        intent.putExtra("extra_category_name", category.name)
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CategoriesFragment", "Woo categories failed", e)
                    setupStaticCategoriesFallback()
                }
            }
        } else {
            setupStaticCategoriesFallback()
        }
    }

    private fun setupStaticCategoriesFallback() {
        val staticCategories = listOf(
            Category(1, "Tractors", imageUrl = "", hasSpecialOffer = true, productCount = 25),
            Category(2, "Power Tillers", imageUrl = "", hasSpecialOffer = true, productCount = 18)
        )
        categoriesRecycler.adapter = CategoryAdapter(staticCategories) { category ->
            val intent = android.content.Intent(requireContext(), AllProductsActivity::class.java)
            intent.putExtra("extra_category_name", category.name)
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
