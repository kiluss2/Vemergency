package com.kiluss.vemergency.ui.login

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kiluss.vemergency.constant.ROLE_ADMIN

class LoginPagerAdapter(fm: FragmentActivity, private val role: String) : FragmentStateAdapter(fm) {
    enum class TabLayoutEnum {
        LOGIN, SIGNUP
    }

    override fun getItemCount(): Int = if (role == ROLE_ADMIN) {
        1
    } else {
        2
    }

    override fun createFragment(position: Int): Fragment = when (position) {
        TabLayoutEnum.LOGIN.ordinal -> LoginFragment()
        TabLayoutEnum.SIGNUP.ordinal -> SignupFragment()
        else -> LoginFragment()
    }
}
