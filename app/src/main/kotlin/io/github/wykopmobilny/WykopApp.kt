package io.github.wykopmobilny

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.widget.Toast
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.Lazy
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.github.wykopmobilny.api.ApiSignInterceptor
import io.github.wykopmobilny.di.DaggerAppComponent
import io.github.wykopmobilny.domain.blacklist.di.BlacklistScope
import io.github.wykopmobilny.domain.di.DomainComponent
import io.github.wykopmobilny.domain.login.ConnectConfig
import io.github.wykopmobilny.domain.login.di.LoginScope
import io.github.wykopmobilny.domain.navigation.InteropRequest
import io.github.wykopmobilny.domain.navigation.android.DaggerFrameworkComponent
import io.github.wykopmobilny.domain.settings.di.SettingsScope
import io.github.wykopmobilny.domain.styles.di.StylesScope
import io.github.wykopmobilny.storage.android.DaggerStoragesComponent
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.styles.StylesDependencies
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.blacklist.BlacklistDependencies
import io.github.wykopmobilny.ui.login.LoginDependencies
import io.github.wykopmobilny.ui.modules.blacklist.BlacklistActivity
import io.github.wykopmobilny.ui.modules.search.SuggestionDatabase
import io.github.wykopmobilny.ui.profile.ProfileDependencies
import io.github.wykopmobilny.ui.settings.SettingsDependencies
import io.github.wykopmobilny.utils.ApplicationInjector
import io.github.wykopmobilny.utils.usermanager.SimpleUserManagerApi
import io.github.wykopmobilny.utils.usermanager.UserCredentials
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import javax.inject.Inject
import kotlin.reflect.KClass

open class WykopApp : DaggerApplication(), ApplicationInjector, AppScopes {

    companion object {

        const val WYKOP_API_URL = "https://a2.wykop.pl"
    }

    @Inject
    lateinit var userManagerApi: Lazy<UserManagerApi>

    @Inject
    lateinit var settingsPreferencesApi: Lazy<SettingsPreferencesApi>

    override val applicationScope = CoroutineScope(Job() + Dispatchers.Default)

    private val okHttpClient = OkHttpClient()

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        doInterop()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerAppComponent.factory().create(
            instance = this,
            okHttpClient = okHttpClient,
            wykop = wykopApi,
            patrons = patrons,
            scraper = scraper,
            storages = storages,
            settingsInterop = domainComponent.settingsApiInterop(),
        )

    protected open val domainComponent: DomainComponent by lazy {
        daggerDomain().create(
            appScopes = this,
            connectConfig = ConnectConfig(connectUrl = "https://a2.wykop.pl/login/connect/appkey/${BuildConfig.APP_KEY}"),
            storages = storages,
            scraper = scraper,
            wykop = wykopApi,
            framework = framework,
        )
    }

    public open val storages by lazy {
        DaggerStoragesComponent.factory().create(
            context = this,
            executor = AppDispatchers.IO.asExecutor(),
        )
    }

    protected open val scraper by lazy {
        daggerScraper().create(
            okHttpClient = okHttpClient,
            baseUrl = "https://wykop.pl",
            cookieProvider = { webPage -> CookieManager.getInstance().getCookie(webPage) },
        )
    }

    protected open val patrons by lazy {
        daggerPatrons().create(
            okHttpClient = okHttpClient.newBuilder()
                .cache(Cache(cacheDir.resolve("okhttp/patrons"), maxSize = 5 * 1024 * 1024L))
                .build(),
            baseUrl = "https://raw.githubusercontent.com/",
        )
    }

    protected open val wykopApi by lazy {
        daggerWykop().create(
            okHttpClient = okHttpClient,
            baseUrl = WYKOP_API_URL,
            appKey = BuildConfig.APP_KEY,
            cacheDir = cacheDir.resolve("okhttp/wykop"),
            signingInterceptor = ApiSignInterceptor(
                object : SimpleUserManagerApi {

                    override fun getUserCredentials(): UserCredentials? = userManagerApi.get().getUserCredentials()
                },
            ),
        )
    }

    protected open val framework by lazy {
        DaggerFrameworkComponent.factory().create(
            application = this,
        )
    }

    private val scopes = mutableMapOf<Any, SubScope<Any>>()

    data class SubScope<T>(
        val dependencyContainer: T,
        val coroutineScope: CoroutineScope,
    )

    // TODO @mk : 25/07/2021 I don't know where I'm going here yet. Will figure something out 👀 
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getDependency(clazz: KClass<T>, scopeId: String?): T =
        when (clazz) {
            LoginDependencies::class -> scopes.getOrPut(LoginScope::class) { SubScope(domainComponent.login(), newScope()) }
            StylesDependencies::class -> scopes.getOrPut(StylesScope::class) { SubScope(domainComponent.styles(), newScope()) }
            SettingsDependencies::class -> scopes.getOrPut(SettingsScope::class) { SubScope(domainComponent.settings(), newScope()) }
            BlacklistDependencies::class -> scopes.getOrPut(BlacklistScope::class) { SubScope(domainComponent.blacklist(), newScope()) }
            ProfileDependencies::class -> {
                checkNotNull(scopeId)
                scopes.getOrPut(scopeId) { SubScope(domainComponent.profile().create(scopeId), newScope()) }
            }
            else -> error("Unknown dependency type $clazz")
        }.dependencyContainer.also { Log.i("WykopApp", "Create component clazz=${clazz.java.simpleName}, scopeId=$scopeId") } as T

    private fun newScope() = CoroutineScope(Job(applicationScope.coroutineContext[Job]) + Dispatchers.Default)

    override fun <T : Any> destroyDependency(clazz: KClass<T>, scopeId: String?) {
        Log.i("WykopApp", "Destroy component clazz=${clazz.java.simpleName}, scopeId=$scopeId")
        when (clazz) {
            LoginDependencies::class -> scopes.remove(LoginScope::class)
            StylesDependencies::class -> scopes.remove(StylesScope::class)
            SettingsDependencies::class -> scopes.remove(SettingsScope::class)
            BlacklistDependencies::class -> scopes.remove(BlacklistScope::class)
            ProfileDependencies::class -> scopes.remove(checkNotNull(scopeId))
            else -> error("Unknown dependency type $clazz")
        }?.coroutineScope?.cancel()
    }

    override fun <T : Any> launchScoped(clazz: KClass<T>, id: String?, block: suspend CoroutineScope.() -> Unit) =
        scopes.getValue(id ?: clazz).coroutineScope.launch(block = block)

    private fun doInterop() {
        applicationScope.launch {
            var currentActivity: Activity? = null
            registerActivityLifecycleCallbacks(
                object : ActivityLifecycleCallbacks {
                    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

                    override fun onActivityStarted(activity: Activity) = Unit

                    override fun onActivityResumed(activity: Activity) {
                        currentActivity = activity
                    }

                    override fun onActivityPaused(activity: Activity) {
                        currentActivity = null
                    }

                    override fun onActivityStopped(activity: Activity) = Unit

                    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

                    override fun onActivityDestroyed(activity: Activity) = Unit
                },
            )
            domainComponent.navigation().request.collect {
                val context = currentActivity ?: return@collect
                when (it) {
                    InteropRequest.BlackListScreen -> context.startActivity(BlacklistActivity.createIntent(context))
                    InteropRequest.ClearSuggestionDatabase -> {
                        SuggestionDatabase(context).clearDb()
                        withContext(AppDispatchers.Main) {
                            Toast.makeText(context, "Wyczyszczono historię wyszukiwarki", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}
