package io.github.wykopmobilny.domain.navigation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

interface InteropRequestService {
    val request: Flow<InteropRequest>
}

internal interface InteropRequestsProvider {

    suspend fun request(navigation: InteropRequest)
}

sealed class InteropRequest {
    object BlackListScreen : InteropRequest()
    data class ShowToast(val message: String) : InteropRequest()
}

@Singleton
internal class InMemoryInteropRequestService @Inject constructor() : InteropRequestService, InteropRequestsProvider {

    override val request = MutableSharedFlow<InteropRequest>()

    override suspend fun request(navigation: InteropRequest) {
        request.emit(navigation)
    }
}
