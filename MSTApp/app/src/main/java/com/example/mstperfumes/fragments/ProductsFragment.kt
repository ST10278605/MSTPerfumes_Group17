package com.example.mstperfumes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mstperfumes.MainActivity
import com.example.mstperfumes.R
import com.example.mstperfumes.adapters.ProductAdapter
import com.example.mstperfumes.databinding.FragmentProductsBinding
import com.example.mstperfumes.models.Product

class ProductsFragment : Fragment() {
    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private val menProducts = listOf(
        Product(1, "Elegant Night", "100ml", 350.0, 180.0, "Woody & Musk Notes", R.drawable.elegant_night, "men"),
        Product(2, "Citrus Zest", "50ml", 280.0, 140.0, "Fresh & Bright Notes", R.drawable.citrus_zest, "men"),
        Product(3, "Ocean Breeze", "100ml", 320.0, 160.0, "Aquatic & Marine Notes", R.drawable.ocean_breez, "men"),
        Product(4, "Bold Leather", "50ml", 290.0, 150.0, "Leather & Spice Notes", R.drawable.bold_leather, "men")
    )

    private val womenProducts = listOf(
        Product(5, "Midnight Rose", "100ml", 360.0, 190.0, "Floral & Rose Notes", R.drawable.midnight_rose, "women"),
        Product(6, "Vanilla Dream", "50ml", 270.0, 135.0, "Sweet Vanilla Notes", R.drawable.vanilla_dream, "women"),
        Product(7, "Lavender Bliss", "100ml", 310.0, 155.0, "Lavender & Herbal Notes", R.drawable.lavender_bliss, "women"),
        Product(8, "Citrus Bloom", "50ml", 260.0, 130.0, "Citrus & Floral Notes", R.drawable.citrus_bloom, "women")
    )

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
    }

    private fun setupProductGrids() {
        val mainActivity = activity as? MainActivity

        binding.rvMenProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvMenProducts.adapter = ProductAdapter(menProducts) { product ->
            mainActivity?.addToCart(product)
            Toast.makeText(requireContext(), "${product.name} added to cart", Toast.LENGTH_SHORT).show()
        }

        binding.rvWomenProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvWomenProducts.adapter = ProductAdapter(womenProducts) { product ->
            mainActivity?.addToCart(product)
            Toast.makeText(requireContext(), "${product.name} added to cart", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
