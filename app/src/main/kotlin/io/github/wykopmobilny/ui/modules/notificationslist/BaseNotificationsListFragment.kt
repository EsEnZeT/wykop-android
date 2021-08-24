package io.github.wykopmobilny.ui.modules.notificationslist

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.R
import io.github.wykopmobilny.base.BaseFragment
import io.github.wykopmobilny.databinding.ActivityNotificationsListBinding
import io.github.wykopmobilny.models.dataclass.Notification
import io.github.wykopmobilny.ui.adapters.NotificationsListAdapter
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandlerApi
import io.github.wykopmobilny.utils.prepare
import io.github.wykopmobilny.utils.viewBinding

abstract class BaseNotificationsListFragment :
    BaseFragment(R.layout.activity_notifications_list),
    NotificationsListView,
    SwipeRefreshLayout.OnRefreshListener {

    protected val binding by viewBinding(ActivityNotificationsListBinding::bind)
    abstract var notificationAdapter: NotificationsListAdapter
    abstract var linkHandler: WykopLinkHandlerApi

    abstract fun markAsRead()

    abstract fun loadMore()

    private fun onNotificationClicked(position: Int) {
        val notification = notificationAdapter.data[position]
        notification.new = false
        notificationAdapter.notifyDataSetChanged()
        Napier.d("Notification url=${notification.url}")
        notification.url?.let { linkHandler.handleUrl(it, true) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swiperefresh.setOnRefreshListener(this)

        notificationAdapter.loadNewDataListener = {
            loadMore()
        }

        notificationAdapter.itemClickListener = { onNotificationClicked(it) }
        binding.recyclerView.apply {
            prepare()
            adapter = notificationAdapter
        }
        binding.swiperefresh.isRefreshing = false
    }

    override fun addNotifications(notifications: List<Notification>, shouldClearAdapter: Boolean) {
        binding.loadingView.isVisible = false
        binding.swiperefresh.isRefreshing = false
        notificationAdapter.addData(
            if (!shouldClearAdapter) notifications.filterNot { notificationAdapter.data.contains(it) } else notifications,
            shouldClearAdapter,
        )
    }

    override fun showReadToast() {
        onRefresh()
        Toast.makeText(context, R.string.read_notifications, Toast.LENGTH_SHORT).show()
    }

    override fun disableLoading() = notificationAdapter.disableLoading()
}
