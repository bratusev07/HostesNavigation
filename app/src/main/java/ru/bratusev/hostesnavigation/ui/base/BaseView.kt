package ru.bratusev.hostesnavigation.ui.base

interface BaseView<T> {
    fun setPresenter(presenter: T)
}