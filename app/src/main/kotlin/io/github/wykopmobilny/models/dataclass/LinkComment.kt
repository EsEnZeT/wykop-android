package io.github.wykopmobilny.models.dataclass

import android.os.Parcel
import android.os.Parcelable
import io.github.wykopmobilny.utils.toPrettyDate
import kotlinx.datetime.Instant

class LinkComment(
    val id: Long,
    val author: Author,
    val fullDate: Instant,
    var body: String?,
    val favorite: Boolean,
    var voteCount: Int,
    var voteCountPlus: Int,
    var voteCountMinus: Int,
    var userVote: Int,
    var parentId: Long,
    val canVote: Boolean,
    val linkId: Long,
    var embed: Embed?,
    val app: String?,
    var isCollapsed: Boolean,
    var isParentCollapsed: Boolean,
    var childCommentCount: Int,
    val violationUrl: String?,
    var isNsfw: Boolean = false,
    var isBlocked: Boolean = false,
) : Parcelable {

    val url = "https://www.wykop.pl/link/$linkId/#comment-$id"

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readParcelable(Author::class.java.classLoader)!!,
        Instant.fromEpochMilliseconds(parcel.readLong()),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong(),
        parcel.readParcelable(Embed::class.java.classLoader),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeParcelable(author, flags)
        parcel.writeLong(fullDate.toEpochMilliseconds())
        parcel.writeString(body)
        parcel.writeByte(if (favorite) 1 else 0)
        parcel.writeInt(voteCount)
        parcel.writeInt(voteCountPlus)
        parcel.writeInt(voteCountMinus)
        parcel.writeInt(userVote)
        parcel.writeLong(parentId)
        parcel.writeByte(if (canVote) 1 else 0)
        parcel.writeLong(linkId)
        parcel.writeParcelable(embed, flags)
        parcel.writeString(app)
        parcel.writeByte(if (isCollapsed) 1 else 0)
        parcel.writeByte(if (isParentCollapsed) 1 else 0)
        parcel.writeInt(childCommentCount)
        parcel.writeString(violationUrl)
        parcel.writeByte(if (isNsfw) 1 else 0)
        parcel.writeByte(if (isBlocked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LinkComment> {
        override fun createFromParcel(parcel: Parcel): LinkComment {
            return LinkComment(parcel)
        }

        override fun newArray(size: Int): Array<LinkComment?> {
            return arrayOfNulls(size)
        }
    }

    val date: String
        get() = this.fullDate.toPrettyDate()
}
