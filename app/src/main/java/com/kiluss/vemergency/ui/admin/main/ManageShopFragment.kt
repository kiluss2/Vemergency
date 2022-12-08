package com.kiluss.vemergency.ui.admin.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.kiluss.vemergency.constant.EXTRA_SHOP_DETAIL
import com.kiluss.vemergency.constant.EXTRA_SHOP_PENDING
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.databinding.FragmentManageShopBinding
import com.kiluss.vemergency.ui.admin.approve.ApproveShopActivity
import com.kiluss.vemergency.ui.admin.manage.AdminManageShopActivity
import com.kiluss.vemergency.utils.OnLoadMoreListener
import com.kiluss.vemergency.utils.RecyclerViewLoadMoreScroll

class ManageShopFragment : Fragment(), ShopAdapter.OnClickListener, ShopGridAdapter.OnClickListener {
    private var _binding: FragmentManageShopBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminMainViewModel by activityViewModels()
    private var shopPendingAdapter: ShopAdapter? = null
    private var allShopAdapter: ShopGridAdapter? = null
    lateinit var scrollListener: RecyclerViewLoadMoreScroll
    private lateinit var layoutManager: GridLayoutManager

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
        setUpRecyclerViewGridView(2)
        setRVScrollListener()
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
            allShop.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    allShopAdapter?.updateData(it)
                    binding.tvNoShopFound.visibility = View.GONE
                } else {
                    binding.tvNoShopFound.visibility = View.VISIBLE
                }
                scrollListener.setLoaded()
                setProgressBarStatus(false)
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

    private fun setUpRecyclerViewGridView(column: Int) {
        layoutManager = GridLayoutManager(requireActivity(), column)
        allShopAdapter = ShopGridAdapter(mutableListOf(), requireActivity(), this)
        with(binding.rvShopList) {
            adapter = allShopAdapter
            layoutManager = this@ManageShopFragment.layoutManager
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun setRVScrollListener() {
        scrollListener = RecyclerViewLoadMoreScroll(layoutManager)
        scrollListener.setOnLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore() {
                setProgressBarStatus(true)
                viewModel.getMoreActiveShop()
            }
        })

        binding.rvShopList.addOnScrollListener(scrollListener)
    }

    private fun setProgressBarStatus(status: Boolean) {
        binding.pbLoading.visibility = if (status) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // for approve shop
    override fun onOpen(shop: Shop) {
        startActivity(Intent(requireActivity(), ApproveShopActivity::class.java).apply {
            putExtra(EXTRA_SHOP_PENDING, shop)
        })
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onOpenShopDetail(shop: Shop) {
        startActivity(Intent(requireActivity(), AdminManageShopActivity::class.java).apply {
            putExtra(EXTRA_SHOP_DETAIL, shop)
        })
    }
}
