/**
 * Структура данных маркера для карты
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package ru.bratusev.hostesnavigation.ui.map

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView

/**
 * Структура данных для создания метки для карты
 * @See [MapHelper]
 *
 * @Param [x] X координата метки на карте
 * @Param [y] Y координата метки на карте
 * @Param [name] название метки или идентификатор
 * @Constructor создаёт маркер для карты по шаблону
 *
 * @Param context контекст для работы с ресурсами
 */
class MapMarker(context: Context, val x: Double, val y: Double, val name: String) :
    AppCompatImageView(context)