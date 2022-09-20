package com.kiluss.bookrate.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.vemergency.ui.login.LoginFragment
import com.example.vemergency.ui.login.SignupFragment

class LoginPagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
    enum class TabLayoutEnum {
        LOGIN, SIGNUP
    }

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        TabLayoutEnum.LOGIN.ordinal -> LoginFragment()
        TabLayoutEnum.SIGNUP.ordinal -> SignupFragment()
        else -> LoginFragment()
    }
}
