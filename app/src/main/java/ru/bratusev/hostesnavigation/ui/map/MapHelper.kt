/**
 * Вспомогательный класс для работы с MapView
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package ru.bratusev.hostesnavigation.ui.map

import android.annotation.SuppressLint
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
import ovh.plrapps.mapview.api.removeMarker
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
import ru.bratusev.hostesnavigation.ui.map.MapConstants.finishNode
import ru.bratusev.hostesnavigation.ui.map.MapConstants.levelNumber
import ru.bratusev.hostesnavigation.ui.map.MapConstants.mapHeight
import ru.bratusev.hostesnavigation.ui.map.MapConstants.mapWidth
import ru.bratusev.hostesnavigation.ui.map.MapConstants.markerList
import ru.bratusev.hostesnavigation.ui.map.MapConstants.maxPathWidth
import ru.bratusev.hostesnavigation.ui.map.MapConstants.maxScale
import ru.bratusev.hostesnavigation.ui.map.MapConstants.minPathWidth
import ru.bratusev.hostesnavigation.ui.map.MapConstants.minScale
import ru.bratusev.hostesnavigation.ui.map.MapConstants.startNode
import ru.bratusev.hostesnavigation.ui.map.MapConstants.unzipPath
import ru.bratusev.hostesnavigation.ui.map.MapConstants.zoomLevelCount
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.math.atan

/**
 * @Constructor Create empty Map helper
 * @Param [context] контекст для работы с ресурсами
 * @Param [mapView] MapView для конфигурации карты
 * @Param [locationName] название локации
 * @Param [navigation] навигатор для поиска маршрута
 */
