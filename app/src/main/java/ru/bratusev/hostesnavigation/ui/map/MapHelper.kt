package ru.bratusev.hostesnavigation.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Interpolator
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
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
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class MapHelper(
    private val context: Context,
    private val mapView: MapView,
    private val tileLevel: Int,
    private val levelCount: Int,
    private val locationName: String,
    private val mapWidth: Int,
    private val mapHeight: Int,
    private val navigation: Navigation
) : TileStreamProvider {

    private val SDPath = Environment.getExternalStorageDirectory().absolutePath + "/Android/data/ru.bratusev.hostesnavigation"
    private var unzipPath = "$SDPath/files/locations/"

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

    private var isPathSet = false
    private var isFinishSet = false

    init {
        mapView.configure(generateConfig(levelCount, mapWidth, mapHeight))
        mapView.defineBounds(0.0, 0.0, mapWidth.toDouble(), mapHeight.toDouble())
    }

    internal fun addPositionMarker(x: Double, y: Double) {
        val level = if(getStart() > 134) 2 else 1
        if(level == tileLevel) mapView.addMarker(positionMarker, x, y, -0.5f, -0.5f)
    }

    internal fun addFinishMarker(x: Double, y: Double) {
        mapView.addMarker(finishMarker, x, y, -0.5f, -0.5f)
    }

    private fun addFinishMarker(id: String) {
        var x = 0.0
        var y = 0.0
        for (marker in markerList) {
            if(marker.name == id){
                x = marker.x
                y = marker.y
                break
            }
        }
        isFinishSet = true
        mapView.addMarker(finishMarker, x, y, -0.5f, -0.5f)
    }

    private fun addDefaultMarker(x: Double, y: Double, level: Int, name: String, angel: Float = 0f) {
        val marker = MapMarker(context, x, y, name).apply {
            setImageDrawable(BitmapDrawable(resources, drawText("$name ")))
        }
        marker.rotation = angel
        markerList.add(marker)
        if(tileLevel == level) mapView.addMarker(marker, x, y)
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

    private fun generateConfig(levelCount: Int = 5, fullWidth: Int = 3840, fullHeight: Int = 2160): MapViewConfiguration {
        pathView = PathView(context)
        return MapViewConfiguration(levelCount, fullWidth, fullHeight, 256, this).setMaxScale(2f)
            .enableRotation().setStartScale(0f)
    }

    @SuppressLint("SdCardPath")
    override fun getTileStream(row: Int, col: Int, zoomLvl: Int): InputStream? {
        return try {
            FileInputStream(File("$unzipPath/$locationName/", "tiles$tileLevel/$zoomLvl/$row/$col.jpg"))
        } catch (e: Exception) {
            Log.d("MyLog", e.message.toString() + e.stackTraceToString())
            FileInputStream(File("$unzipPath/$locationName/", "tiles$tileLevel/blank.png"))
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
            updatePath()
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

    private fun addPathView(){
        isPathSet = true
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

    internal fun updatePath() {
        var myStart = getStart()
        var myFinish = getFinish()
        if(tileLevel==2) myStart = 135
        else myFinish = 33
        Log.d("MyPath", "$myStart $myFinish")
        val myPath = navigation.path(myStart, myFinish)
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
        if(!isFinishSet && myFinish == getFinish()) addFinishMarker(myFinish.toString())
        if(!isPathSet) addPathView()
    }

    internal fun addAllMarkers(dotList: java.util.ArrayList<Map.Dot>) {
        try {
            for (dot in dotList) {
                addDefaultMarker(
                    dot.getX().toDouble(),
                    dot.getY().toDouble(),
                    dot.getLevel(),
                    dot.getId().toString()
                )
            }
        } catch (e: Exception) {
            Log.d("MyLog", e.message.toString())
        }
    }

    private fun getStart(): Int {
        val sharedPref: SharedPreferences = context.getSharedPreferences("path", Context.MODE_PRIVATE)
        return sharedPref.getInt("start", 0)
    }

    private fun getFinish(): Int {
        val sharedPref: SharedPreferences = context.getSharedPreferences("path", Context.MODE_PRIVATE)
        return sharedPref.getInt("finish", 0)
    }

    internal fun zoomIn() {
        newScale += (maxScale - minScale) / levelCount
        if (newScale > maxScale) newScale = maxScale
        mapView.smoothScaleFromFocalPoint(mapView.offsetX,mapView.offsetY, newScale)
    }

    internal fun zoomOut() {
        newScale -= (maxScale - minScale) / levelCount
        if (newScale < minScale) newScale = minScale
        mapView.smoothScaleFromFocalPoint(mapView.offsetX,mapView.offsetY, newScale)
    }
}