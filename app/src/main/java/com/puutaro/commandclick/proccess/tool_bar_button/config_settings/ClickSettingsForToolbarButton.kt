package com.puutaro.commandclick.proccess.tool_bar_button.config_settings

object ClickSettingsForToolbarButton {
    enum class ClickConfigMapKey(
        val str: String
    ){
        JS_PATH("jsPath"),
        MENU_PATH("menuPath"),
        ON_HIDE_FOOTER("onHideFooter"),
        ON_SCRIPT_SAVE("onScriptSave"),
        MONITOR_SIZE("monitorSize"),
    }

    enum class MonitorSize {
        SHORT,
        LONG,
    }

    enum class OnScriptSave {
        ON,
        OFF
    }
}