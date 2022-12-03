package com.kiluss.vemergency.ui.admin.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.kiluss.vemergency.constant.CURRENT_TRANSACTION_COLLECTION
import com.kiluss.vemergency.constant.HISTORY_TRANSACTION_COLLECTION
import com.kiluss.vemergency.constant.PENDING_TRANSACTION_COLLECTION
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.databinding.FragmentManageTransactionBinding
import com.kiluss.vemergency.utils.OnLoadMoreListener
import com.kiluss.vemergency.utils.RecyclerViewLoadMoreScroll

class ManageTransactionFragment : Fragment(), ManageTransactionAdapter.OnClickListener {
    private var _binding: FragmentManageTransactionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminMainViewModel by activityViewModels()
    private var historyTransactionAdapter: ManageTransactionAdapter? = null
    private var pendingTransactionAdapter: ManageTransactionAdapter? = null
    private var currentTransactionAdapter: ManageTransactionAdapter? = null
    private lateinit var historyScrollListener: RecyclerViewLoadMoreScroll
    private lateinit var pendingScrollListener: RecyclerViewLoadMoreScroll
    private lateinit var currentScrollListener: RecyclerViewLoadMoreScroll
    private lateinit var historyLayoutManager: LinearLayoutManager
    private lateinit var pendingLayoutManager: LinearLayoutManager
    private lateinit var currentLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setUpRecyclerViewGridView(2)
        setRVScrollListener()
        viewModel.getHistoryTransaction()
        viewModel.getPendingTransaction()
        viewModel.getCurrentTransaction()
    }

    private fun observeViewModel() {
        with(viewModel) {
            historyTransaction.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    historyTransactionAdapter?.updateData(it)
                    binding.tvNoHistoryTransaction.visibility = View.GONE
                } else {
                    binding.tvNoHistoryTransaction.visibility = View.VISIBLE
                }
                historyScrollListener.setLoaded()
                setProgressBarStatus(false)
            }
            pendingTransaction.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    pendingTransactionAdapter?.updateData(it)
                    binding.tvNoPendingTransaction.visibility = View.GONE
                } else {
                    binding.tvNoPendingTransaction.visibility = View.VISIBLE
                }
                pendingScrollListener.setLoaded()
                setProgressBarStatus(false)
            }
            currentTransaction.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    currentTransactionAdapter?.updateData(it)
                    binding.tvNoCurrentTransaction.visibility = View.GONE
                } else {
                    binding.tvNoCurrentTransaction.visibility = View.VISIBLE
                }
                currentScrollListener.setLoaded()
                setProgressBarStatus(false)
            }
        }
    }

    private fun setUpRecyclerViewGridView(column: Int) {
        historyLayoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        pendingLayoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        currentLayoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        historyTransactionAdapter =
            ManageTransactionAdapter(mutableListOf(), requireActivity(), this, HISTORY_TRANSACTION_COLLECTION)
        with(binding.rvHistoryTransaction) {
            adapter = historyTransactionAdapter
            layoutManager = this@ManageTransactionFragment.historyLayoutManager
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
        pendingTransactionAdapter =
            ManageTransactionAdapter(mutableListOf(), requireActivity(), this, PENDING_TRANSACTION_COLLECTION)
        with(binding.rvPendingTransaction) {
            adapter = pendingTransactionAdapter
            layoutManager = this@ManageTransactionFragment.pendingLayoutManager
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
        currentTransactionAdapter =
            ManageTransactionAdapter(mutableListOf(), requireActivity(), this, CURRENT_TRANSACTION_COLLECTION)
        with(binding.rvCurrentTransaction) {
            adapter = currentTransactionAdapter
            layoutManager = this@ManageTransactionFragment.currentLayoutManager
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun setRVScrollListener() {
        historyScrollListener = RecyclerViewLoadMoreScroll(historyLayoutManager)
        historyScrollListener.setOnLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore() {
                setProgressBarStatus(true)
                viewModel.getMoreHistoryTransaction()
            }
        })
        binding.rvHistoryTransaction.addOnScrollListener(historyScrollListener)
        pendingScrollListener = RecyclerViewLoadMoreScroll(pendingLayoutManager)
        pendingScrollListener.setOnLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore() {
                setProgressBarStatus(true)
                viewModel.getMorePendingTransaction()
            }
        })
        binding.rvPendingTransaction.addOnScrollListener(pendingScrollListener)
        currentScrollListener = RecyclerViewLoadMoreScroll(currentLayoutManager)
        currentScrollListener.setOnLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore() {
                setProgressBarStatus(true)
                viewModel.getMoreCurrentTransaction()
            }
        })
        binding.rvCurrentTransaction.addOnScrollListener(currentScrollListener)
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

    override fun onResume() {
        super.onResume()
    }

    override fun onSelect(transaction: Transaction, position: Int) {
    }
}
