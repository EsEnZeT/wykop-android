package io.github.wykopmobilny.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.utils.FileUtils
import io.github.wykopmobilny.utils.queryFileName
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class WykopImageFile(val uri: Uri, val context: Context) {

    fun getFileMultipart(): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val filename = uri.queryFileName(contentResolver)
        var file: File?
        try {
            file = FileUtils.getFile(context, uri)
            if (file == null) {
                file = saveUri(uri, filename)
            }
        } catch (_: Throwable) {
            file = saveUri(uri, filename)
        }

        var mimetype = contentResolver.getType(uri)

        file?.let {
            val opt = BitmapFactory.Options()
            opt.inJustDecodeBounds = true
            BitmapFactory.decodeFile(it.absolutePath, opt)
            mimetype = opt.outMimeType
        }

        val rotatedFile = file?.let(::ensureRotation)
        Napier.d("Rotated ${rotatedFile!!.name}")
        return MultipartBody.Part.createFormData("embed", rotatedFile.name, rotatedFile.asRequestBody(mimetype?.toMediaTypeOrNull()))
    }

    private fun saveUri(uri: Uri, filename: String): File? {
        val inputStream = context.contentResolver.openInputStream(uri)
        inputStream.use { input ->
            val file = File.createTempFile(filename, "0", context.cacheDir)
            val output = FileOutputStream(file)
            try {
                val buffer = ByteArray(4 * 1024) // or other buffer size

                var read = input!!.read(buffer)
                while (read != -1) {
                    output.write(buffer, 0, read)
                    read = input.read(buffer)
                }

                output.flush()
            } finally {
                output.close()
                return file
            }
        }
    }

    private fun ensureRotation(f: File): File? {
        try {
            val exif = ExifInterface(f.path)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )

            val angle = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> return f
            }

            val mat = Matrix()
            mat.postRotate(angle.toFloat())
            val options = BitmapFactory.Options()
            options.inSampleSize = 2

            val bmp = BitmapFactory.decodeStream(
                FileInputStream(f),
                null,
                options,
            )
            val bitmap = Bitmap.createBitmap(
                bmp!!,
                0,
                0,
                bmp.width,
                bmp.height,
                mat,
                true,
            )

            val file = File.createTempFile("rSaved", ".0", context.cacheDir)
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
            return file
        } catch (error: IOException) {
            Napier.w("-- Error in setting image", error)
        } catch (_: OutOfMemoryError) {
            Napier.w("-- OOM Error in setting image")
        }

        return f
    }
}
