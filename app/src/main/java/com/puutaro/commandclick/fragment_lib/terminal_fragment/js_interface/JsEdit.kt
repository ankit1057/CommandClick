package com.puutaro.commandclick.fragment_lib.terminal_fragment.js_interface

import android.webkit.JavascriptInterface
import androidx.fragment.app.activityViewModels
import com.puutaro.commandclick.fragment.TerminalFragment
import com.puutaro.commandclick.view_model.activity.EditViewModel

class JsEdit(
    private val terminalFragment: TerminalFragment
) {
    private val context = terminalFragment.context
    val editViewModel: EditViewModel by terminalFragment.activityViewModels()

    @JavascriptInterface
    fun updateEditText(
        updateVariableName: String,
        updateVariableValue: String
    ){
        val listener = context as? TerminalFragment.OnEditTextUpdateListenerForTermFragment
        val editTextId = editViewModel.variableNameToEditTextIdMap.get(updateVariableName)
        listener?.onEditTextUpdateForTermFragment(
            editTextId,
            updateVariableValue
        )
    }
}