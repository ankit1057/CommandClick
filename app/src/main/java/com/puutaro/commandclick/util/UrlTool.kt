package com.puutaro.commandclick.util

import com.puutaro.commandclick.util.url.WebUrlVariables

object UrlTool {
    fun trimTitle(
        urlTitleSource: String
    ): String {
        val titleLimitStrNum = 100
        val urlTitleLength = urlTitleSource.length
        return if(
            urlTitleLength <= titleLimitStrNum
        )  urlTitleSource
        else urlTitleSource
            .substring(0, titleLimitStrNum) + "..."
    }

    fun extractDomain(
        url: String
    ): String? {
        val httpsPrefixMark = "httpsPrefix"
        val httpPrefixMark = "httpPrefix"
        val filePrefixMark = "filePrefix"
        val slashPrefixMark = "slashPrefix"
        val httpsPrefix = WebUrlVariables.httpsPrefix
        val httpPrefix = WebUrlVariables.httpPrefix
        val filePrefix = WebUrlVariables.filePrefix
        val slashPrefix = WebUrlVariables.slashPrefix
        return url.replace(
            httpsPrefix,
            httpsPrefixMark
        )
            .replace(
                Regex("^${slashPrefix}"),
                slashPrefixMark
            )
            .replace(
                httpPrefix,
                httpPrefixMark
            )
            .replace(
                filePrefix,
                filePrefixMark
            )
            .split("/")
            .firstOrNull()
            ?.replace(
                slashPrefixMark,
                slashPrefix,
            )
            ?.replace(
                httpsPrefixMark,
                httpsPrefix,
            )
            ?.replace(
                httpPrefixMark,
                httpPrefix,
            )
            ?.replace(
                filePrefixMark,
                filePrefix,
            )
    }
}