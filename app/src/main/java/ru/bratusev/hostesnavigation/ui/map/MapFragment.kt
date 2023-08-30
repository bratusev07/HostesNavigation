/**
 * Класс для работы с MapFragment
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package ru.bratusev.hostesnavigation.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import ru.bratusev.hostesnavigation.R
import ru.bratusev.hostesnavigation.ui.map.MapConstants.startNode


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
            //val fileHelper = FileHelper(requireContext(), "location1")
            //fileHelper.fileDownload("1rq4aFmBEvLCAhXTQ3YPbtaHkoc2_8B8v")
            val mapConnector = MapConnector(requireContext(), it as ViewGroup, "location1")
            it.findViewById<ImageButton>(R.id.btn_zoomIn).setOnClickListener {
                startNode++
                mapConnector.updatePath(136)
            }
        }
    }
}