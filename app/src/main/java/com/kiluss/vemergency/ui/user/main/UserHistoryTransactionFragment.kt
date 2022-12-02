package com.kiluss.vemergency.ui.user.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.kiluss.vemergency.R
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.databinding.FragmentShopHistoryTransactionBinding
import com.kiluss.vemergency.databinding.FragmentUserHistoryTransactionBinding
import com.kiluss.vemergency.ui.shop.main.HistoryTransactionAdapter
import com.kiluss.vemergency.ui.shop.main.ShopMainViewModel

class UserHistoryTransactionFragment  : Fragment(), HistoryTransactionAdapter.OnClickListener {
    private var _binding: FragmentUserHistoryTransactionBinding? = null
    private val binding get() = _binding!!
    private var historyTransactionAdapter: HistoryTransactionAdapter? = null

    // view model ktx
    private val viewModel: ShopMainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHistoryTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setUpRecyclerViewListView()
        viewModel.getHistoryTransaction()
    }

    private fun observeViewModel() {
        with(viewModel) {
            historyTransaction.observe(viewLifecycleOwner) {
                historyTransactionAdapter?.updateData(it)
                if (it.isNotEmpty()) {
                    binding.tvNoHistory.visibility = View.GONE
                } else {
                    binding.tvNoHistory.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setUpRecyclerViewListView() {
        historyTransactionAdapter = HistoryTransactionAdapter(mutableListOf(), requireActivity(), this)
        with(binding.rvTransaction) {
            adapter = historyTransactionAdapter
            layoutManager = LinearLayoutManager(requireActivity())
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSelect(transaction: Transaction, position: Int) {
    }
}