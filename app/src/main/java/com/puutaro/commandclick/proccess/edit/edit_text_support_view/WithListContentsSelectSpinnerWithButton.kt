package com.puutaro.commandclick.proccess.edit.edit_text_support_view

import android.graphics.Color
import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.puutaro.commandclick.common.variable.edit.EditParameters
import com.puutaro.commandclick.fragment.EditFragment
import com.puutaro.commandclick.proccess.edit.edit_text_support_view.lib.ButtonViewProducer
import com.puutaro.commandclick.proccess.edit.edit_text_support_view.lib.ListContentsSelectSpinnerViewProducer
import com.puutaro.commandclick.view_model.activity.TerminalViewModel


class WithListContentsSelectSpinnerWithButton(
    private val editFragment: EditFragment,
) {
    val terminalViewModel: TerminalViewModel by editFragment.activityViewModels()

    fun create(
        insertTextView: TextView,
        insertEditText: EditText,
        editParameters: EditParameters,
    ): LinearLayout {
        val context = editParameters.context
        val currentVariableValue = editParameters.currentVariableValue
        val horizontalLinearLayout = LinearLayout(context)
        horizontalLinearLayout.orientation = LinearLayout.HORIZONTAL
        val linearParamsForEditTextTest = LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        insertEditText.inputType = InputType.TYPE_CLASS_TEXT
        insertEditText.setText(currentVariableValue)
        insertEditText.setTextColor(Color.parseColor("#FFFFFF"))
        linearParamsForEditTextTest.weight = 0.001F
        insertEditText.layoutParams = linearParamsForEditTextTest
        horizontalLinearLayout.addView(insertEditText)
        val insertSpinner =  ListContentsSelectSpinnerViewProducer.make(
            insertEditText,
            editParameters,
            0.8F,
        )
        horizontalLinearLayout.addView(insertSpinner)
        val insertButton = ButtonViewProducer.make(
            editFragment,
            insertTextView,
            insertEditText,
            editParameters,
            0.2F,
            true
        )
        horizontalLinearLayout.addView(insertButton)
        return horizontalLinearLayout
    }
}