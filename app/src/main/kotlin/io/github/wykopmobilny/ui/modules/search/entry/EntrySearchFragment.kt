package io.github.wykopmobilny.ui.modules.search.entry

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import io.github.wykopmobilny.base.BaseEntriesFragment
import io.github.wykopmobilny.ui.modules.search.SearchFragment
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class EntrySearchFragment : BaseEntriesFragment(), EntrySearchView {

    companion object {
        fun newInstance() = EntrySearchFragment()
    }

    @Inject
    lateinit var presenter: EntrySearchPresenter

    @Inject
    lateinit var userManager: UserManagerApi

    var query = ""
    lateinit var querySubscribe: Disposable
    override var loadDataListener: (Boolean) -> Unit = { presenter.searchEntries(query, it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.subscribe(this)
        querySubscribe = (parentFragment as SearchFragment).querySubject.subscribe {
            binding.swipeRefresh.isRefreshing = true
            query = it
            presenter.searchEntries(query, true)
        }
        entriesAdapter.entryActionListener = presenter
        entriesAdapter.loadNewDataListener = { loadDataListener(false) }
        binding.loadingView.isVisible = false
        showSearchEmptyView = true
    }

    override fun onDestroyView() {
        presenter.unsubscribe()
        querySubscribe.dispose()
        super.onDestroyView()
    }
}
