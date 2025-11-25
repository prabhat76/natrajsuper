package com.example.natraj

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.lifecycle.lifecycleScope
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.WooPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import java.util.Locale

class CategoriesFragment : Fragment() {
    
    private lateinit var categoriesRecycler: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var voiceSearchIcon: ImageView
    private var allCategories: List<Category> = emptyList()
    private var categoryAdapter: CategoryAdapter? = null
    
    private val voiceSearchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val searchText = matches[0]
                searchBar.setText(searchText)
                filterCategories(searchText)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_categories, container, false)
        
        initializeViews(view)
        setupSearchBar()
        setupCategories()
        
        // View All button - scroll to top
        view.findViewById<TextView>(R.id.view_all_categories)?.setOnClickListener {
            categoriesRecycler.smoothScrollToPosition(0)
        }

        // View All Offers button - perhaps show offers or scroll
        view.findViewById<TextView>(R.id.view_all_offers)?.setOnClickListener {
            // For now, scroll to categories
            categoriesRecycler.smoothScrollToPosition(0)
        }

        return view
    }

    private fun initializeViews(view: View) {
        categoriesRecycler = view.findViewById(R.id.categories_grid_recycler)
        searchBar = view.findViewById(R.id.category_search_bar)
        voiceSearchIcon = view.findViewById(R.id.voice_search_icon)
    }
    
    private fun setupSearchBar() {
        // Text search
        searchBar.addTextChangedListener { text ->
            filterCategories(text.toString())
        }
        
        // Voice search
        voiceSearchIcon.setOnClickListener {
            startVoiceSearch()
        }
    }
    
    private fun startVoiceSearch() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say category name...")
            }
            voiceSearchLauncher.launch(intent)
        } catch (e: Exception) {
            showToast("Voice search not available")
        }
    }
    
    private fun filterCategories(query: String) {
        if (allCategories.isEmpty()) return
        
        val filteredList = if (query.isBlank()) {
            allCategories
        } else {
            allCategories.filter { category ->
                category.name.contains(query, ignoreCase = true)
            }
        }
        
        categoryAdapter?.updateCategories(filteredList)
        
        if (filteredList.isEmpty() && query.isNotBlank()) {
            showToast("No categories found for \"$query\"")
        }
    }

    private fun setupCategories() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        categoriesRecycler.layoutManager = gridLayoutManager
        
        // Add spacing between items
        val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        categoriesRecycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.left = spacing
                outRect.right = spacing
                outRect.top = spacing
                outRect.bottom = spacing
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val repo = WooRepository(requireContext())
                val categories = withContext(Dispatchers.IO) { repo.getCategories() }
                
                if (categories.isNotEmpty()) {
                    allCategories = categories
                    categoryAdapter = CategoryAdapter(categories) { category ->
                        if (category.id > 0) {
                            // Open dedicated CategoryProductsActivity with proper extras
                            val intent = android.content.Intent(requireContext(), com.example.natraj.ui.activities.CategoryProductsActivity::class.java)
                            intent.putExtra("category_id", category.id)
                            intent.putExtra("category_name", category.name)
                            startActivity(intent)
                        } else {
                            showToast("Invalid category")
                        }
                    }
                    categoriesRecycler.adapter = categoryAdapter
                    android.util.Log.d("CategoriesFragment", "Loaded ${categories.size} categories from WordPress")
                } else {
                    android.util.Log.w("CategoriesFragment", "No categories found in WordPress")
                    showToast("No categories available")
                }
            } catch (e: Exception) {
                android.util.Log.e("CategoriesFragment", "Failed to load categories from WordPress: ${e.message}", e)
                showToast("Unable to load categories. Please check your connection.")
            }
        }
    }

    private fun showToast(message: String) {
        if (isAdded && context != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
