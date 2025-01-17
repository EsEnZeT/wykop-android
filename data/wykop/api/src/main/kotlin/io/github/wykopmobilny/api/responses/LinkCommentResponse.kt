package io.github.wykopmobilny.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.datetime.Instant

@JsonClass(generateAdapter = true)
data class LinkCommentResponse(
    @field:Json(name = "id") val id: Long,
    @field:Json(name = "date") val date: Instant,
    @field:Json(name = "author") val author: AuthorResponse,
    @field:Json(name = "vote_count") val voteCount: Int,
    @field:Json(name = "vote_count_plus") val voteCountPlus: Int,
    @field:Json(name = "body") val body: String?,
    @field:Json(name = "parent_id") val parentId: Long,
    @field:Json(name = "can_vote") val canVote: Boolean,
    @field:Json(name = "user_vote") val userVote: Int,
    @field:Json(name = "blocked") val blocked: Boolean,
    @field:Json(name = "favorite") val favorite: Boolean,
    @field:Json(name = "link_id") val linkId: Long,
    @field:Json(name = "embed") val embed: EmbedResponse?,
    @field:Json(name = "app") val app: String?,
    @field:Json(name = "violation_url") val violationUrl: String?,
)
