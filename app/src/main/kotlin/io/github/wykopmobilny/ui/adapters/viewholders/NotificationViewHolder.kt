package io.github.wykopmobilny.ui.adapters.viewholders

import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.view.isVisible
import io.github.wykopmobilny.databinding.NotificationsListItemBinding
import io.github.wykopmobilny.models.dataclass.Notification
import io.github.wykopmobilny.utils.api.getGroupColor
import io.github.wykopmobilny.utils.textview.removeHtml
import io.github.wykopmobilny.utils.toPrettyDate
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler

class NotificationViewHolder(
    private val binding: NotificationsListItemBinding,
    private val linkHandler: WykopLinkHandler,
    private val updateHeader: (String) -> Unit,
) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

    fun bindNotification(notification: Notification) {
        binding.apply {
            // Setup widgets
            body.setText(notification.body.removeHtml(), TextView.BufferType.SPANNABLE)
            date.text = notification.date?.toPrettyDate()
            unreadLine.isVisible = notification.new
            unreadMark.isVisible = notification.new
            unreadDotMark.isVisible = notification.new

            if (notification.author != null) {
                avatarView.isVisible = true
                avatarView.setAuthor(notification.author)

                if (notification.author.nick.isNotEmpty()) {
                    // Color nickname
                    val nickName = notification.body.substringBefore(" ") // nick
                    val spannable = body.text as Spannable
                    spannable.setSpan(
                        ForegroundColorSpan(getGroupColor(notification.author.group)),
                        0,
                        nickName.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                    )
                }
            } else {
                avatarView.isVisible = false
            }

            notificationItem.setOnClickListener {
                if (notification.new) {
                    updateHeader(notification.tag)
                }
                notification.new = false
                unreadLine.isVisible = false
                unreadMark.isVisible = false
                unreadDotMark.isVisible = false
                linkHandler.handleUrl(notification.url ?: "https://www.wykop.pl/ludzie/feelfree")
            }
        }
    }
}
