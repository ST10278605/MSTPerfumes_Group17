package com.example.mstperfumes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mstperfumes.MainActivity
import com.example.mstperfumes.adapters.ProductAdapter
import com.example.mstperfumes.data.ProductRepository
import com.example.mstperfumes.databinding.FragmentProductsBinding

class ProductsFragment : Fragment() {
    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var menAdapter: ProductAdapter
    private lateinit var womenAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProductGrids()
        setupSearch()
    }

    private fun calculateSpanCount(): Int {
        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        return when {
            screenWidthDp >= 900 -> 4
            screenWidthDp >= 500 -> 3
            else -> 2
        }
    }

    private fun setupProductGrids() {
        val mainActivity = activity as? MainActivity
        val menProducts = ProductRepository.getMenProducts()
        val womenProducts = ProductRepository.getWomenProducts()

        menAdapter = ProductAdapter(menProducts) { product ->
            mainActivity?.addToCart(product)
            Toast.makeText(requireContext(), "${product.name} added to cart", Toast.LENGTH_SHORT).show()
        }

        womenAdapter = ProductAdapter(womenProducts) { product ->
            mainActivity?.addToCart(product)
            Toast.makeText(requireContext(), "${product.name} added to cart", Toast.LENGTH_SHORT).show()
        }

        val spanCount = calculateSpanCount()

        binding.rvMenProducts.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.rvMenProducts.adapter = menAdapter
        binding.rvMenProducts.isNestedScrollingEnabled = false

        binding.rvWomenProducts.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.rvWomenProducts.adapter = womenAdapter
        binding.rvWomenProducts.isNestedScrollingEnabled = false
    }

    private fun setupSearch() {
        binding.etSearchPerfumes.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.isNotEmpty()) {
                binding.btnClearSearch.visibility = View.VISIBLE
                filterProducts(query)
            } else {
                binding.btnClearSearch.visibility = View.GONE
                resetProducts()
            }
        }

        binding.btnClearSearch.setOnClickListener {
            binding.etSearchPerfumes.text?.clear()
        }
    }

    private fun filterProducts(query: String) {
        val lowerQuery = query.lowercase()

        val filteredMen = ProductRepository.getMenProducts().filter { p ->
            p.name.lowercase().contains(lowerQuery) ||
            p.scentProfile.lowercase().contains(lowerQuery) ||
            p.size.lowercase().contains(lowerQuery)
        }

        val filteredWomen = ProductRepository.getWomenProducts().filter { p ->
            p.name.lowercase().contains(lowerQuery) ||
            p.scentProfile.lowercase().contains(lowerQuery) ||
            p.size.lowercase().contains(lowerQuery)
        }

        menAdapter.updateProducts(filteredMen)
        womenAdapter.updateProducts(filteredWomen)

        binding.tvMensHeader.visibility = if (filteredMen.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rvMenProducts.visibility = if (filteredMen.isNotEmpty()) View.VISIBLE else View.GONE

        binding.tvWomensHeader.visibility = if (filteredWomen.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rvWomenProducts.visibility = if (filteredWomen.isNotEmpty()) View.VISIBLE else View.GONE

        if (filteredMen.isEmpty() && filteredWomen.isEmpty()) {
            binding.tvNoResults.text = "No perfumes found matching \"$query\""
            binding.tvNoResults.visibility = View.VISIBLE
        } else {
            binding.tvNoResults.visibility = View.GONE
        }
    }

    private fun resetProducts() {
        val menProducts = ProductRepository.getMenProducts()
        val womenProducts = ProductRepository.getWomenProducts()

        menAdapter.updateProducts(menProducts)
        womenAdapter.updateProducts(womenProducts)

        binding.tvMensHeader.visibility = View.VISIBLE
        binding.rvMenProducts.visibility = View.VISIBLE
        binding.tvWomensHeader.visibility = View.VISIBLE
        binding.rvWomenProducts.visibility = View.VISIBLE
        binding.tvNoResults.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        val currentQuery = binding.etSearchPerfumes.text.toString().trim()
        if (currentQuery.isNotEmpty()) {
            filterProducts(currentQuery)
        } else {
            resetProducts()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}