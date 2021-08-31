package io.github.wykopmobilny.domain.login

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.fresh
import io.github.wykopmobilny.domain.login.di.LoginScope
import io.github.wykopmobilny.domain.navigation.AppRestarter
import io.github.wykopmobilny.storage.api.Blacklist
import io.github.wykopmobilny.storage.api.LoggedUserInfo
import io.github.wykopmobilny.storage.api.SessionStorage
import io.github.wykopmobilny.storage.api.UserSession
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.base.SimpleViewStateStorage
import io.github.wykopmobilny.ui.base.launchIn
import io.github.wykopmobilny.ui.login.InfoMessageUi
import io.github.wykopmobilny.ui.login.Login
import io.github.wykopmobilny.ui.login.LoginUi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoginQuery @Inject constructor(
    private val sessionStorage: SessionStorage,
    private val userInfoStore: Store<UserSession, LoggedUserInfo>,
    private val blacklistStore: Store<Unit, Blacklist>,
    private val loginConfig: () -> ConnectConfig,
    private val appRestarter: AppRestarter,
    private val viewStateStorage: SimpleViewStateStorage,
    private val appScopes: AppScopes,
) : Login {

    override fun invoke() =
        viewStateStorage.state.map { viewState ->
            LoginUi(
                urlToLoad = loginConfig().connectUrl,
                isLoading = viewState.isLoading,
                visibleError = viewState.visibleError?.let {
                    InfoMessageUi(
                        title = "Oops...",
                        message = it.localizedMessage ?: it.toString(),
                        confirmAction = ::dismissError,
                        dismissAction = ::dismissError,
                    )
                },
                parseUrlAction = ::onUrlInvoked,
            )
        }

    private fun onUrlInvoked(url: String) = appScopes.launchIn<LoginScope> {
        val userSession = withContext(AppDispatchers.Default) {
            val match = loginPattern.find(url) ?: return@withContext null

            val login = match.groups[1]?.value?.takeIf { it.isNotBlank() }
            val token = match.groups[2]?.value?.takeIf { it.isNotBlank() }

            if (login.isNullOrBlank() || token.isNullOrBlank()) {
                null
            } else {
                UserSession(login, token)
            }
        } ?: return@launchIn
        viewStateStorage.update { it.copy(isLoading = true) }

        runCatching {
            sessionStorage.updateSession(userSession)
            userInfoStore.fresh(userSession)
            blacklistStore.fresh(Unit)
            appRestarter.restart()
        }
            .onFailure { throwable ->
                sessionStorage.updateSession(null)
                userInfoStore.clearAll()
                blacklistStore.clearAll()
                viewStateStorage.update { it.copy(isLoading = false, visibleError = throwable) }
            }
            .onSuccess { viewStateStorage.update { it.copy(isLoading = false, visibleError = null) } }
    }

    private fun dismissError() = appScopes.launchIn<LoginScope> {
        viewStateStorage.update { it.copy(visibleError = null) }
    }

    companion object {
        private val loginPattern = "/ConnectSuccess/appkey/.+/login/(.+)/token/(.+)/".toRegex()
    }
}
