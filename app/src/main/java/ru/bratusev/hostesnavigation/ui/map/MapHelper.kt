package ru.bratusev.hostesnavigation.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import ovh.plrapps.mapview.MapView
import ovh.plrapps.mapview.MapViewConfiguration
import ovh.plrapps.mapview.ReferentialData
import ovh.plrapps.mapview.ReferentialListener
import ovh.plrapps.mapview.api.addCallout
import ovh.plrapps.mapview.api.addMarker
import ovh.plrapps.mapview.api.setMarkerTapListener
import ovh.plrapps.mapview.core.TileStreamProvider
import ovh.plrapps.mapview.markers.MarkerTapListener
import ovh.plrapps.mapview.util.AngleDegree
import ru.bratusev.hostesnavigation.R
import java.io.InputStream

class MapHelper(private val context: Context, private val mapView: MapView) : TileStreamProvider {

    private val markerList = ArrayList<MapMarker>()

    private val finishMarker = AppCompatImageView(context).apply {
        setImageResource(R.drawable.finish_marker)
    }

    private val positionMarker = AppCompatImageView(context).apply {
        setImageResource(R.drawable.position_marker)
    }

    init {
        mapView.configure(generateConfig())
        mapView.defineBounds(0.0, 0.0, 3840.0, 2160.0)
    }

    internal fun addPositionMarker(x: Double, y: Double) {
        mapView.addMarker(positionMarker, x, y, -0.5f, -0.5f)
    }

    internal fun addFinishMarker(x: Double, y: Double) {
        mapView.addMarker(finishMarker, x, y, -0.5f, -0.5f)
    }

    internal fun addDefaultMarker(x: Double, y: Double, name: String, angel: Float = 0f) {
        val marker = MapMarker(context, x, y, name).apply {
            setImageDrawable(BitmapDrawable(resources, drawText("$name ")))
        }
        marker.rotation = angel
        markerList.add(marker)
        mapView.addMarker(marker, x, y)
    }

    private fun drawText(
        text: String = "Г-320",
        textColor: Int = Color.WHITE,
        textSize: Float = 18F * 2,
        typeface: Typeface = Typeface.SERIF,
        style: Int = Typeface.BOLD,
        isUnderline: Boolean = false,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(
            (textSize.toInt() * text.length / 1.6).toInt(),
            textSize.toInt(),
            Bitmap.Config.ARGB_8888
        )
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

    private fun generateConfig(): MapViewConfiguration {
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

    private val refOwner = object : ReferentialListener {
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

        internal var angleDegree: AngleDegree = 0f
            set(value) {
                field = value
                rotateMaker()
            }

        private fun rotateMaker() {
            val refData = referentialData ?: return
            positionMarker.rotation = angleDegree
        }
    }

    private fun setMarkerScale(scale: Float) {
        positionMarker.scaleX = scale + 1f
        positionMarker.scaleY = scale + 1f
    }

    private fun setMarkerScale(scale: Float, mapMarker: MapMarker) {
        val mapMax = 2.0
        val mapMin = 0.1875
        val markerMin = 0
        val markerMax = 3
        var tmp =
            (((scale - mapMin) / (mapMax - mapMin)) * (markerMax - markerMin) + markerMin).toFloat()
        tmp = if (tmp > 0) tmp else 0.0F
        mapMarker.scaleX = tmp
        mapMarker.scaleY = tmp
    }

    internal fun addReferentialListener() {
        mapView.addReferentialListener(refOwner)
    }

    internal fun addMarkerClickListener() {
        mapView.setMarkerTapListener(object : MarkerTapListener {
            override fun onMarkerTap(view: View, x: Int, y: Int) {
                if (view is MapMarker) {
                    val callout = MarkerCallout(mapView.context)
                    callout.setTitle(view.name)
                    callout.setSubTitle("position: ${view.x} , ${view.y}")
                    mapView.addCallout(callout, view.x, view.y, -0.5f, -1.2f, 0f, 0f)
                    callout.transitionIn()
                }
            }
        })
    }
}