package com.example.natraj

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
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
        val categories = listOf(
            Category(1, "Tractors", 
                imageUrl = "https://images.unsplash.com/photo-1558618047-3c8c76ca7d13?w=200",
                hasSpecialOffer = true, productCount = 25),
            Category(2, "Power Tillers", 
                imageUrl = "https://images.unsplash.com/photo-1584464491033-06628f3a6b7b?w=200",
                hasSpecialOffer = true, productCount = 18),
            Category(3, "Generators", 
                imageUrl = "https://images.unsplash.com/photo-1581094794329-c8112a89af12?w=200",
                hasSpecialOffer = false, productCount = 32),
            Category(4, "Water Pumps", 
                imageUrl = "https://images.unsplash.com/photo-1558618966-fbd74c0ac1a7?w=200",
                hasSpecialOffer = false, productCount = 45),
            Category(5, "Garden Tools", 
                imageUrl = "https://images.unsplash.com/photo-1530587191325-3db32d826c18?w=200",
                hasSpecialOffer = true, productCount = 67),
            Category(6, "Seeds & Plants", 
                imageUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=200",
                hasSpecialOffer = false, productCount = 89),
            Category(7, "Fertilizers", 
                imageUrl = "https://images.unsplash.com/photo-1574263867128-f2e0bb4b9e23?w=200",
                hasSpecialOffer = true, productCount = 34),
            Category(8, "Irrigation", 
                imageUrl = "https://images.unsplash.com/photo-1462899006636-339e08d1844e?w=200",
                hasSpecialOffer = false, productCount = 56),
            Category(9, "Harvesting", 
                imageUrl = "https://images.unsplash.com/photo-1500595046743-cd271d694d30?w=200",
                hasSpecialOffer = true, productCount = 23),
            Category(10, "Animal Feed", 
                imageUrl = "https://images.unsplash.com/photo-1500948117430-526badc93a06?w=200",
                hasSpecialOffer = false, productCount = 41),
            Category(11, "Pesticides", 
                imageUrl = "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=200",
                hasSpecialOffer = false, productCount = 28),
            Category(12, "Farm Vehicles", 
                imageUrl = "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=200",
                hasSpecialOffer = true, productCount = 15)
        )

        categoriesRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        categoriesRecycler.adapter = CategoryAdapter(categories) { category ->
            showToast("Selected: ${category.name} (${category.productCount} products)")
            // Navigate to products for this category
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
