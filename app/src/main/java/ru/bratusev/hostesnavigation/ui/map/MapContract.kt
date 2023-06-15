package ru.bratusev.hostesnavigation.ui.map

import ru.bratusev.hostesnavigation.ui.base.BasePresenter
import ru.bratusev.hostesnavigation.ui.base.BaseView

interface MapContract {
    interface Presenter : BasePresenter {
        fun loadMap(json: String)
    }

    interface View : BaseView<Presenter> {

    }
}