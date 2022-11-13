package com.kiluss.vemergency.ui.shop.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.databinding.FragmentPendingTransactionBinding

class PendingTransactionFragment : Fragment(), PendingTransactionAdapter.OnClickListener {
    private var _binding: FragmentPendingTransactionBinding? = null
    private val binding get() = _binding!!
    private var pendingTransactionAdapter: PendingTransactionAdapter? = null

    // view model ktx
    private val viewModel: ShopMainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPendingTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setUpRecyclerViewListView()
        viewModel.getPendingTransaction()
    }

    private fun observeViewModel() {
        with(viewModel) {
            pendingTransaction.observe(viewLifecycleOwner) {
                pendingTransactionAdapter?.updateData(it)
                if (it.isNotEmpty()) {
                    binding.tvNoRequest.visibility = View.GONE
                } else {
                    binding.tvNoRequest.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setUpRecyclerViewListView() {
        pendingTransactionAdapter = PendingTransactionAdapter(mutableListOf(), requireActivity(), this)
        with(binding.rvTransaction) {
            adapter = pendingTransactionAdapter
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
        viewModel.deletePendingTransaction(transaction, position)
    }
}
