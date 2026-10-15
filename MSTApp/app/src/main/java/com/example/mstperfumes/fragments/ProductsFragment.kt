package com.example.mstperfumes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mstperfumes.MainActivity
import com.example.mstperfumes.adapters.ProductAdapter
import com.example.mstperfumes.data.ProductRepository
import com.example.mstperfumes.databinding.FragmentProductsBinding

class ProductsFragment : Fragment() {
    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

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
        val menProducts = ProductRepository.getMenProducts()
        val womenProducts = ProductRepository.getWomenProducts()

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

    override fun onResume() {
        super.onResume()
        setupProductGrids()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
