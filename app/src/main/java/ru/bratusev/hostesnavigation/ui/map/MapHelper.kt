package ru.bratusev.hostesnavigation.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.widget.AppCompatImageView
import ovh.plrapps.mapview.MapView
import ovh.plrapps.mapview.MapViewConfiguration
import ovh.plrapps.mapview.ReferentialData
import ovh.plrapps.mapview.ReferentialListener
import ovh.plrapps.mapview.api.addMarker
import ovh.plrapps.mapview.core.TileStreamProvider
import ovh.plrapps.mapview.util.AngleDegree
import ru.bratusev.hostesnavigation.R
import java.io.InputStream

class MapHelper(private val context: Context) : TileStreamProvider {

    private val markerList = ArrayList<MapMarker>()

    internal val finishMarker = AppCompatImageView(context).apply {
        setImageResource(R.drawable.finish_marker)
    }

    internal val positionMarker = AppCompatImageView(context).apply {
        setImageResource(R.drawable.position_marker)
    }

    fun addPositionMarker(mapView: MapView, x: Double, y: Double) {
        mapView.addMarker(positionMarker, x, y, -0.5f, -0.5f)
    }

    internal fun addDefaultMarker(mapView: MapView, x: Double, y: Double, name: String) {
        val marker = MapMarker(context, x, y, name).apply {
            setImageDrawable(BitmapDrawable(resources, drawText()))
        }
        markerList.add(marker)
        mapView.addMarker(marker, x, y)
    }

    private fun drawText(
        text: String = "Г-320",
        textColor: Int = Color.WHITE,
        textSize: Float = 18F*2,
        typeface: Typeface = Typeface.SERIF,
        style: Int = Typeface.BOLD,
        isUnderline: Boolean = false,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap((textSize.toInt() * text.length / 1.6).toInt(), textSize.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap).apply {}

        val paint = Paint().apply {
            isAntiAlias = true
            color = textColor
            this.textSize = textSize
            this.typeface = typeface
            setTypeface(Typeface.create(typeface, style))
            if (isUnderline) {
                flags = Paint.UNDERLINE_TEXT_FLAG
            }
        }
        canvas.drawText(text, 10F, canvas.height.toFloat(), paint)
        return bitmap
    }

    internal fun generateConfig(): MapViewConfiguration {
        return MapViewConfiguration(5, 3840, 2160, 256, this).setMaxScale(2f)
            .enableRotation().setStartScale(0f)
    }

    override fun getTileStream(row: Int, col: Int, zoomLvl: Int): InputStream? {
        return try {
            context.assets?.open("tiles/$zoomLvl/$row/$col.jpg")
        } catch (e: Exception) {
            context.assets?.open("tiles/blank.png")
        }
    }

    /*internal fun addMarkers(mapView: MapView, markerList: java.util.ArrayList<MapMarker>) {
        for (mapMarker in markerList) {
            mapView.addMarker(
                getDefaultMarker(),
                mapMarker.x,
                mapMarker.y,
                -0.5f,
                -0.5f,
                -0.5f,
                -0.5f,
                mapMarker.name
            )
        }
    }*/


    internal val refOwner = object : ReferentialListener {
        var referentialData: ReferentialData? = null

        override fun onReferentialChanged(refData: ReferentialData) {
            angleDegree = refData.angle
            setMarkerScale(refData.scale)
            referentialData = refData
            for (mapMarker in markerList) {
                setMarkerScale(refData.scale, mapMarker)
            }
            rotateMaker()
        }

        var angleDegree: AngleDegree = 0f
            set(value) {
                field = value
                rotateMaker()
            }

        private fun rotateMaker() {
            val refData = referentialData ?: return
            positionMarker.rotation = angleDegree
        }
    }

    internal fun setMarkerScale(scale: Float) {
        positionMarker.scaleX = scale + 1f
        positionMarker.scaleY = scale + 1f
    }

    internal fun setMarkerScale(scale: Float, mapMarker: MapMarker) {
        val mapMax = 2.0
        val mapMin = 0.1875
        val markerMin = 0
        val markerMax = 3
        var tmp = (((scale - mapMin) / (mapMax - mapMin)) * (markerMax - markerMin) + markerMin).toFloat()
        tmp = if (tmp > 0) tmp else 0.0F
        mapMarker.scaleX = tmp
        mapMarker.scaleY = tmp
    }
}