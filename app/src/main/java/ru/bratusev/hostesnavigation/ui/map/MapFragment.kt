package ru.bratusev.hostesnavigation.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ovh.plrapps.mapview.MapView
import ru.bratusev.hostesnavigation.R

class MapFragment : Fragment() {
    private lateinit var parentView: ViewGroup
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
        mapView = view.findViewById(R.id.mapView) ?: return
        mapHelper = MapHelper(requireContext(), mapView)

        mapHelper.addDefaultMarker(0.446, 0.49, "Г-320")
        mapHelper.addDefaultMarker(0.37, 0.305, "Г-317")
        mapHelper.addDefaultMarker(0.62, 0.31, "Г-326")

        mapHelper.addPositionMarker( 0.7, 0.6)
        mapHelper.addReferentialListener()
        mapHelper.addMarkerClickListener()
    }
}