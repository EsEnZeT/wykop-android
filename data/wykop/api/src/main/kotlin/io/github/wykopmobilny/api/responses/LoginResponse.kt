package io.github.wykopmobilny.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @field:Json(name = "profile") val profile: ProfileResponse,
    @field:Json(name = "userkey") val userkey: String,
)
