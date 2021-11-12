package io.github.wykopmobilny.links.details.items

import androidx.core.view.isVisible
import com.github.wykopmobilny.ui.components.SelectableLinkMovement
import com.github.wykopmobilny.ui.components.bind
import com.github.wykopmobilny.ui.components.setUserNick
import com.github.wykopmobilny.ui.components.utils.bind
import io.github.wykopmobilny.links.details.LinkCommentUi
import io.github.wykopmobilny.links.details.ParentCommentUi
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsParentCommentBinding
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsParentCommentHiddenBinding
import io.github.wykopmobilny.utils.bindings.setOnClick
import io.github.wykopmobilny.utils.bindings.toColorInt

internal fun LinkDetailsParentCommentBinding.bindParentComment(
    parent: ParentCommentUi,
    data: LinkCommentUi.Normal,
    hasReplies: Boolean,
) {
//    root.setOnLongClick(parent.toggleExpansionStateAction)

    txtCollapsed.isVisible = parent.collapsedCount != null
    txtCollapsed.text = parent.collapsedCount
    txtCollapsed.setOnClick(parent.toggleExpansionStateAction)

    imgAvatar.bind(data.author.avatar)
    txtUser.setUserNick(data.author)
    txtTimestamp.text = data.postedAgo
    txtApp.text = data.app?.let { "via $it" }
    txtBody.isVisible = data.body != null
    txtBody.text = data.body
    txtBody.movementMethod = SelectableLinkMovement
    imgEmbed.bind(data.embed)
    imgBadge.setBackgroundColor(data.badge.toColorInt(context = root.context).defaultColor)
    plusButton.bind(data.plusCount)
    minusButton.bind(data.minusCount)
    moreButton.setOnClick(data.moreAction)
    lineComment.isVisible = hasReplies
}

internal fun LinkDetailsParentCommentHiddenBinding.bindHiddenParent(
    parent: ParentCommentUi,
    data: LinkCommentUi.Hidden,
) {
    root.setOnClick(data.onClicked)
//    root.setOnLongClick(parent.toggleExpansionStateAction)

    txtCollapsed.isVisible = parent.collapsedCount != null
    txtCollapsed.text = parent.collapsedCount
//    txtCollapsed.setOnClick(parent.toggleExpansionStateAction)

    txtUser.setUserNick(data.author)
    imgBadge.setBackgroundColor(data.badge.toColorInt(context = root.context).defaultColor)
}
