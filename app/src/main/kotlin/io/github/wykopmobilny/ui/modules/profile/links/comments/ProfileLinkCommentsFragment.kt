package io.github.wykopmobilny.ui.modules.profile.links.comments

import android.os.Bundle
import android.view.View
import io.github.wykopmobilny.base.BaseLinkCommentFragment
import io.github.wykopmobilny.ui.modules.profile.ProfileActivity
import javax.inject.Inject

class ProfileLinkCommentsFragment : BaseLinkCommentFragment(), ProfileLinkCommentsView {

    @Inject
    lateinit var presenter: ProfileLinksFragmentPresenter

    override var loadDataListener: (Boolean) -> Unit = { presenter.loadData(it) }
    private val username by lazy { (activity as ProfileActivity).username }

    companion object {
        fun newInstance() = ProfileLinkCommentsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.subscribe(this)
        presenter.username = username
        linkCommentsAdapter.linkCommentActionListener = presenter
        linkCommentsAdapter.loadNewDataListener = { loadDataListener(false) }
        presenter.loadData(true)
    }

    override fun onDestroyView() {
        presenter.unsubscribe()
        super.onDestroyView()
    }
}
