package com.puutaro.commandclick.util.state

import com.puutaro.commandclick.common.variable.path.UsePath
import com.puutaro.commandclick.common.variable.variables.CommandClickScriptVariable
import com.puutaro.commandclick.proccess.edit.lib.SetReplaceVariabler
import com.puutaro.commandclick.util.CommandClickVariables
import com.puutaro.commandclick.util.file.FileSystems
import com.puutaro.commandclick.util.file.ReadText
import java.io.File

object SettingVariableImportManager {
    fun import(
        settingVariableList: List<String>?,
        currentAppDirPath: String,
        currentFannelName: String,
        setReplaceVariableMap: Map<String, String>?,
        settingSectionStart: String,
        settingSectionEnd: String,
    ): List<String>? {
        val importedSettingVariables = settingVariableList?.map {
            val settingImportConList =
                CommandClickVariables.substituteCmdClickVariableList(
                    listOf(it),
                    CommandClickScriptVariable.SETTING_IMPORT
                )
            when(
                settingImportConList.isNullOrEmpty()
            ) {
                false -> settingImportConList.map {
                    CommandClickVariables.substituteVariableListFromHolder(
                        ReadText(it).textToList(),
                        settingSectionStart,
                        settingSectionEnd
                    ) ?: emptyList()
                }.flatten().joinToString("\n")
                else -> it
            }
        }?.joinToString("\n")?.split("\n")
        val filterImportedSettingVariables = importedSettingVariables?.filter {
            val trimLine = it.trim()
            trimLine.isNotEmpty()
                    && !it.startsWith("//")
        }?.joinToString("\n")?.let {
            SetReplaceVariabler.execReplaceByReplaceVariables(
                it,
                setReplaceVariableMap,
                currentAppDirPath,
                currentFannelName
            )
        }?.split("\n")
//        FileSystems.writeFile(
//            File(UsePath.cmdclickDefaultAppDirPath, "edits_settinfImport.txt").absolutePath,
//            listOf(
//                "settingVariableListSrc: ${settingVariableList?.joinToString("\n")}",
//                "settingVariableList: ${importedSettingVariables}",
//                "settingVariableListFilter: ${filterImportedSettingVariables}",
//            ).joinToString("\n\n")
//        )
        return filterImportedSettingVariables
    }
}