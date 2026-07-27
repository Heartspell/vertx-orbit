package com.heartspell.vertxorbit.settings

import com.intellij.util.messages.Topic

fun interface OrbitSettingsListener {
    fun settingsChanged()

    companion object {
        val TOPIC: Topic<OrbitSettingsListener> = Topic.create(
            "Vert.x Orbit settings changed",
            OrbitSettingsListener::class.java
        )
    }
}
