package com.example.immersivexiangqi.p20261105

import com.example.immersivexiangqi.p20261105.content.HomeStage
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.dsl.Stage
import com.pico.spatial.ui.foundation.dsl.SpatialAppScope

fun mainApp(scope: SpatialAppScope) =
    with(scope) {
        Stage(id = "XiangqiStage") {
            PicoTheme {
                HomeStage()
            }
        }
    }

