package com.puutaro.commandclick.proccess.edit.edit_text_support_view

import android.content.Context
import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.puutaro.commandclick.common.variable.edit.EditParameters
import com.puutaro.commandclick.common.variable.edit.EditTextSupportViewName
import com.puutaro.commandclick.common.variable.edit.TypeVariable
import com.puutaro.commandclick.proccess.edit.edit_text_support_view.lib.EditableListContentsSelectSpinnerViewProducer
import com.puutaro.commandclick.proccess.edit.edit_text_support_view.lib.EditableSpinnerViewProducer
import com.puutaro.commandclick.proccess.edit.edit_text_support_view.lib.FileSelectSpinnerViewProducer
import com.puutaro.commandclick.proccess.edit.edit_text_support_view.lib.InDeCrementerViewProducer
import com.puutaro.commandclick.proccess.edit.edit_text_support_view.lib.ListContentsSelectSpinnerViewProducer
import com.puutaro.commandclick.proccess.edit.edit_text_support_view.lib.SpinnerViewProducer
import com.puutaro.commandclick.proccess.edit.edit_text_support_view.lib.VariableLabelAdder

class WithEditComponentForListIndex {
    private val textAndLabelList = TypeVariable.textAndLabelList
    private val noIndexTypeList = TypeVariable.noIndexTypeList

    fun insert(
        insertTextView: TextView,
        editParameters: EditParameters,
    ): LinearLayout {
        val context = editParameters.context
        val textLabelIndex = culcTextLabelMarkIndex(
            editParameters,
            textAndLabelList
        )
        VariableLabelAdder.add(
            insertTextView,
            editParameters,
            textLabelIndex
        )
        val variableTypeList = updateVariableTypeListByLabel(
            editParameters,
            textLabelIndex
        )
        editParameters.variableTypeList = variableTypeList
        editParameters.setVariableMap = updateSetVariableMapByLabelIndex(
            editParameters,
            textLabelIndex,
        )
        val editTextWeight = decideTextEditWeight(
            variableTypeList,
        )
        val otherComponentWeight = decideOtherComponentWeight(
            editTextWeight,
            variableTypeList,
        )
        val insertEditText = initEditText(
            editParameters,
            editTextWeight
        )
        val horizontalLinearLayout = makeHorizontalLayout(context)
        horizontalLinearLayout.addView(insertEditText)
        hideSettingVariableWhenSettingEdit(
            editParameters,
            insertTextView,
            horizontalLinearLayout,
        )
        (variableTypeList.indices).forEach {
            val variableTypeListUntilCurrent =  variableTypeList.take(it + 1)
            val currentComponentIndex = variableTypeListUntilCurrent.filter {
                !noIndexTypeList.contains(it)
            }.size - 1
            when(variableTypeList[it]){
                EditTextSupportViewName.CHECK_BOX.str -> {
                    val insertSpinner = SpinnerViewProducer.make(
                        insertEditText,
                        editParameters,
                        currentComponentIndex,
                        otherComponentWeight,
                    )
                    horizontalLinearLayout.addView(insertSpinner)
                }
                EditTextSupportViewName.EDITABLE_CHECK_BOX.str -> {
                    val insertSpinner = EditableSpinnerViewProducer.make(
                        insertEditText,
                        editParameters,
                        currentComponentIndex,
                        otherComponentWeight,
                    )
                    horizontalLinearLayout.addView(insertSpinner)
                }
                EditTextSupportViewName.EDITABLE_FILE_SELECT_BOX.str -> {
                    val editableFileSelectSpinner = FileSelectSpinnerViewProducer.make(
                        insertEditText,
                        editParameters,
                        currentComponentIndex,
                        otherComponentWeight,
                    )
                    horizontalLinearLayout.addView(editableFileSelectSpinner)
                }
                EditTextSupportViewName.LIST_CONTENTS_SELECT_BOX.str -> {
                    val insertListConSpinner = ListContentsSelectSpinnerViewProducer.make(
                        insertEditText,
                        editParameters,
                        currentComponentIndex,
                        otherComponentWeight,
                    )
                    horizontalLinearLayout.addView(insertListConSpinner)
                }
                EditTextSupportViewName.EDITABLE_LIST_CONTENTS_SELECT_BOX.str -> {
                    val insertListConSpinner = EditableListContentsSelectSpinnerViewProducer.make(
                        insertEditText,
                        editParameters,
                        currentComponentIndex,
                        otherComponentWeight,
                    )
                    horizontalLinearLayout.addView(insertListConSpinner)
                }
                EditTextSupportViewName.NUM_INDE_CREMENTER.str -> {
                    val incButton = InDeCrementerViewProducer.make(
                        insertEditText,
                        editParameters,
                        currentComponentIndex,
                        otherComponentWeight,
                        true,
                    )
                    horizontalLinearLayout.addView(incButton)
                    val decButton = InDeCrementerViewProducer.make(
                        insertEditText,
                        editParameters,
                        currentComponentIndex,
                        otherComponentWeight,
                        false
                    )
                    horizontalLinearLayout.addView(decButton)
                }
                EditTextSupportViewName.READ_ONLY_EDIT_TEXT.str -> {
                    insertEditText.isEnabled = false
                }
                EditTextSupportViewName.PASSWORD.str -> {
                    insertEditText.inputType = (
                            InputType.TYPE_CLASS_TEXT or
                                    InputType.TYPE_TEXT_VARIATION_PASSWORD
                            )
                }
                else -> {}
            }
        }
        return horizontalLinearLayout
    }

