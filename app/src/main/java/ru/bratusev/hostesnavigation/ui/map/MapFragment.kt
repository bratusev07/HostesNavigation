package ru.bratusev.hostesnavigation.ui.map

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.json.JSONObject
import org.json.JSONTokener
import ovh.plrapps.mapview.MapView
import ru.bratusev.hostesnavigation.R
import ru.bratusev.hostesnavigation.navigation.Map
import ru.bratusev.hostesnavigation.ui.views.LevelBar

class MapFragment : Fragment(), View.OnTouchListener {
    private lateinit var parentView: ViewGroup
    private lateinit var mapView: MapView
    private lateinit var mapHelper: MapHelper
    private lateinit var levelBar: LevelBar

    private var dotList: ArrayList<Map.Dot> = ArrayList()
    private var width = 0
    private var height = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false).also {
            levelBar = it.findViewById<LevelBar>(R.id.levelBar)
            levelBar.setLevelRange(1,2)
            levelBar.setOnTouchListener(this)

            parentView = it as ViewGroup
            val json = requireActivity().assets?.open("map.json")?.reader().use { it?.readText() }
            if (json != null) {
                loadFromString(json)
            }else Log.d("MyLog", "json is null")
            configureMapView(it)
        }
    }

    private fun configureMapView(view: View) {
        mapView = view.findViewById(R.id.mapView) ?: return
        mapHelper = MapHelper(requireContext(), mapView, 1)

        try {
            for (dot in dotList) {
                mapHelper.addDefaultMarker(
                    dot.getX().toDouble(),
                    dot.getY().toDouble(),
                    dot.getId().toString()
                )
            }
        }catch (e: Exception){
            Log.d("MyLog", e.message.toString())
        }

        mapHelper.addPositionMarker(0.7, 0.6)
        mapHelper.addReferentialListener()
        mapHelper.addMarkerClickListener()
    }

    fun loadFromString(json: String) {
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

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                try {
                    val level = levelBar.getLevel()
                    parentView.removeView(mapView)
                    parentView.removeView(levelBar)
                    mapView = MapView(requireContext())
                    parentView.addView(mapView)
                    parentView.addView(levelBar)

                    val scale = mapHelper.getScale()
                    val rotation = mapHelper.rotation
                    mapHelper = MapHelper(requireContext(), mapView, level)
                    mapHelper.setScale(scale)
                    mapHelper.rotation = rotation
                }catch (e: Exception){
                    Log.d("MyLog", e.message.toString())
                }
                return true
            }
        }
        return v!!.onTouchEvent(event)
    }
}