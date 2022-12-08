package com.kiluss.vemergency.ui.admin.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.kiluss.vemergency.constant.EXTRA_USER_DETAIL
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.databinding.FragmentManageUserBinding
import com.kiluss.vemergency.ui.admin.manage.AdminManageUserActivity
import com.kiluss.vemergency.utils.OnLoadMoreListener
import com.kiluss.vemergency.utils.RecyclerViewLoadMoreScroll

class ManageUserFragment : Fragment(), UserGridAdapter.OnClickListener {
    private var _binding: FragmentManageUserBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminMainViewModel by activityViewModels()
    private var allUserAdapter: UserGridAdapter? = null
    lateinit var scrollListener: RecyclerViewLoadMoreScroll
    private lateinit var layoutManager: GridLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setUpRecyclerViewGridView(2)
        setRVScrollListener()
    }

    private fun observeViewModel() {
        with(viewModel) {
            allUser.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    allUserAdapter?.updateData(it)
                    binding.tvNoUserFound.visibility = View.GONE
                } else {
                    binding.tvNoUserFound.visibility = View.VISIBLE
                }
                scrollListener.setLoaded()
                setProgressBarStatus(false)
            }
        }
    }

    private fun setUpRecyclerViewGridView(column: Int) {
        layoutManager = GridLayoutManager(requireActivity(), column)
        allUserAdapter = UserGridAdapter(mutableListOf(), requireActivity(), this)
        with(binding.rvUserList) {
            adapter = allUserAdapter
            layoutManager = this@ManageUserFragment.layoutManager
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
                viewModel.getMoreAllUser()
            }
        })
        binding.rvUserList.addOnScrollListener(scrollListener)
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

    override fun onOpenUserDetail(user: User) {
        startActivity(Intent(requireActivity(), AdminManageUserActivity::class.java).apply {
            putExtra(EXTRA_USER_DETAIL, user)
        })
    }
}
