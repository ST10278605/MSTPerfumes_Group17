package com.example.mstperfumes.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mstperfumes.fragments.CartFragment
import com.example.mstperfumes.fragments.CheckoutFragment
import com.example.mstperfumes.fragments.ForumFragment
import com.example.mstperfumes.fragments.HomeFragment
import com.example.mstperfumes.fragments.ProductsFragment

class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> ProductsFragment()
            2 -> ForumFragment()
            3 -> CartFragment()
            4 -> CheckoutFragment()
            else -> HomeFragment()
        }
    }
}
