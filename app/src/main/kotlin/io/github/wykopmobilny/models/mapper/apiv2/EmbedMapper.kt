package io.github.wykopmobilny.models.mapper.apiv2

import io.github.wykopmobilny.api.responses.EmbedResponse
import io.github.wykopmobilny.api.responses.properUrl
import io.github.wykopmobilny.models.dataclass.Embed
import io.github.wykopmobilny.models.mapper.Mapper

object EmbedMapper : Mapper<EmbedResponse, Embed> {

    override fun map(value: EmbedResponse) =
        Embed(value.type, value.preview, value.properUrl, value.plus18, value.source, value.animated, value.size ?: "")
}
