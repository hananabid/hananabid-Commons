package com.hananabid.commons.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hananabid.commons.extensions.baseConfig
import com.hananabid.commons.extensions.getSharedTheme
import com.hananabid.commons.extensions.isThankYouInstalled

abstract class BaseSplashActivity : AppCompatActivity() {

    abstract fun initActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ((baseConfig.appRunCount == 0 || !baseConfig.wasSharedThemeAfterUpdateChecked) && isThankYouInstalled()) {
            baseConfig.wasSharedThemeAfterUpdateChecked = false
            getSharedTheme {
                if (it != null) {
                    baseConfig.apply {
                        wasSharedThemeForced = false
                        isUsingSharedTheme = false
                        wasSharedThemeEverActivated = false

                        textColor = it.textColor
                        backgroundColor = it.backgroundColor
                        primaryColor = it.primaryColor
                        appIconColor = it.appIconColor
                    }
                }
                initActivity()
            }
        } else {
            initActivity()
        }
    }
}
