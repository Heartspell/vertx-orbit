package com.heartspell.vertxorbit.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
@State(name = "VertxOrbitSettings", storages = [Storage("vertxOrbit.xml")])
class VertxOrbitSettings : PersistentStateComponent<VertxOrbitSettings.State> {
    private var settingsState = State()

    data class State(
        var language: String = OrbitLanguage.ENGLISH.name,
        var displayMode: String = DisplayMode.FULL.name
    )

    enum class OrbitLanguage(val label: String) {
        ENGLISH("English"),
        RUSSIAN("Русский");

        override fun toString(): String = label
    }

    enum class DisplayMode(val label: String) {
        FULL("Full"),
        TINY("Tiny"),
        TRACE("Trace");

        override fun toString(): String = label
    }

    var language: OrbitLanguage
        get() = runCatching { OrbitLanguage.valueOf(settingsState.language) }.getOrDefault(OrbitLanguage.ENGLISH)
        set(value) {
            settingsState.language = value.name
        }

    var displayMode: DisplayMode
        get() = runCatching { DisplayMode.valueOf(settingsState.displayMode) }.getOrDefault(DisplayMode.FULL)
        set(value) {
            settingsState.displayMode = value.name
        }

    override fun getState(): State = settingsState

    override fun loadState(state: State) {
        settingsState = state
    }

    companion object {
        fun getInstance(): VertxOrbitSettings = service()
    }
}
