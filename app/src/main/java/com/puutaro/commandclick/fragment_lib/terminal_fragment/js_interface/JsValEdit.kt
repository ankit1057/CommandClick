package com.puutaro.commandclick.fragment_lib.terminal_fragment.js_interface

import android.webkit.JavascriptInterface
import android.widget.Toast
import com.puutaro.commandclick.fragment.TerminalFragment
import com.puutaro.commandclick.util.CmdClickMap
import com.puutaro.commandclick.util.FileSystems
import com.puutaro.commandclick.util.LogSystems
import com.puutaro.commandclick.util.ReadText
import java.io.File

class JsValEdit(
    private val terminalFragment: TerminalFragment
) {

    private val context = terminalFragment.context
    private val activity = terminalFragment.activity


    @JavascriptInterface
    fun editAndSaveCmdVar(
        title: String,
        fContents: String,
        setVariableTypes: String,
        targetVariables: String,
    ) {
        try {
            execEditAndSaveCmdVar(
                title,
                fContents,
                setVariableTypes,
                targetVariables,
            )
        } catch (e: Exception){
            Toast.makeText(
                context,
                "$e",
                Toast.LENGTH_SHORT
            ).show()
            LogSystems.stdErr("$e")
        }
    }


    private fun execEditAndSaveCmdVar(
        title: String,
        fannelPath: String,
        setVariableTypes: String,
        targetVariables: String,
    ) {
        val fannelPathObj = File(fannelPath)
        val parentDirPath = fannelPathObj.parent
            ?: return
        val fannelName = fannelPathObj.name
        val resultKeyValueConSrc = JsDialog(terminalFragment).formDialog(
            title,
            setVariableTypes,
            targetVariables,
        )
        if(
            resultKeyValueConSrc.isEmpty()
        ) return
        val resultKeyValueCon =
            resultKeyValueConSrc.replace(
                "\n",
                "\t"
            )

        val variableMap = CmdClickMap.createMap(
            resultKeyValueCon,
            "\t"
        )
        val jsEdit = JsEdit(terminalFragment)
        variableMap.forEach {
            val varName = it.first
            val varValue = it.second
            jsEdit.updateEditText(
                varName,
                varValue
            )
        }
        val jsScript = JsScript(terminalFragment)
        val fcon = ReadText(
            parentDirPath,
            fannelName
        ).readText()
        val replacedCon =  jsScript.replaceCommandVariable(
            fcon,
            resultKeyValueCon
        )
        if(
            replacedCon.isEmpty()
        ) return
        FileSystems.writeFile(
            parentDirPath,
            fannelName,
            replacedCon
        )
    }
}