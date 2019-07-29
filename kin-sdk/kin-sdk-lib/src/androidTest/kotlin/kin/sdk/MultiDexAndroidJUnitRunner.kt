package kin.sdk

import android.os.Bundle
import androidx.multidex.MultiDex
import androidx.test.runner.AndroidJUnitRunner


class MultiDexAndroidJUnitRunner : AndroidJUnitRunner() {
    override fun onCreate(arguments: Bundle) {
        MultiDex.install(targetContext)

        super.onCreate(arguments)
    }
}
