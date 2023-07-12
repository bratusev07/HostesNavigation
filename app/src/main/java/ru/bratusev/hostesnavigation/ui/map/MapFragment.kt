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


class MapFragment : Fragment(), NumberPicker.OnValueChangeListener, OnClickListener{
    private lateinit var parentView: ViewGroup
    private lateinit var mapView: MapView
    private lateinit var mapHelper: MapHelper
    private lateinit var levelPicker: NumberPicker
    private lateinit var zoomIn: ImageButton
    private lateinit var zoomOut: ImageButton
    private lateinit var navigation: Navigation

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

    private fun initPickerWithString(min: Int, max: Int, p: NumberPicker, str: Array<String>) {
        p.minValue = min
        p.maxValue = max
        p.displayedValues = str
    }

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