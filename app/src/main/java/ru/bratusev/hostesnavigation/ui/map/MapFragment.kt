package ru.bratusev.hostesnavigation.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import ovh.plrapps.mapview.MapView
import ovh.plrapps.mapview.MapViewConfiguration
import ovh.plrapps.mapview.ReferentialData
import ovh.plrapps.mapview.ReferentialListener
import ovh.plrapps.mapview.api.addMarker
import ovh.plrapps.mapview.core.TileStreamProvider
import ovh.plrapps.mapview.util.AngleDegree
import ru.bratusev.hostesnavigation.R

class MapFragment : Fragment() {
    private lateinit var parentView: ViewGroup
    private lateinit var positionMarker: AppCompatImageView
    private lateinit var mapView: MapView
    private lateinit var mapHelper: MapHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false).also {
            parentView = it as ViewGroup
            configureMapView(it)
        }
    }

    private fun configureMapView(view: View) {
        mapHelper = MapHelper(requireContext())
        mapView = view.findViewById(R.id.mapView) ?: return
        mapView.configure(mapHelper.generateConfig())
        mapView.defineBounds(0.0, 0.0, 1.0, 1.0)

        mapHelper.addDefaultMarker(mapView,0.446, 0.49, "marker #1")
        mapHelper.addDefaultMarker(mapView,0.37, 0.3, "marker #2")
        mapHelper.addDefaultMarker(mapView,0.62, 0.32, "marker #3")

        mapHelper.addPositionMarker(mapView, 0.7, 0.6)
        mapView.addReferentialListener(mapHelper.refOwner)
    }
}