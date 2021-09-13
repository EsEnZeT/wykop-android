package io.github.wykopmobilny.ui.modules.notificationslist.hashtags

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import io.github.wykopmobilny.R
import io.github.wykopmobilny.models.dataclass.Notification
import io.github.wykopmobilny.models.fragments.DataFragment
import io.github.wykopmobilny.models.fragments.PagedDataModel
import io.github.wykopmobilny.models.fragments.getDataFragmentInstance
import io.github.wykopmobilny.models.fragments.removeDataFragment
import io.github.wykopmobilny.ui.adapters.NotificationsListAdapter
import io.github.wykopmobilny.ui.modules.notificationslist.BaseNotificationsListFragment
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandlerApi
import javax.inject.Inject

class HashTagsNotificationsListFragment : BaseNotificationsListFragment() {

    companion object {
        const val DATA_FRAGMENT_TAG = "NOTIFICATIONS_HASH_TAG_LIST_ACTIVITY"
        fun newInstance() = HashTagsNotificationsListFragment()
    }

    @Inject
    override lateinit var linkHandler: WykopLinkHandlerApi

    @Inject
    override lateinit var notificationAdapter: NotificationsListAdapter

    @Inject
    lateinit var settingsApi: SettingsPreferencesApi

    @Inject
    lateinit var presenter: HashTagsNotificationsListPresenter

    private lateinit var entryFragmentData: DataFragment<PagedDataModel<List<Notification>>>

    var expanded = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        presenter.subscribe(this)
        super.onCreate(savedInstanceState)
        entryFragmentData = supportFragmentManager.getDataFragmentInstance(DATA_FRAGMENT_TAG)
        val pagedModel = entryFragmentData.data
        if (pagedModel != null && pagedModel.model.isNotEmpty()) {
            binding.loadingView.isVisible = false
            presenter.page = pagedModel.page
            notificationAdapter.addData(pagedModel.model, true)
            notificationAdapter.disableLoading()
        } else {
            binding.loadingView.isVisible = true
            onRefresh()
        }
    }

    override fun onDestroyView() {
        presenter.unsubscribe()
        super.onDestroyView()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.collapseAll)?.isVisible = expanded && settingsApi.groupNotifications
        menu.findItem(R.id.expandAll)?.isVisible = !expanded && settingsApi.groupNotifications
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.hashtag_notification_menu, menu)
        menu.findItem(R.id.groupNotifications).isChecked = settingsApi.groupNotifications
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.groupNotifications -> {
                item.isChecked = !item.isChecked
                settingsApi.groupNotifications = item.isChecked
                onRefresh()
                activity?.invalidateOptionsMenu()
                return true
            }

            R.id.collapseAll -> {
                notificationAdapter.collapseAll()
                expanded = false
                Toast.makeText(context, "Schowano wszystkie powiadomienia", Toast.LENGTH_SHORT).show()
                activity?.invalidateOptionsMenu()
                return true
            }

            R.id.expandAll -> {
                notificationAdapter.expandAll()
                expanded = true
                Toast.makeText(context, "Rozwinięto wszystkie powiadomienia", Toast.LENGTH_SHORT).show()
                activity?.invalidateOptionsMenu()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun loadMore() {
        if (settingsApi.groupNotifications) {
            presenter.loadAllNotifications(false)
        } else {
            presenter.loadData(false)
        }
    }

    override fun onRefresh() {
        if (settingsApi.groupNotifications) {
            presenter.loadAllNotifications(true)
        } else {
            presenter.loadData(true)
        }
    }

    override fun markAsRead() = presenter.readNotifications()

    override fun onPause() {
        if (isRemoving) supportFragmentManager.removeDataFragment(entryFragmentData)
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        entryFragmentData.data = PagedDataModel(presenter.page, notificationAdapter.data)
    }

    override fun showTooManyNotifications() =
        Toast.makeText(context, "Zbyt wiele powiadomień, funkcja grupowania wyłączona", Toast.LENGTH_LONG).show()
}
