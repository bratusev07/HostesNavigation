/**
 * Класс для работы с MainActivity
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package ru.bratusev.hostesnavigation

import android.os.Build
import android.os.Bundle
import android.window.SplashScreen
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

/**
 * Основное активити, на котором размещены все фрагменты
 *
 * @Constructor Создаёт пустой основной экран
 */
@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : AppCompatActivity(), SplashScreen {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun setOnExitAnimationListener(listener: SplashScreen.OnExitAnimationListener) {}

    override fun clearOnExitAnimationListener() {}

    override fun setSplashScreenTheme(themeId: Int) {}
}