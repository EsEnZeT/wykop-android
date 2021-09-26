package io.github.wykopmobilny.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.wykopmobilny.databinding.PmmessageSentLayoutBinding
import io.github.wykopmobilny.models.dataclass.PMMessage
import io.github.wykopmobilny.ui.adapters.viewholders.PMMessageViewHolder
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.utils.layoutInflater
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler
import javax.inject.Inject

class PMMessageAdapter @Inject constructor(
    private val settingsPreferencesApi: SettingsPreferencesApi,
    private val navigator: NewNavigator,
    private val linkHandler: WykopLinkHandler
) : RecyclerView.Adapter<PMMessageViewHolder>() {

    val messages: ArrayList<PMMessage> = arrayListOf()

    override fun getItemCount() = messages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PMMessageViewHolder = PMMessageViewHolder(
        PmmessageSentLayoutBinding.inflate(parent.layoutInflater, parent, false),
        linkHandler,
        settingsPreferencesApi,
        navigator
    )

    override fun onBindViewHolder(holder: PMMessageViewHolder, position: Int) =
        holder.bindView(messages[position])

    override fun onViewRecycled(holder: PMMessageViewHolder) {
        (holder as? PMMessageViewHolder)?.cleanRecycled()
        super.onViewRecycled(holder)
    }
}
