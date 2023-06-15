package ru.bratusev.hostesnavigation

import android.os.Build
import android.os.Bundle
import android.window.SplashScreen
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

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