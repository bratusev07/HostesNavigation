package ru.bratusev.hostesnavigation.ui.map

import ru.bratusev.hostesnavigation.navigation.Navigation

class MapPresenter(view: MapContract.View) : MapContract.Presenter {
    private var view: MapContract.View? = view
    private var nav : Navigation = Navigation()

    override fun loadMap(json: String){
        nav.loadMapFromJson(json)
    }

    override fun onDestroy() {
        this.view = null
    }
}