    private fun initEditText(
        editParameters: EditParameters,
        editTextWeight: Float
    ): EditText {
        val context = editParameters.context
        val currentId = editParameters.currentId
        val currentVariableValue = editParameters.currentVariableValue
        val currentVariableName = editParameters.currentVariableName

        val linearParamsForEditTextTest = LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        val insertEditText = EditText(context)
        insertEditText.tag = currentVariableName
        insertEditText.id = currentId
        insertEditText.inputType = InputType.TYPE_CLASS_TEXT
        insertEditText.setText(currentVariableValue)
        insertEditText.setSelectAllOnFocus(true)
        insertEditText.clearFocus()
//        insertEditText.setTextColor(Color.parseColor("#FFFFFF"))
        linearParamsForEditTextTest.weight = editTextWeight
        insertEditText.layoutParams = linearParamsForEditTextTest
        return insertEditText
    }

    private  fun makeHorizontalLayout(
        context: Context?
    ): LinearLayout {
        val horizontalLinearLayout = LinearLayout(context)
        horizontalLinearLayout.orientation = LinearLayout.HORIZONTAL
        horizontalLinearLayout.weightSum = 1F
        val linearParamsForHorizontal = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        horizontalLinearLayout.layoutParams = linearParamsForHorizontal
        return horizontalLinearLayout
    }

    private fun decideTextEditWeight(
        variableTypeList: List<String>,
    ): Float {
        val isTextEmphasis = variableTypeList.contains(
            EditTextSupportViewName.EDIT_TEXT_EMPHASIS.str
        )
        val TextAndLabelOtherCompLength = makeTextAndLabelOtherCompLength(variableTypeList)
        if(
            TextAndLabelOtherCompLength == 0
        ) return 1F
        if(
            !isTextEmphasis
        ) return 0.001F
        return when(true){
            (TextAndLabelOtherCompLength >= 3) -> 0.45F
            (TextAndLabelOtherCompLength == 2) -> 0.6F
            (TextAndLabelOtherCompLength == 1) -> 0.7F
            else -> 1F
        }
    }

    private fun decideOtherComponentWeight(
        editTextWeight: Float,
        variableTypeList: List<String>,
    ): Float {
        val variableTypeIndexLength = makeVariableTypeIndexLength(
            variableTypeList
        )
        if(
            variableTypeIndexLength > 0
        ) return (1F - editTextWeight) / variableTypeIndexLength
        return  0F
    }

    private fun makeVariableTypeIndexLength(
        variableTypeList: List<String>
    ): Int {
        val otherCompLengthSource = variableTypeList.filter {
            !textAndLabelList.contains(it)
        }.size
        val isNumComponent = variableTypeList.contains(
            EditTextSupportViewName.NUM_INDE_CREMENTER.str
        )
        if (
            isNumComponent
        ) return otherCompLengthSource + 1
        return otherCompLengthSource
    }

    private fun makeTextAndLabelOtherCompLength(
        variableTypeList: List<String>
    ): Int {
        val textAndLabelOtherCompLength = variableTypeList.filter {
            !textAndLabelList.contains(it)
        }.size
        val isNumComponent = variableTypeList.contains(
            EditTextSupportViewName.NUM_INDE_CREMENTER.str
        )
        if (
            isNumComponent
        ) return textAndLabelOtherCompLength + 1
        return textAndLabelOtherCompLength
    }

    private fun hideSettingVariableWhenSettingEdit(
        editParameters: EditParameters,
        insertTextView: TextView,
        horizontalLinearLayout: LinearLayout,
    ){
        val hideSettingVariableList = editParameters.hideSettingVariableList
        if(
            hideSettingVariableList.isEmpty()
        ) return
        val isHidden = !editParameters.hideSettingVariableList.contains(
            editParameters.currentVariableName
        )
        horizontalLinearLayout.isVisible = isHidden
        insertTextView.isVisible = isHidden
    }
}