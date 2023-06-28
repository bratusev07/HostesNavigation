package ru.bratusev.hostesnavigation.ui.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.core.view.size
import androidx.fragment.app.Fragment
import org.json.JSONObject
import org.json.JSONTokener
import ovh.plrapps.mapview.MapView
import ru.bratusev.hostesnavigation.R
import ru.bratusev.hostesnavigation.navigation.Map
import ru.bratusev.hostesnavigation.navigation.Navigation


class MapFragment : Fragment(), NumberPicker.OnValueChangeListener {
    private lateinit var parentView: ViewGroup
    private lateinit var mapView: MapView
    private lateinit var mapHelper: MapHelper
    private lateinit var levelPicker: NumberPicker
    private lateinit var navigation: Navigation

    private var dotList: ArrayList<Map.Dot> = ArrayList()
    private var width = 0
    private var height = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false).also {
            parentView = it as ViewGroup
            navigation = Navigation()
            val json = requireActivity().assets?.open("map.json")?.reader().use { it?.readText() }
            if (json != null) {
                loadFromString(json)
                navigation.loadMapFromJson(json)
            }
            configureMapView(it)
        }
    }

    private fun configureLevelPicker(view: View) {
        try {
            val str = arrayOf("1", "2")
            str.reverse()
            levelPicker = view.findViewById(R.id.picker)
            levelPicker.wrapSelectorWheel = false
            initPickerWithString(1, str.size, levelPicker, str)
            val delta = (levelPicker.minValue + levelPicker.value - str.size) % levelPicker.size
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

    private fun configureMapView(view: View) {
        configureLevelPicker(view)
        mapView = view.findViewById(R.id.mapView) ?: return
        mapHelper = MapHelper(requireContext(), mapView, levelPicker.value, navigation)

        mapHelper.addAllMarkers(dotList)
        mapHelper.addPositionMarker(0.7, 0.6)
        mapHelper.addReferentialListener()
        mapHelper.addMarkerClickListener()
        mapHelper.updatePath(0, 66)
    }

    private fun loadFromString(json: String) {
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
            dotList.add(dot)
        }
    }

    @SuppressLint("ResourceAsColor", "SoonBlockedPrivateApi")
    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        try {
            Log.d("MyLog", newVal.toString())
            if (oldVal != newVal) {
                parentView.removeView(mapView)
                parentView.removeView(levelPicker)
                mapView = MapView(requireContext())
                parentView.addView(mapView)
                configureMapView(mapView)
                parentView.addView(levelPicker)
                configureLevelPicker(parentView)
                val scale = mapHelper.getScale()
                val rotation = mapHelper.rotation
                mapHelper = MapHelper(requireContext(), mapView, newVal, navigation)
                mapHelper.addAllMarkers(dotList)
                mapHelper.setScale(scale)
                mapHelper.rotation = rotation
                mapHelper.updatePath(2, 43)
                mapHelper.addReferentialListener()
                mapHelper.addMarkerClickListener()
            }
        } catch (e: Exception) {
            Log.d("MyLog", e.message.toString())
        }
    }
}