class MapHelper(
    private val context: Context,
    private val mapView: MapView,
    private val locationName: String,
    private val navigation: Navigation
) : TileStreamProvider {

    /** @Param [finishMarker] – маркер конца маршрута на карте */
    private val finishMarker = AppCompatImageView(context).apply {
        setImageResource(R.drawable.finish_marker)
    }

    /** @Param [positionMarker] – маркер текущего положения пользователя на карте */
    private val positionMarker = AppCompatImageView(context).apply {
        setImageResource(R.drawable.position_marker)
    }

    /** @Param [pathView] – конструктор маршрута для пользователя */
    private lateinit var pathView: PathView

    /** @Param [newScale] – текущее приблежение карты */
    private var newScale = 0f

    /**
     * @Param [isPathSet] – флаг отображения маршрута
     * @Param [isFinishSet] – флаг отображения метки конца маршрута
     * */
    private var isPathSet = false
    private var isFinishSet = false
    private var isPositionSet = false

    /**
     * Метод для первичной настройки MapView
     * @See [MapHelper.generateConfig]
     * */
    init {
        markerList.clear()
        mapView.configure(generateConfig(zoomLevelCount, mapWidth, mapHeight))
        mapView.defineBounds(0.0, 0.0, mapWidth.toDouble(), mapHeight.toDouble())
    }

    /**
     * Добавляет на карту маркер с текущим положением пользователя
     * @Param [x] - X координата маркера на карте
     * @Param [y] - Y координата маркера на карте
     */
    internal fun addPositionMarker(x: Double, y: Double) {
        val level = if (startNode > 134) 2 else 1
        if (level == levelNumber) mapView.addMarker(positionMarker, x, y, -0.5f, -0.5f)
    }

    /**
     * Добавляет на карту маркер конца маршрута
     * @Param [x] - X координата маркера на карте
     * @Param [y] - Y координата маркера на карте
     */
    internal fun addFinishMarker(x: Double, y: Double) {
        mapView.addMarker(finishMarker, x, y, -0.5f, -0.5f)
    }

    /**
     * Функция установки метки конца маршрута
     * @Param [id] - уникальный идентификатор точки на графе
     * */
    private fun addFinishMarker(id: String) {
        var x = 0.0
        var y = 0.0
        for (marker in markerList) {
            if (marker.name == id) {
                x = marker.x
                y = marker.y
                break
            }
        }
        try {
            mapView.removeMarker(finishMarker)
        } catch (e: Exception) {
        }
        mapView.addMarker(finishMarker, x, y, -0.5f, -0.5f)
    }

    /**
     * Функция установки метки текущего положения
     * @Param [id] - уникальный идентификатор точки на графе
     * */
    private fun addPositionMarker(id: String) {
        var x = 0.0
        var y = 0.0
        for (marker in markerList) {
            if (marker.name == id) {
                x = marker.x
                y = marker.y
                break
            }
        }
        try {
            mapView.removeMarker(positionMarker)
        } catch (e: Exception) {
        }
        mapView.addMarker(positionMarker, x, y, -0.5f, -0.5f)
    }

    /**
     * Функция установки стандартой метки на карту
     * @Param [x] - координата по горизонтали
     * @Param [y] - координата по вертикали
     * @Param [level] - номер отображаемого этажа
     * @Param [name] - описание маркера. В данной реализации его идентификатор
     * @Param [angel] - угол поворота маркера
     * */
    private fun addDefaultMarker(
        x: Double,
        y: Double,
        level: Int,
        name: String,
        angel: Float = 0f
    ) {
        val marker = MapMarker(context, x, y, name).apply {
            setImageDrawable(BitmapDrawable(resources, drawText("$name ")))
        }
        marker.rotation = angel
        markerList.add(marker)
        Log.d("MarkerLog", "${marker.name} $levelNumber $level")
        if (levelNumber == level) mapView.addMarker(marker, x, y)
    }

    /**
     * Функция отрисовки текста на карте
     * @Param [text] - отображаемый текст
     * @Param [textColor] - цвет отображаемого текста
     * @Param [textSize] - размер шрифта текста
     * @Param [typeface] - семейство шрифтов
     * @Param [style] - стиль шрифта
     * @Param [isUnderline] - наличие подчеркивания
     * @Return возвращает карту с отрисованным на ней текстом
     * */
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

    /**
     * Функция генерации базовой конфигурации MapView
     * @Param [levelCount] - количество уровней приближения
     * @Param [fullWidth] - фактическая ширина карты
     * @Param [fullHeight] - фактическая высота карты
     * */
    private fun generateConfig(
        levelCount: Int,
        fullWidth: Int,
        fullHeight: Int
    ): MapViewConfiguration {
        pathView = PathView(context)
        return MapViewConfiguration(levelCount, fullWidth, fullHeight, 256, this).setMaxScale(
            maxScale
        )
            .enableRotation().setStartScale(minScale)
    }

    /**
     * Метод для подгрузки тайлов карты к MapView*/
    @SuppressLint("SdCardPath")
    override fun getTileStream(row: Int, col: Int, zoomLvl: Int): InputStream? {
        return try {
            FileInputStream(
                File(
                    "$unzipPath/$locationName/",
                    "tiles$levelNumber/$zoomLvl/$row/$col.jpg"
                )
            )
        } catch (e: Exception) {
            Log.d("MyLog", e.message.toString() + e.stackTraceToString())
            FileInputStream(File("$unzipPath/$locationName/", "tiles$levelNumber/blank.png"))
        }
    }

    /**
     * @Param [refOwner] – обработчик вращения и изменения размера карты
     * */
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

    /**
     * Метод устанавливает приближение маркера метоположения
     * @Param [scale] уровень приближения
     * */
    private fun setMarkerScale(scale: Float) {
        positionMarker.scaleX = scale + 1f
        positionMarker.scaleY = scale + 1f
        finishMarker.scaleX = scale + 1f
        finishMarker.scaleY = scale + 1f
    }

    /**
     * Метод устанавливает приближение маркера местоположения
     * @Param [scale] уровень приближения маркера
     * @Param [mapMarker] настраиваемый маркер
     * */
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

    /**
     * Метод для размещения слушателя [refOwner]
     * @See [MapHelper.refOwner]
     */
    internal fun addReferentialListener() {
        mapView.addReferentialListener(refOwner)
    }

    /**
     * Метод для добавления маршрута [pathView] на [mapView]
     * */
    private fun addPathView() {
        isPathSet = true
        mapView.addPathView(pathView)
    }

    /**
     * Метод для удаления маршрута с карты [mapView]
     */
    internal fun deletePaths() {
        mapView.removePathView(pathView)
        pathView = PathView(context)
    }

    /**
     * Метод для установки обработчика нажатий на маркер
     * @See [MarkerCallout]
     */
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

    /**
     * Устанавливает уровень увеличения
     * @Param [scale] значение уровня приближения
     */
    internal fun setScale(scale: Float) {
        mapView.scale = scale
    }

    /**
     * Возвращает уровень текущего приближения карты
     * @Return текущий уровень приближения
     */
    internal fun getScale(): Float {
        return mapView.scale
    }

    /**
     *  @Param [strokePaint] настройка кисти для рисования маршрута на карте
     * */
    private val strokePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.brand)
        strokeCap = Paint.Cap.ROUND
    }

    /**
     * Метод для построения маршрута на карте
     * @See [Navigation]
     */
    internal fun updatePath() {
        var myStart = startNode
        var myFinish = finishNode
        if(levelNumber == 1){
            if(finishNode > 134) myFinish = 33
            else addFinishMarker(myFinish.toString())
            if(startNode < 135) addPositionMarker(myStart.toString())
            else myStart = 33
        }else {
            if(finishNode < 135) myFinish = 135
            else addFinishMarker(myFinish.toString())
            if(startNode > 134) addPositionMarker(myStart.toString())
            else myStart = 135
        }
        val myPath = navigation.path(myStart, myFinish)
        if(myPath?.size!! > 3){
            val x1 = myPath[0].toInt()
            val y1 = myPath[1].toInt()
            val x2 = myPath[2].toInt()
            val y2 = myPath[3].toInt()
            positionMarker.rotation += calculateAngel(x1,y1,x2,y2)
        }
        var temp = minPathWidth + (maxPathWidth - minPathWidth) * newScale / maxScale
        if (newScale == minScale) temp = minPathWidth
        else if (newScale == maxScale) temp = maxPathWidth
        val drawablePath = object : PathView.DrawablePath {
            override val visible: Boolean = true
            override var path: FloatArray = myPath as FloatArray
            override var paint: Paint? = strokePaint
            override val width: Float = temp
        }

        pathView.updatePaths(listOf(drawablePath))
        if (!isFinishSet && myFinish == finishNode) addFinishMarker(myFinish.toString())
        if (!isPathSet) addPathView()
    }

    /**
     * Метод для подсчёта угла поворота маркера положения пользователя
     * @Param [x1] X координата пользователя
     * @Param [y1] Y координата пользователя
     * @Param [x2] X координата следующей точки по маршруту
     * @Param [y2] Y координата следующей точки по маршруту
     * */
    private fun calculateAngel(x1: Int, y1: Int, x2: Int, y2: Int): Float {
        var angel = 90.0f
        val tmp = (atan(((y2-y1).toDouble()/(x2-x1).toDouble()))*180/Math.PI).toFloat()
        angel += if(x2-x1 >= 0) tmp
        else tmp+180
        return angel
    }

    /**
     * Метод для добавления всех маркеров на карту
     * @Param [dotList] массив маркеров для карты
     * @See [Map.Dot]
     */
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

    /**
     * Метод для увеличения карты кнопкой
     */
    internal fun zoomIn() {
        newScale += (maxScale - minScale) / zoomLevelCount
        if (newScale > maxScale) newScale = maxScale
        mapView.smoothScaleFromFocalPoint(mapView.offsetX, mapView.offsetY, newScale)
    }

    /**
     * Метод для уменьшения карты кнопкой
     */
    internal fun zoomOut() {
        newScale -= (maxScale - minScale) / zoomLevelCount
        if (newScale < minScale) newScale = minScale
        mapView.smoothScaleFromFocalPoint(mapView.offsetX, mapView.offsetY, newScale)
    }
}