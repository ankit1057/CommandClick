package com.puutaro.commandclick.proccess.edit.edit_text_support_view.lib

import android.content.Context
import android.view.View
import android.widget.*
import com.puutaro.commandclick.R
import com.puutaro.commandclick.common.variable.SharePrefferenceSetting
import com.puutaro.commandclick.common.variable.edit.EditParameters
import com.puutaro.commandclick.common.variable.edit.SetVariableTypeColumn
import com.puutaro.commandclick.fragment_lib.edit_fragment.variable.EditTextSupportViewId
import com.puutaro.commandclick.proccess.edit.lib.ReplaceVariableMapReflecter
import com.puutaro.commandclick.proccess.edit.lib.SpinnerInstance
import com.puutaro.commandclick.util.*
import java.io.File


object EditableListContentsSelectSpinnerViewProducer {

    fun make (
        insertEditText: EditText,
        editParameters: EditParameters,
        weight: Float,
    ): Spinner {
        val context = editParameters.context
        val currentId = editParameters.currentId
        val currentSetVariableMap = editParameters.setVariableMap
        val currentAppDirPath = SharePreffrenceMethod.getReadSharePreffernceMap(
            editParameters.readSharePreffernceMap,
            SharePrefferenceSetting.current_app_dir
        )
        val throughMark = "-"
        val linearParamsForSpinner = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
        )
        linearParamsForSpinner.weight = weight

        val listContentsFilePath = currentSetVariableMap?.get(
            SetVariableTypeColumn.VARIABLE_TYPE_VALUE.name
        )?.split('|')
            ?.firstOrNull()
            ?.replace("\${01}", currentAppDirPath)
            .let {
                ReplaceVariableMapReflecter.reflect(
                    BothEdgeQuote.trim(it),
                    editParameters
                )
            } ?: String()
        val fileObj = File(listContentsFilePath)
        val parentDir = fileObj.parent ?: String()
        val listFileName = fileObj.name
        FileSystems.createDirs(parentDir)
        val editableSpinnerList = ReadText(
            parentDir,
            listFileName
        ).textToList().filter {
            it.trim().isNotEmpty()
        }
        val updatedEditableSpinnerList = listOf(throughMark) + editableSpinnerList

        val insertSpinner = SpinnerInstance.make(
            context,
            updatedEditableSpinnerList,
            editParameters.onFixNormalSpinner
        )
        insertSpinner.id = currentId + EditTextSupportViewId.EDITABLE_SPINNER.id
        insertSpinner.tag = "spinnerEdit${currentId + EditTextSupportViewId.EDITABLE_SPINNER.id}"
        val adapter = ArrayAdapter<String>(
            context as Context,
            R.layout.sppinner_layout,
        )

        adapter.addAll(updatedEditableSpinnerList)
        insertSpinner.adapter = adapter
        insertSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val selectedItem = adapter.getItem(pos)
                    ?: return
                val currentSpinnerList = ReadText(
                    parentDir,
                    listFileName
                ).textToList().filter {
                    it.trim().isNotEmpty()
                   }
                val updateListContents = if(selectedItem == throughMark) {
                    currentSpinnerList.filter {
                        it != selectedItem
                    }
                } else {
                    listOf(selectedItem) + currentSpinnerList.filter {
                        it != selectedItem
                                && it != throughMark
                    }
                }
                FileSystems.writeFile(
                    parentDir,
                    listFileName,
                    updateListContents.joinToString("\n")
                )
                val selectUpdatedSpinnerList = if(
                    selectedItem == throughMark
                ){
                    listOf(throughMark) + currentSpinnerList
                } else listOf(
                    throughMark,
                    selectedItem,
                ) + currentSpinnerList.filter {
                    it != selectedItem
                }
                adapter.clear()
                adapter.addAll(selectUpdatedSpinnerList)
                adapter.notifyDataSetChanged()
                insertSpinner.setSelection(0)
                if(
                    selectedItem == throughMark
                ) return
                insertEditText.setText(selectedItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        insertSpinner.layoutParams = linearParamsForSpinner
        return insertSpinner
    }
}
