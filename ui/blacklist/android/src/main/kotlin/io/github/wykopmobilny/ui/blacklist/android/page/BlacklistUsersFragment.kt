package io.github.wykopmobilny.ui.blacklist.android.page

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.wykopmobilny.ui.blacklist.BlacklistDependencies
import io.github.wykopmobilny.ui.blacklist.BlacklistedDetailsUi
import io.github.wykopmobilny.ui.blacklist.GetBlacklistDetails
import io.github.wykopmobilny.ui.blacklist.android.R
import io.github.wykopmobilny.ui.blacklist.android.databinding.FragmentPageBinding
import io.github.wykopmobilny.utils.requireDependency
import io.github.wykopmobilny.utils.viewBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class BlacklistUsersFragment : Fragment(R.layout.fragment_page) {

    lateinit var getBlacklistDetails: GetBlacklistDetails

    private val binding by viewBinding(FragmentPageBinding::bind)

    override fun onAttach(context: Context) {
        getBlacklistDetails = context.requireDependency<BlacklistDependencies>().blacklistDetails()
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.list.layoutManager = LinearLayoutManager(view.context)
        binding.list.addItemDecoration(DividerItemDecoration(view.context, LinearLayoutManager.VERTICAL))
        val adapter = BlacklistPageAdapter()
        binding.list.adapter = adapter
        lifecycleScope.launchWhenResumed {
            getBlacklistDetails()
                .mapNotNull { it.content }
                .filterIsInstance<BlacklistedDetailsUi.Content.WithData>()
                .map { it.users }
                .collect { page ->
                    binding.swipeRefresh.isRefreshing = page.isRefreshing
                    binding.swipeRefresh.setOnRefreshListener { page.refreshAction() }
                    adapter.submitList(page.elements)
                }
        }
    }
}
