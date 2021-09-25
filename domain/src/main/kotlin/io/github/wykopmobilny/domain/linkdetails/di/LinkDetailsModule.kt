package io.github.wykopmobilny.domain.linkdetails.di

import com.dropbox.android.external.store4.StoreBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.github.wykopmobilny.api.endpoints.LinksRetrofitApi
import io.github.wykopmobilny.data.cache.api.AppCache
import io.github.wykopmobilny.domain.api.apiFetcher
import io.github.wykopmobilny.domain.di.ScopeInitializer
import io.github.wykopmobilny.domain.linkdetails.GetLinkDetailsQuery
import io.github.wykopmobilny.domain.linkdetails.InitializeLinkDetails
import io.github.wykopmobilny.domain.linkdetails.datasource.linkCommentsSourceOfTruth
import io.github.wykopmobilny.domain.linkdetails.datasource.linkDetailsSourceOfTruth
import io.github.wykopmobilny.links.details.GetLinkDetails
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.base.SimpleViewStateStorage

@Module
internal abstract class LinkDetailsModule {

    companion object {

        @LinkDetailsScope
        @Provides
        fun viewState() = SimpleViewStateStorage()

        @LinkDetailsScope
        @Provides
        fun linkDetailsStore(
            retrofitApi: LinksRetrofitApi,
            appScopes: AppScopes,
            cache: AppCache,
        ) = StoreBuilder.from(
            fetcher = apiFetcher { linkId -> retrofitApi.getLink(linkId) },
            sourceOfTruth = linkDetailsSourceOfTruth(cache),
        )
            .scope(appScopes.applicationScope)
            .build()

        @LinkDetailsScope
        @Provides
        fun linkComments(
            retrofitApi: LinksRetrofitApi,
            appScopes: AppScopes,
            cache: AppCache,
        ) = StoreBuilder.from(
            fetcher = apiFetcher { linkId ->
                retrofitApi.getLinkComments(
                    linkId = linkId,
                    sortBy = "old", // we'll sort it on the app side 😬
                )
            },
            sourceOfTruth = linkCommentsSourceOfTruth(cache),
        )
            .scope(appScopes.applicationScope)
            .build()
    }

    @Binds
    abstract fun GetLinkDetailsQuery.getProfileDetails(): GetLinkDetails

    @Binds
    abstract fun InitializeLinkDetails.scopeInitializer(): ScopeInitializer
}
