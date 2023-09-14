package ru.bratusev.hostesnavigation.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.NumberPicker
import androidx.core.view.size
import ovh.plrapps.mapview.MapView
import ru.bratusev.hostesnavigation.R
import ru.bratusev.hostesnavigation.navigation.Navigation
import ru.bratusev.hostesnavigation.ui.main.MainFragment
import ru.bratusev.hostesnavigation.ui.map.MapConstants.dotList
import ru.bratusev.hostesnavigation.ui.map.MapConstants.finishNode
import ru.bratusev.hostesnavigation.ui.map.MapConstants.levelArray
import ru.bratusev.hostesnavigation.ui.map.MapConstants.levelNumber
import ru.bratusev.hostesnavigation.ui.map.MapConstants.startNode
import ru.bratusev.hostesnavigation.ui.map.MapConstants.zoomLevelCount

class MapConnector(
    private val context: Context,
    private val parentView: ViewGroup,
    private val locationName: String
) :
    NumberPicker.OnValueChangeListener, OnClickListener {

    private lateinit var mapView: MapView
    private lateinit var mapHelper: MapHelper
    private lateinit var levelPicker: NumberPicker
    private lateinit var zoomIn: ImageButton
    private lateinit var zoomOut: ImageButton
    private var navigation: Navigation = Navigation()

    /**
     * @Param [fileHelper] класс для работы с файловой системой
     * @See [FileHelper]
     * */
    private var fileHelper = FileHelper(context, MainFragment(), locationName)

    init {
        val json = fileHelper.getJsonMap(locationName)
        if (json != "empty location") {
            loadFromString(json)
            configureViews(parentView)
        }
    }

    /**
     * Метод для настройки view выбора этажа
     * @Param [levelPicker] view для выбора номера этажа
     * @See [MapFragment.initPickerWithString]
     * */
    private fun configureLevelPicker(levelPicker: NumberPicker) {
        try {
            levelArray.reverse()
            levelPicker.wrapSelectorWheel = false
            initPickerWithString(1, levelArray.size, levelPicker, levelArray.toTypedArray())
            val delta =
                (levelPicker.minValue + levelPicker.value - levelArray.size) % levelPicker.size
            levelPicker.scrollBy(0, -(delta * levelPicker.getChildAt(0).height))
            levelPicker.setOnValueChangedListener(this)
        } catch (e: Exception) {
            Log.d("MyLog", e.message.toString())
        }
    }

    /**
     * Метод для настройки диапазона значений, отображаемых в [levelPicker]
     * @Param [min] минимальный номер этажа
     * @Param [max] максимальный номер этажа
     * @Param [p] view для выбора этажа которую мы настраиваем
     * @Param [levels] массив номеров этажей
     *
     * @See [MapFragment.configureLevelPicker]
     * */
    private fun initPickerWithString(min: Int, max: Int, p: NumberPicker, levels: Array<String>) {
        p.minValue = min
        p.maxValue = max
        p.displayedValues = levels
    }

    /**
     * Метод для поиска и настройки работы с view элементами
     * @Param [view] родительское view
     * @Param [confMap] флаг для проверки сконфигурированность [mapView]
     * */
    private fun configureViews(view: View, confMap: Boolean = true) {
        zoomIn = view.findViewById(R.id.btn_zoomIn)
        zoomOut = view.findViewById(R.id.btn_zoomOut)
        zoomIn.setOnClickListener(this)
        zoomOut.setOnClickListener(this)
        levelPicker = view.findViewById(R.id.picker)
        mapView = view.findViewById(R.id.mapView) ?: return
        configureLevelPicker(levelPicker)
        if (confMap) configureMapView(mapView)
    }

    /**
     * Метод для настройки [mapHelper]
     * @See [MapHelper]
     * @Param [mapView] view для настройки
     * @Param [scale] уровень приближения карты
     * */
    private fun configureMapView(
        mapView: MapView,
        scale: Float = 0f
    ) {
        mapHelper =
            MapHelper(context, mapView, locationName, navigation)
        mapHelper.setScale(scale)
        mapHelper.addAllMarkers(dotList)
        mapHelper.addReferentialListener()
        mapHelper.addMarkerClickListener()
        mapHelper.addPositionMarker(startNode.toString(), 0f)
        mapHelper.addFinishMarker(finishNode.toString())
    }

    /**
     * Получает [dotList], [mapWidth], [mapHeight] и [zoomLevelCount] из json строки
     * @Param [json] - строка в формате json с графом
     * @See [Map]
     */
    private fun loadFromString(json: String) {
        zoomLevelCount = fileHelper.getLevelCount("tiles1") - 1
        navigation.loadMapFromJson(json)
    }

    /**
     * Метод для обработки обновления значения [levelPicker]
     * @See [MapFragment.configureViews]
     * @See [MapFragment.configureMapView]
     * */
    @SuppressLint("ResourceAsColor", "SoonBlockedPrivateApi")
    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        try {
            levelNumber = levelArray[picker?.value!! - 1].toInt()
            if (oldVal != newVal) {
                updateViews()
                configureViews(parentView, false)
                configureMapView(mapView, mapHelper.getScale())
                mapHelper.updatePath()
            }
        } catch (e: Exception) {
            Log.d("MyLog", e.message.toString())
        }
    }

    /**
     * Метод для пересоздания всех view на экране
     * */
    private fun updateViews() {
        parentView.removeAllViewsInLayout()
        mapView = MapView(context)
        parentView.addView(mapView)
        parentView.addView(levelPicker)
        parentView.addView(zoomIn)
        parentView.addView(zoomOut)
    }

    /**
     * Метод для построения маршрута
     * @Param [finish] идентификатор точки конца маршрута
     * */
    internal fun updatePath(finish: Int) {
        finishNode = finish
        mapHelper.updatePath()
        mapHelper.movePosition(startNode.toString())
    }

    /**
     * Обработчки нажатий на кнопки приближения и отдаления карты
     * */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_zoomIn -> mapHelper.zoomIn()
            R.id.btn_zoomOut -> mapHelper.zoomOut()
        }
    }
}