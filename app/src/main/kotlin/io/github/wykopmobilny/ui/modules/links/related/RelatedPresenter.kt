package io.github.wykopmobilny.ui.modules.links.related

import io.github.wykopmobilny.api.links.LinksApi
import io.github.wykopmobilny.base.BasePresenter
import io.github.wykopmobilny.base.Schedulers
import io.github.wykopmobilny.utils.intoComposite
import javax.inject.Inject

class RelatedPresenter @Inject constructor(
    private val schedulers: Schedulers,
    private val linksApi: LinksApi
) : BasePresenter<RelatedView>() {

    var linkId = -1L

    fun getRelated() {
        linksApi.getRelated(linkId)
            .subscribeOn(schedulers.backgroundThread())
            .observeOn(schedulers.mainThread())
            .subscribe({ view?.showRelated(it) }, { view?.showErrorDialog(it) })
            .intoComposite(compositeObservable)
    }

    fun addRelated(title: String, description: String) {
        linksApi.relatedAdd(title, description, false, linkId)
            .subscribeOn(schedulers.backgroundThread())
            .observeOn(schedulers.mainThread())
            .subscribe({ view?.onRefresh() }, { view?.showErrorDialog(it) })
            .intoComposite(compositeObservable)
    }
}
