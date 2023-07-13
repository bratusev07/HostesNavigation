/**
 * Класс для базовой работы с картой
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package ru.bratusev.hostesnavigation.navigation

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import kotlin.math.sqrt

/**
 * Класс Map, содержащий базовые методы для работы с картой
 *
 * @constructor Создаёт объект типа Map без параметров
 */
class Map {
    /**
     * @Param [dots] список точек на графе
     * @See [Dot]
     * */
    private val dots = ArrayList<Dot>()
    /** @Param width ширина карты в пикселях*/
    private var width = 0
    /** @Param height длинна карты в пикселях*/
    private var height = 0

    /**
     * Получает [dots], [width] и [height] из json строки
     * @Param [json] - строка в формате json с графом
     */
    fun loadFromString(json: String) {
        val map = JSONTokener(json).nextValue() as JSONObject
        val jsonDots = map.getJSONArray("dots")
        width = map.getInt("width")
        height = map.getInt("height")
        var i = -1
        while (++i < jsonDots.length()) {
            val jsonDot = jsonDots.getJSONObject(i)
            val dot = Dot(jsonDot.getDouble("x").toFloat(), jsonDot.getDouble("y").toFloat())
            dot.setLevel(jsonDot.getInt("level"))
            dot.setId(jsonDot.getInt("id"))
            dot.setConnected(jsonDot.getJSONArray("connected"))
            dots.add(dot)
        }
    }

    /**
     * Возвращает точку на графе по [id]
     * @See [Dot]
     * @Param [id]
     * @Return возвращает точку на графе
     */
    fun getDot(id: Int): Dot {
        return dots[id]
    }

    /**
     * Считает расстояние от точки до точки на графе
     *
     * @Param [dot1] идентификатор первой точки
     * @Param [dot2] идентификатор второй точки
     * @Return расстояние между точками
     */
    fun dist(dot1: Int, dot2: Int): Float {
        if (dots.isEmpty()) return -1f
        if (dot1 < 0 || dot2 < 0) return -1f
        if (dot1 > dots.size || dot2 > dots.size) return -1f
        val dX1 = width * dots[dot1].getX()
        val dX2 = width * dots[dot2].getX()
        val dY1 = height * dots[dot1].getY()
        val dY2 = height * dots[dot2].getY()

        return sqrt((dX1 - dX2) * (dX1 - dX2) + (dY1 - dY2) * (dY1 - dY2))
    }

    /**
     * Сбрасывает состояние точек к исходному из списка [dots]
     */
    fun clear() {
        for (dot in dots) {
            dot.setVisited(false)
            dot.setFromId(-1)
            dot.setG(0f)
            dot.setH(0f)
        }
    }

    /**
     * Стурктура данных "Точка"
     *
     * @Param [x] X координата точки
     * @property [y] Y координата точки
     * @Constructor Создаёт точку с координатами [x], [y]
     */
    class Dot(private var x: Float, private var y: Float) {
        /**
         * @Param [id] идентификатор точки
         * @Param [g]  стоимость пути от начальной вершины
         * @Param [h]  эвристическое приближение
         * @Param [visited] флаг для проверки посещения точки
         * @Param [level] этаж на котором расположена точка на графе
         * @Param [fromId] идентификатор исходной точки маршрута
         * @Param [nei] массив идентификаторов соседних точек для данной
         * */
        private var id = 0
        private var g = 0f
        private var h = 0f
        private var visited = false
        private var level = 1
        private var fromId = -1
        private var nei = ArrayList<Int>()

        /** Устанавливает [level] для текущей точки */
        fun setLevel(level: Int) {
            this.level = level
        }

        /** Возвращает значение [level] для текущей точки */
        fun getLevel(): Int{
            return level
        }

        /** Устанавливает [g] для текущей точки */
        fun setG(g: Float) {
            this.g = g
        }

        /** Возвращает значение [g] для текущей точки */
        fun getG(): Float {
            return g
        }

        /** Устанавливает [h] для текущей точки */
        fun setH(h: Float) {
            this.h = h
        }

        /** Устанавливает [fromId] для текущей точки */
        fun setFromId(id: Int) {
            fromId = id
        }

        /** Возвращает значение [fromId] для текущей точки */
        fun getFromId(): Int {
            return fromId
        }

        /** Устанавливает [visited] для текущей точки */
        fun setVisited(x: Boolean) {
            visited = x
        }

        /** Возвращает значение [visited] для текущей точки */
        fun isVisited(): Boolean {
            return visited
        }

        /** Устанавливает [id] для текущей точки */
        fun setId(id: Int) {
            this.id = id;
        }

        /** Возвращает значение [id] для текущей точки */
        fun getId(): Int {
            return id
        }

        /** Возвращает значение [x] для текущей точки */
        fun getX(): Float {
            return x
        }

        /** Возвращает значение [g] + [h] для текущей точки */
        fun getF(): Float {
            return g + h
        }

        /** Возвращает значение [y] для текущей точки */
        fun getY(): Float {
            return y
        }

        /** Устанавливает [nei] для текущей точки */
        fun setConnected(nei: JSONArray) {
            var i = -1
            while (++i < nei.length()) {
                this.nei.add(nei.getInt(i));
            }
        }

        /** Возвращает значение [nei] для текущей точки */
        fun getConnected(): ArrayList<Int> {
            return nei
        }
    }
}