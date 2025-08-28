package com.mindblowers.leasehub.data.repository

import com.mindblowers.leasehub.data.prefs.AppPrefs
import com.mindblowers.leasehub.data.prefs.ThemeOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepo @Inject constructor(
    private val prefs: AppPrefs
) {
    private val _themeOption = MutableStateFlow(prefs.getTheme())
    val themeOption: StateFlow<ThemeOption> = _themeOption

    private val _dynamicColor = MutableStateFlow(prefs.getDynamicColor())
    val dynamicColor: StateFlow<Boolean> = _dynamicColor

    fun setTheme(option: ThemeOption) {
        prefs.setTheme(option)
        _themeOption.value = option
    }

    fun setDynamicColor(enabled: Boolean) {
        prefs.setDynamicColor(enabled)
        _dynamicColor.value = enabled
    }
}
