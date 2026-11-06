package com.example.immersivexiangqi.p20261105.platform

import android.app.Application
import com.pico.spatial.ui.foundation.dsl.launch
import com.example.immersivexiangqi.p20261105.mainApp

class SpatialApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        launch(::mainApp)
    }
}
