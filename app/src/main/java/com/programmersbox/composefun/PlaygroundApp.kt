package com.programmersbox.composefun

import android.app.Application
import com.thelumierguy.crashwatcher.CrashWatcher

class PlaygroundApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashWatcher.initCrashWatcher(this)
    }
}