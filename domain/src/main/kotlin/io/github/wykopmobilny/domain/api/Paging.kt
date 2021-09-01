package io.github.wykopmobilny.domain.api

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class StoreMediator<T : Any> @Inject constructor(
    private val store: Store<Int, T>,
) : RemoteMediator<Int, T>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, T>): MediatorResult {
        val loadKey = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> state.anchorPosition
        }
        val response = store.stream(StoreRequest.fresh(loadKey ?: 1))
            .first {
                when (it) {
                    is StoreResponse.Loading -> false
                    is StoreResponse.Data,
                    is StoreResponse.NoNewData,
                    is StoreResponse.Error,
                    -> true
                }
            }

        return when (response) {
            is StoreResponse.Loading -> error("excluded")
            is StoreResponse.Data -> MediatorResult.Success(endOfPaginationReached = false)
            is StoreResponse.NoNewData -> MediatorResult.Success(endOfPaginationReached = true)
            is StoreResponse.Error.Exception -> MediatorResult.Error(response.error)
            is StoreResponse.Error.Message -> MediatorResult.Error(Exception(response.message))
        }
    }
}

class PagingSource<T : Any> @Inject constructor(
    private val store: Store<Int, List<T>>,
) : PagingSource<Int, T>() {

    override suspend fun load(
        params: LoadParams<Int>,
    ): LoadResult<Int, T> =
        runCatching {
            val nextPageNumber = params.key ?: 1
            LoadResult.Page(
                data = store.get(nextPageNumber),
                prevKey = null,
                nextKey = nextPageNumber + 1,
            )
        }
            .getOrElse { LoadResult.Error(it) }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? =
        state.anchorPosition?.let(state::closestPageToPosition).let { anchorPage ->
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
}