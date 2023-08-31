/**
 * Класс для работы с MainFragment
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package ru.bratusev.hostesnavigation.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import ru.bratusev.hostesnavigation.R
import ru.bratusev.hostesnavigation.navigation.Navigation
import ru.bratusev.hostesnavigation.ui.map.FileHelper

/**
 * Реализуется как стартовая точка навигации по проекту
 *
 * @Constructor Создаёт пустой меин фрагмент
 */
class MainFragment : Fragment(R.layout.fragment_main) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false).also{

            val fileHelper = FileHelper(requireContext(), "Korpus_G")
            //fileHelper.fileDownload("1rq4aFmBEvLCAhXTQ3YPbtaHkoc2_8B8v")
            fileHelper.fileDownload("19e-oKDYTncxJn3cL34IYkYW5QKoxJguK")
            it.findViewById<Button>(R.id.downloadBtn).setOnClickListener{download()}
        }
    }

    private fun download(){
        findNavController().navigate(R.id.action_mainFragment_to_mapFragment)
    }
}