package com.puutaro.commandclick.fragment_lib.edit_fragment.processor.edit_text_support_view

import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.puutaro.commandclick.fragment.EditFragment
import com.puutaro.commandclick.fragment_lib.edit_fragment.processor.edit_text_support_view.lib.ButtonViewProducer
import com.puutaro.commandclick.fragment_lib.edit_fragment.processor.edit_text_support_view.lib.EditableSpinnerViewProducer
import com.puutaro.commandclick.view_model.activity.TerminalViewModel

class WithEditableSpinnerWithButtonView(
    private val editFragment: EditFragment,
    private val readSharePreffernceMap: Map<String, String>,
) {
    private val context = editFragment.context
    val terminalViewModel: TerminalViewModel by editFragment.activityViewModels()

    fun create(
        currentId: Int,
        currentVariableValue: String?,
        insertTextView: TextView,
        insertEditText: EditText,
        currentRecordNumToSetVariableMap: Map<String,String>,
    ): LinearLayout {
        val horizontalLinearLayout = LinearLayout(context)
        horizontalLinearLayout.orientation = LinearLayout.HORIZONTAL
        val linearParamsForEditTextTest = LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        insertEditText.inputType = InputType.TYPE_CLASS_TEXT
        insertEditText.setText(currentVariableValue)
        linearParamsForEditTextTest.weight = 0.5F
        insertEditText.layoutParams = linearParamsForEditTextTest
        horizontalLinearLayout.addView(insertEditText)
        val insertSpinner = EditableSpinnerViewProducer.make(
            context,
            currentId,
            insertEditText,
            currentRecordNumToSetVariableMap,
            0.3F,
        )
        horizontalLinearLayout.addView(insertSpinner)
        val insertButton = ButtonViewProducer.make(
            editFragment,
            readSharePreffernceMap,
            currentId,
            insertTextView,
            insertEditText,
            currentRecordNumToSetVariableMap,
            0.2F,
            true
        )
        horizontalLinearLayout.addView(insertButton)
        return horizontalLinearLayout
    }
}