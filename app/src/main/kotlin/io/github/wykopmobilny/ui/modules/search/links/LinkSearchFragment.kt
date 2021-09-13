package io.github.wykopmobilny.ui.modules.search.links

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import io.github.wykopmobilny.base.BaseLinksFragment
import io.github.wykopmobilny.ui.modules.search.SearchFragment
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class LinkSearchFragment : BaseLinksFragment(), LinkSearchView {

    companion object {
        fun newInstance() = LinkSearchFragment()
    }

    @Inject
    lateinit var userManager: UserManagerApi

    @Inject
    lateinit var presenter: LinkSearchPresenter

    var query = ""
    lateinit var querySubscribe: Disposable
    override var loadDataListener: (Boolean) -> Unit = {
        presenter.searchLinks(query, it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.subscribe(this)
        querySubscribe = (parentFragment as SearchFragment).querySubject.subscribe {
            binding.swipeRefresh.isRefreshing = true
            query = it
            presenter.searchLinks(query, true)
        }
        linksAdapter.linksActionListener = presenter
        linksAdapter.loadNewDataListener = { loadDataListener(false) }
        binding.loadingView.isVisible = false
    }

    override fun onDestroyView() {
        presenter.unsubscribe()
        querySubscribe.dispose()
        super.onDestroyView()
    }
}
