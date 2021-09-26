package io.github.wykopmobilny.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.github.wykopmobilny.base.adapter.EndlessProgressAdapter
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.models.dataclass.Entry
import io.github.wykopmobilny.models.dataclass.EntryLink
import io.github.wykopmobilny.models.dataclass.Link
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.ui.adapters.viewholders.BlockedViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.EntryViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.LinkViewHolder
import io.github.wykopmobilny.ui.adapters.viewholders.SimpleLinkViewHolder
import io.github.wykopmobilny.ui.fragments.entries.EntryActionListener
import io.github.wykopmobilny.ui.fragments.links.LinkActionListener
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import javax.inject.Inject

class EntryLinksAdapter @Inject constructor(
    private val userManagerApi: UserManagerApi,
    private val settingsPreferencesApi: SettingsPreferencesApi,
    private val navigator: NewNavigator,
    private val linkHandler: WykopLinkHandler,
    private val appStorage: AppStorage,
) : EndlessProgressAdapter<ViewHolder, EntryLink>() {
    // Required field, interacts with presenter. Otherwise will throw exception
    lateinit var entryActionListener: EntryActionListener
    lateinit var linkActionListener: LinkActionListener

    override fun getViewType(position: Int): Int {
        val entryLink = dataset[position]
        return if (entryLink?.dataType == EntryLink.TYPE_ENTRY) {
            EntryViewHolder.getViewTypeForEntry(entryLink.entry!!)
        } else {
            LinkViewHolder.getViewTypeForLink(entryLink!!.link!!, settingsPreferencesApi)
        }
    }

    override fun addData(items: List<EntryLink>, shouldClearAdapter: Boolean) {
        super.addData(
            items.filterNot {
                val isBlocked = it.entry?.isBlocked == true || it.link?.isBlocked == true
                settingsPreferencesApi.hideBlacklistedViews && isBlocked
            },
            shouldClearAdapter,
        )
    }

    override fun constructViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            LinkViewHolder.TYPE_IMAGE, LinkViewHolder.TYPE_NOIMAGE ->
                LinkViewHolder.inflateView(
                    parent = parent,
                    viewType = viewType,
                    userManagerApi = userManagerApi,
                    settingsPreferencesApi = settingsPreferencesApi,
                    navigator = navigator,
                    linkActionListener = linkActionListener,
                    appStorage = appStorage,
                )
            EntryViewHolder.TYPE_BLOCKED, LinkViewHolder.TYPE_BLOCKED -> BlockedViewHolder.inflateView(parent, ::notifyItemChanged)
            else -> EntryViewHolder.inflateView(
                parent,
                viewType,
                userManagerApi,
                settingsPreferencesApi,
                navigator,
                linkHandler,
                entryActionListener,
                null,
            )
        }
    }

    override fun bindHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is EntryViewHolder -> dataset[position]?.entry?.let(holder::bindView)
            is LinkViewHolder -> dataset[position]?.link?.let(holder::bindView)
            is BlockedViewHolder -> {
                val data = dataset[position]
                data?.link?.let(holder::bindView)
                data?.entry?.let(holder::bindView)
            }
            is SimpleLinkViewHolder -> dataset[position]!!.link?.let(holder::bindView)
        }
    }

    fun updateEntry(entry: Entry) {
        val position = dataset.indexOfFirst { it!!.entry?.id == entry.id }
        dataset[position]!!.entry = entry
        notifyItemChanged(position)
    }

    fun updateLink(link: Link) {
        val position = dataset.indexOfFirst { it!!.link?.id == link.id }
        dataset[position]!!.link = link
        notifyItemChanged(position)
    }
}
