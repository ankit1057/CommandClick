package com.puutaro.commandclick.fragment_lib.command_index_fragment.list_view_lib.internet_button

import android.R
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.puutaro.commandclick.common.variable.WebUrlVariables
import com.puutaro.commandclick.fragment.CommandIndexFragment
import com.puutaro.commandclick.fragment_lib.command_index_fragment.common.UrlTexter
import com.puutaro.commandclick.util.Keyboard
import java.net.URLDecoder


class SuggestEditTexter(
    private val cmdIndexFragment: CommandIndexFragment,
) {

    private val tabReplaceStr = "\t"

    private val context = cmdIndexFragment.context as Context
    private val binding = cmdIndexFragment.binding
    private val cmdSearchEditText = binding.cmdSearchEditText
    private val queryLimitStrLength = 50


    fun setAdapter(
        context: Context?,
        cmdSearchEditText: AutoCompleteTextView,
        suggestList: List<String>
    ) {
        if(context == null) return
        cmdSearchEditText.setAdapter(
            makeUrlComAdapter(
                context,
                suggestList,
            )
        )
        cmdSearchEditText.threshold = 0
    }

    private fun makeUrlComAdapter(
        context: Context,
        suggestList: List<String>
    ): ArrayAdapter<String> {
        return ArrayAdapter(
            context,
            R.layout.simple_list_item_1,
            suggestList
        )
    }


    fun setItemClickListener(){
        cmdSearchEditText.setOnItemClickListener { parent, _, position, _ ->
            val selectedUrlSource = parent.getItemAtPosition(position) as String
            val selectedUrl = selectedUrlSource.split(tabReplaceStr).lastOrNull()
            val queryUrl = WebUrlVariables.queryUrl

            if (
                selectedUrl?.startsWith(queryUrl) != true
            ) {
                execUrlLaunch(selectedUrl)
                return@setOnItemClickListener
            }
            val decodedSelectedUrl =
                URLDecoder.decode(
                    selectedUrl.removePrefix(queryUrl),
                    "utf-8"
                )
            if(
                decodedSelectedUrl.length < queryLimitStrLength
            ) {
                cmdSearchEditText.setText(
                    decodedSelectedUrl
                )
                return@setOnItemClickListener
            }

            cmdSearchEditText.clearFocus()
            Keyboard.hiddenKeyboardForFragment(
                cmdIndexFragment
            )
            execUrlLaunch(selectedUrl)
        }
    }

    private fun execUrlLaunch(
        selectedUrl: String?
    ){
        cmdSearchEditText.clearFocus()
        Keyboard.hiddenKeyboardForFragment(
            cmdIndexFragment
        )
        UrlTexter.launch(
            context,
            cmdSearchEditText,
            selectedUrl
        )
    }

}