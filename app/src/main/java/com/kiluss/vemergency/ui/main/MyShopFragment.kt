package com.kiluss.vemergency.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kiluss.vemergency.databinding.FragmentMyShopBinding

class MyShopFragment : Fragment() {

    private var _binding: FragmentMyShopBinding? = null
    private val binding get() = _binding!!

    // view model ktx
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
        observeViewModel()
        viewModel.getShopData()
    }

    private fun setUpView() {
        with(binding) {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        with(viewModel) {
            progressBarStatus.observe(viewLifecycleOwner) {
                if (it) {
                    binding.pbLoading.visibility = View.VISIBLE
                } else {
                    binding.pbLoading.visibility = View.GONE
                }
            }
            shop.observe(viewLifecycleOwner) { shop ->
                shop?.let {
                    it.name?.let {
                        binding.tvShopName.text = it
                    }
                    it.phone?.let {
                        binding.tvPhoneNumber.text = it
                    }
                    it.address?.let {
                        binding.tvAddress.text = it
                    }
                    it.openTime?.let {
                        binding.tvOpenTime.text = it
                    }
                    it.website?.let {
                        binding.tvWebsite.text = it
                    }
                }
            }
            shopImage.observe(viewLifecycleOwner) {
                Glide.with(requireActivity())
                    .load(it)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.ivCover)
            }
        }
    }

    private fun showProgressbar() {
        binding.pbLoading.visibility = View.VISIBLE
    }

    private fun hideProgressbar() {
        binding.pbLoading.visibility = View.GONE
    }
}
