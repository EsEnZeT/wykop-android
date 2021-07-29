package io.github.wykopmobilny.domain.di

import dagger.BindsInstance
import dagger.Component
import io.github.wykopmobilny.api.WykopApi
import io.github.wykopmobilny.blacklist.api.Scraper
import io.github.wykopmobilny.data.cache.api.ApplicationCache
import io.github.wykopmobilny.domain.blacklist.di.BlacklistDomainComponent
import io.github.wykopmobilny.domain.login.ConnectConfig
import io.github.wykopmobilny.domain.login.di.LoginDomainComponent
import io.github.wykopmobilny.domain.navigation.Framework
import io.github.wykopmobilny.domain.navigation.InteropModule
import io.github.wykopmobilny.domain.navigation.InteropRequestService
import io.github.wykopmobilny.domain.profile.di.ProfileDomainComponent
import io.github.wykopmobilny.domain.promoted.PromotedModule
import io.github.wykopmobilny.domain.settings.di.SettingsDomainComponent
import io.github.wykopmobilny.domain.styles.di.StylesDomainComponent
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.storage.api.Storages
import io.github.wykopmobilny.ui.base.AppScopes
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        InteropModule::class,
        StoresModule::class,
        PromotedModule::class,
    ],
    dependencies = [
        Storages::class,
        Scraper::class,
        WykopApi::class,
        Framework::class,
        ApplicationCache::class,
    ],
)
interface DomainComponent {

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance appScopes: AppScopes,
            @BindsInstance connectConfig: ConnectConfig,
            storages: Storages,
            scraper: Scraper,
            wykop: WykopApi,
            framework: Framework,
            applicationCache: ApplicationCache,
        ): DomainComponent
    }

    fun login(): LoginDomainComponent

    fun styles(): StylesDomainComponent

    fun settings(): SettingsDomainComponent

    fun blacklist(): BlacklistDomainComponent

    fun profile(): ProfileDomainComponent.Factory

    fun navigation(): InteropRequestService

    fun settingsApiInterop(): SettingsPreferencesApi
}
