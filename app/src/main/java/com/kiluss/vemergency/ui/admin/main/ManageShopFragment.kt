package com.kiluss.vemergency.ui.admin.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.databinding.FragmentManageShopBinding

class ManageShopFragment : Fragment(), ShopAdapter.OnClickListener {

    private var _binding: FragmentManageShopBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private val viewModel: AdminMainViewModel by activityViewModels()
    private var shopPendingAdapter: ShopAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setUpRecyclerViewListView()
        viewModel.getShopPendingInfo()
    }

    private fun observeViewModel() {
        with(viewModel) {
            shopPending.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    shopPendingAdapter?.updateData(it)
                    binding.rvPendingRequest.visibility = View.VISIBLE
                } else {
                    binding.rvPendingRequest.visibility = View.GONE
                }
            }
        }
    }

    private fun setUpRecyclerViewListView() {
        shopPendingAdapter = ShopAdapter(mutableListOf(), requireActivity(), this)
        with(binding.rvPendingRequest) {
            adapter = shopPendingAdapter
            layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onOpen(shop: Shop) {
    }
}
