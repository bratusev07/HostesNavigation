/**
 * Класс для работы с MapFragment
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package ru.bratusev.hostesnavigation.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.NumberPicker
import androidx.core.view.size
import androidx.fragment.app.Fragment
import org.json.JSONObject
import org.json.JSONTokener
import ovh.plrapps.mapview.MapView
import ru.bratusev.hostesnavigation.R
import ru.bratusev.hostesnavigation.navigation.Map
import ru.bratusev.hostesnavigation.navigation.Navigation


/**
 * Класс для работы с MapFragment
 * @Constructor Создаёт пустой MapFragment
 */
class MapFragment : Fragment(), NumberPicker.OnValueChangeListener, OnClickListener{
    private lateinit var parentView: ViewGroup
    private lateinit var mapView: MapView
    private lateinit var mapHelper: MapHelper
    private lateinit var levelPicker: NumberPicker
    private lateinit var zoomIn: ImageButton
    private lateinit var zoomOut: ImageButton
    private lateinit var navigation: Navigation

    /**
     * @Param [dotList] массив точек на карте
     * @See [Map.Dot]
     * @Param [width] реальная ширина карты
     * @Param [height] реальная высота карты
     * @Param [levelCount] количество уровней приближения
     * @Param [locationName] название подгружаемой локации
     * @Param [fileHelper] класс для работы с файловой системой
     * @See [FileHelper]
     * @Param [levelArray] массив этажей в здании
     * */
    private var dotList: ArrayList<Map.Dot> = ArrayList()
    private var width = 0
    private var height = 0
    private var levelCount = 0
    private var locationName = "location1"
    private lateinit var fileHelper: FileHelper
    private val levelArray = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false).also {
            parentView = it as ViewGroup
            fileHelper = FileHelper(requireContext(), locationName)
            //fileHelper.fileDownload("1rq4aFmBEvLCAhXTQ3YPbtaHkoc2_8B8v")
            navigation = Navigation()
            val json = fileHelper.getJsonMap(locationName)
            loadFromString(json)
            configureViews(it)
        }
    }

    /**
     * Метод для настройки view выбора этажа
     * @Param [levelPicker] view для выбора номера этажа
     *
     * @See [MapFragment.initPickerWithString]
     * */
    private fun configureLevelPicker(levelPicker: NumberPicker) {
        try {
            levelArray.reverse()
            levelPicker.wrapSelectorWheel = false
            initPickerWithString(1, levelArray.size, levelPicker, levelArray.toTypedArray())
            val delta = (levelPicker.minValue + levelPicker.value - levelArray.size) % levelPicker.size
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
     * @Param [confMap] флаг для проверки сконфигурированность [mapView]*/
    private fun configureViews(view: View, confMap: Boolean = true) {
        zoomIn = view.findViewById(R.id.btn_zoomIn)
        zoomOut = view.findViewById(R.id.btn_zoomOut)
        zoomIn.setOnClickListener(this)
        zoomOut.setOnClickListener(this)
        levelPicker = view.findViewById(R.id.picker)
        mapView = view.findViewById(R.id.mapView) ?: return
        configureLevelPicker(levelPicker)
        if(confMap)configureMapView(mapView)
    }

    /**
     * Метод для настройки [mapHelper]
     * @See [MapHelper]
     * @Param [mapView] view для настройки
     * @Param [scale] уровень приближения карты
     * @Param [rotation] угол поворота карты
     * @Param [level] номер отображаемого на карте этажа
     * */
    private fun configureMapView(mapView: MapView, scale: Float = 0f, rotation: Float = 0f, level: Int = 1){
        mapHelper = MapHelper(requireContext(), mapView, level,levelCount,locationName, width, height, navigation)
        mapHelper.rotation = rotation
        mapHelper.setScale(scale)
        mapHelper.addAllMarkers(dotList)
        mapHelper.addPositionMarker(dotList[0].getX().toDouble(), dotList[0].getY().toDouble())
        mapHelper.addReferentialListener()
        mapHelper.addMarkerClickListener()
        putData(0, 136)
        mapHelper.updatePath()
    }

    /**
     * Получает [dots], [width], [height] и [levelCount] из json строки
     * @Param [json] - строка в формате json с графом
     */
    private fun loadFromString(json: String) {
        levelCount = fileHelper.getLevelCount("tiles1")-1
        navigation.loadMapFromJson(json)
        val map = JSONTokener(json).nextValue() as JSONObject
        val jsonDots = map.getJSONArray("dots")
        width = map.getInt("width")
        height = map.getInt("height")
        var i = -1
        while (++i < jsonDots.length()) {
            val jsonDot = jsonDots.getJSONObject(i)
            val dot = Map.Dot(jsonDot.getDouble("x").toFloat(), jsonDot.getDouble("y").toFloat())
            dot.setId(jsonDot.getInt("id"))
            dot.setConnected(jsonDot.getJSONArray("connected"))
            dot.setLevel(jsonDot.getInt("level"))
            if(!levelArray.contains(dot.getLevel().toString())){
                levelArray.add(dot.getLevel().toString())
            }
            dotList.add(dot)
        }
        levelArray.sort()
        Log.d("LevelArray", levelArray.toString())
    }

    /**
     * Метод для обработки обновления значения [levelPicker]
     * @See [MapFragment.configureViews]
     * @See [MapFragment.configureMapView]
     * */
    @SuppressLint("ResourceAsColor", "SoonBlockedPrivateApi")
    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        try {
            val level = levelArray[picker?.value!! -1].toInt()
            if (oldVal != newVal) {
                updateViews()
                configureViews(parentView, false)
                val scale = mapHelper.getScale()
                val rotation = mapHelper.rotation
                configureMapView(mapView, scale, rotation, level)
            }
        } catch (e: Exception) {
            Log.d("MyLog", e.message.toString())
        }
    }

    private fun updateViews() {
        parentView.removeAllViewsInLayout()
        mapView = MapView(requireContext())
        parentView.addView(mapView)
        parentView.addView(levelPicker)
        parentView.addView(zoomIn)
        parentView.addView(zoomOut)
    }

    private fun putData(start: Int, finish: Int){
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences("path", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt("start", start)
        editor.putInt("finish", finish)
        editor.apply()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_zoomIn -> mapHelper.zoomIn()
            R.id.btn_zoomOut -> mapHelper.zoomOut()
        }
    }
}