/**
 * Класс для работы с MapFragment
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package ru.bratusev.hostesnavigation.ui.map

import android.os.Bundle
import android.util.Log
import android.util.Log.i
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import ru.bratusev.hostesnavigation.R
import java.io.File


/**
 * Класс для работы с MapFragment
 * @Constructor Создаёт пустой MapFragment
 */
class MapFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false).also {
            val mapConnector = MapConnector(requireContext(), it as ViewGroup, "location1")
            var i = 27
            mapConnector.updatePath(i,15)
            it.findViewById<ImageView>(R.id.btn_zoomIn).setOnClickListener {
                i++
                mapConnector.updatePath(i,15)
            }


            /*val fileHelper = FileHelper(requireContext(), "location1")
            fileHelper.fileDownload("1rq4aFmBEvLCAhXTQ3YPbtaHkoc2_8B8v")*/
        }
    }
}