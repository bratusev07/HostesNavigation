package ru.bratusev.hostesnavigation.ui.map

import android.os.Environment
import ru.bratusev.hostesnavigation.navigation.Map

object MapConstants {
    /**
     * @Param [tileLevel] номер отображаемого этажа
     * @Param [levelCount] количество уровней приближения
     * @Param [mapWidth] полная ширина карты в пикселях
     * @Param [mapHeight] полная высота карты в пикселях
     * @Param [widthMax] – максимальная ширина отображаемого маршрута
     * @Param [widthMin] – минимальная ширина отображаемого маршрута
     * @Param [maxScale] – максимальное приближение карты
     * @Param [minScale] – минимальное приближение карты
     * @Param [rotation] – угол поворота карты
     * @Param [levelArray] массив этажей в здании
     * */
    var mapHeight = 0
    var mapWidth = 0
    internal var maxPathWidth = 50f
    internal var minPathWidth = 10f
    var zoomLevelCount = 0
    internal var levelNumber = 1
    var levelArray = ArrayList<String>()
    internal val minScale = 0f
    internal val maxScale = 2f
    internal val markerList = ArrayList<MapMarker>()
    internal var rotation = 0F
    var dotList: ArrayList<Map.Dot> = ArrayList()

    internal var startNode = 0
    internal var finishNode = 0

    /**
     * @Param [SDPath] путь к файлам проекта
     * @Param [dataPath] путь для скачивания архива
     * @Param [unzipPath] путь для разархивации
     * */
    private val SDPath =
        Environment.getExternalStorageDirectory().absolutePath + "/Android/data/ru.bratusev.hostesnavigation"
    internal val dataPath = "$SDPath/files/locations/"
    internal val unzipPath = "$SDPath/files/locations/"
}