package ru.netology.nmedia.ui.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.yandex.runtime.image.ImageProvider

data class ImageInfo(val drawableRes: Int)

class DrawableImageProvider(private val context: Context) : ImageProvider() {
    private var imageInfo: ImageInfo? = null

    constructor(context: Context, imageInfo: ImageInfo) : this(context) {
        this.imageInfo = imageInfo
    }

    override fun getImage(): Bitmap {
        val drawable: Drawable = context.resources.getDrawable(
            imageInfo!!.drawableRes, context.theme
        )
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun getId(): String = imageInfo!!.drawableRes.toString()
}
