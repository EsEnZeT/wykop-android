package io.github.wykopmobilny.ui.modules.mainnavigation

import io.github.wykopmobilny.api.notifications.NotificationsApi
import io.github.wykopmobilny.base.BasePresenter
import io.github.wykopmobilny.base.Schedulers
import io.github.wykopmobilny.utils.intoComposite
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import io.github.wykopmobilny.utils.usermanager.isUserAuthorized
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainNavigationPresenter @Inject constructor(
    private val schedulers: Schedulers,
    private val notificationsApi: NotificationsApi,
    private val userManagerApi: UserManagerApi,
) : BasePresenter<MainNavigationView>() {

    private var lastCheckMillis = 0L

    fun startListeningForNotifications() {
        compositeObservable.clear()
        compositeObservable.apply {
            if (userManagerApi.isUserAuthorized()) {
                add(
                    Observable.interval(0, 1, TimeUnit.MINUTES)
                        .subscribe {
                            checkNotifications(false)
                        },
                )
            }
        }
    }

    fun checkNotifications(shouldForce: Boolean) {
        if (lastCheckMillis.plus(300000L) < (System.currentTimeMillis()) || lastCheckMillis == 0L || shouldForce) {
            lastCheckMillis = System.currentTimeMillis()
            notificationsApi.getNotificationCount().subscribeOn(schedulers.backgroundThread())
                .observeOn(schedulers.mainThread())
                .subscribe({ view?.showNotificationsCount(it.count) }, { })
                .intoComposite(compositeObservable)
            notificationsApi.getHashTagNotificationCount().subscribeOn(schedulers.backgroundThread())
                .observeOn(schedulers.mainThread())
                .subscribe({ view?.showHashNotificationsCount(it.count) }, { })
                .intoComposite(compositeObservable)
        }
    }
}
