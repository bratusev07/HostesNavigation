package ru.bratusev.hostesnavigation.navigation

import kotlin.math.sqrt

class Navigation {

    private var map: Map = Map()
    private var pathCash: PathCash?

    init {
        pathCash = null
    }

    fun getDot(index: Int): Map.Dot {
        return map.getDot(index)
    }

    fun dist(start: Int, finish: Int): Float {
        val nodeArray = path(start, finish)
        var res = 0f
        var px = nodeArray!![0]
        var py = nodeArray[1]
        for (i in 2 until nodeArray.size step 2) {
            res += sqrt((nodeArray[i] - px) * (nodeArray[i] - px) + (nodeArray[i + 1] - py) * (nodeArray[i + 1] - py))
            px = nodeArray[i]
            py = nodeArray[i + 1]
        }
        return res
    }

    fun path(start: Int, finish: Int): FloatArray? {
        if (pathCash?.from == start && pathCash?.to == finish) return pathCash?.path
        val que = ArrayList<Map.Dot>()
        map.getDot(start).setG(0f)
        map.getDot(start).setH(map.dist(start, finish))

        que.add(map.getDot(start))

        while (que.isNotEmpty()) {
            que.sortBy { it.getF() }
            val x: Map.Dot = que[0]
            que.removeAt(0)
            if (x.getId() == finish) {
                return reconstructPath(x.getId())
            }

            x.setVisited(true)
            for (y in x.getConnected()) {
                if (map.getDot(y).isVisited()) continue
                var isTentativeBetter = false
                val tentativeGScore = x.getG() + map.dist(x.getId(), y)
                if (!que.contains(map.getDot(y))) isTentativeBetter = true
                else if (tentativeGScore < map.getDot(y).getG()) isTentativeBetter = true

                if (isTentativeBetter) {
                    map.getDot(y).setG(tentativeGScore)
                    map.getDot(y).setH(map.dist(y, finish))
                    map.getDot(y).setFromId(x.getId())
                    que.add(map.getDot(y))
                }
            }
        }
        map.clear()
        return null
    }

    private fun reconstructPath(finish: Int): FloatArray {
        val path = ArrayList<Int>()
        path.add(finish)
        var from = map.getDot(finish).getFromId()
        while (from != -1) {
            path.add(from)
            from = map.getDot(from).getFromId()
        }
        path.reverse()
        var init = true
        val size = path.size * 4 - 4
        var i = 0
        var dot = 0
        val lines = FloatArray(size)
        while (i < size) {
            if (init) {
                lines[i] = map.getDot(path[dot]).getX()
                lines[i + 1] = map.getDot(path[dot]).getY()
                init = false
                i += 2
                dot++
            } else {
                lines[i] = map.getDot(path[dot]).getX()
                lines[i + 1] = map.getDot(path[dot]).getY()
                if (i + 2 >= size) break
                lines[i + 2] = lines[i]
                lines[i + 3] = lines[i + 1]
                i += 4
                dot++
            }
        }

        pathCash = object : PathCash() {
            override var path = lines
            override var from = path[0]
            override var to = path[path.size - 1]
        }
        map.clear()
        return lines
    }

    fun loadMapFromJson(json: String) {
        map.loadFromString(json)
    }

    abstract class PathCash {
        abstract var path: FloatArray
        abstract var from: Int
        abstract var to: Int
    }
}