package ru.bratusev.hostesnavigation.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import ovh.plrapps.mapview.MapView
import ovh.plrapps.mapview.MapViewConfiguration
import ovh.plrapps.mapview.ReferentialData
import ovh.plrapps.mapview.ReferentialListener
import ovh.plrapps.mapview.api.addCallout
import ovh.plrapps.mapview.api.addMarker
import ovh.plrapps.mapview.api.setMarkerTapListener
import ovh.plrapps.mapview.core.TileStreamProvider
import ovh.plrapps.mapview.markers.MarkerTapListener
import ovh.plrapps.mapview.paths.PathView
import ovh.plrapps.mapview.paths.addPathView
import ovh.plrapps.mapview.paths.removePathView
import ovh.plrapps.mapview.util.AngleDegree
import ru.bratusev.hostesnavigation.R
import ru.bratusev.hostesnavigation.navigation.Map
import ru.bratusev.hostesnavigation.navigation.Navigation
import java.io.InputStream

class MapHelper(
    private val context: Context,
    private val mapView: MapView,
    private val tileLevel: Int,
    private val navigation: Navigation
) : TileStreamProvider {

    private val markerList = ArrayList<MapMarker>()

    private val finishMarker = AppCompatImageView(context).apply {
        setImageResource(R.drawable.finish_marker)
    }

    private val positionMarker = AppCompatImageView(context).apply {
        setImageResource(R.drawable.position_marker)
    }

    private lateinit var pathView: PathView

    private var newScale = 0f
    private var widthMax = 50f
    private var widthMin = 10f
    private var maxScale = 2f
    private var minScale = 0f
    internal var rotation = 0F

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

    private fun addDefaultMarker(x: Double, y: Double, name: String, angel: Float = 0f) {
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
        pathView = PathView(context)
        return MapViewConfiguration(5, 3840, 2160, 256, this).setMaxScale(2f)
            .enableRotation().setStartScale(0f)
    }

    override fun getTileStream(row: Int, col: Int, zoomLvl: Int): InputStream? {
        return try {
            context.assets?.open("tiles$tileLevel/$zoomLvl/$row/$col.jpg")
        } catch (e: Exception) {
            context.assets?.open("tiles$tileLevel/blank.png")
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
            newScale = refData.scale
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

    private fun setMarkerScale(scale: Float) {
        positionMarker.scaleX = scale + 1f
        positionMarker.scaleY = scale + 1f
    }

    private fun setMarkerScale(scale: Float, mapMarker: MapMarker) {
        val mapMax = 2.0
        val mapMin = 0.3
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

    internal fun addPathView(){
        mapView.addPathView(pathView)
    }

    internal fun deletePaths() {
        mapView.removePathView(pathView)
        pathView = PathView(context)
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

    internal fun setScale(scale: Float) {
        mapView.scale = scale
    }

    internal fun getScale(): Float {
        return mapView.scale
    }

    private val strokePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.brand)
        strokeCap = Paint.Cap.ROUND
    }

    internal fun updatePath(start: Int, finish: Int) {
        val myPath = navigation.path(start, finish)
        var temp = widthMin + (widthMax - widthMin) * newScale / maxScale
        if (newScale == minScale) temp = widthMin
        else if (newScale == maxScale) temp = widthMax
        val drawablePath = object : PathView.DrawablePath {
            override val visible: Boolean = true
            override var path: FloatArray = myPath as FloatArray
            override var paint: Paint? = strokePaint
            override val width: Float = temp
        }

        pathView.updatePaths(listOf(drawablePath))
        addPathView()
    }

    internal fun addAllMarkers(dotList: java.util.ArrayList<Map.Dot>) {
        try {
            for (dot in dotList) {
                addDefaultMarker(
                    dot.getX().toDouble(),
                    dot.getY().toDouble(),
                    dot.getId().toString()
                )
            }
        } catch (e: Exception) {
            Log.d("MyLog", e.message.toString())
        }
    }
}