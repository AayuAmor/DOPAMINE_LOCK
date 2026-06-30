package com.teamdobermans.dopamine_lock.model

enum class PomodoroPresetType {
    STANDARD,
    DEEP_WORK,
    ULTRADIAN,
    CUSTOM
}

data class FocusPreferences(
    val presetType: PomodoroPresetType = PomodoroPresetType.STANDARD,
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val autoStartBreak: Boolean = false,
    val autoStartNextSession: Boolean = false
)
