package com.puutaro.commandclick.proccess.qr

import com.puutaro.commandclick.common.variable.variables.QrLaunchType
import com.puutaro.commandclick.common.variable.variables.QrSeparator
import com.puutaro.commandclick.util.Intent.CurlManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

object QrDecodedTitle {

    private val displayTitleTextLimit = 50
    private val jsDescSeparator = QrSeparator.sepalator.str

    suspend fun makeTitle(scanCon: String): String{
        return when(true){
            scanCon.startsWith(QrLaunchType.Http.prefix),
            scanCon.startsWith(QrLaunchType.Https.prefix)
            -> makeDocFromUrl(
                scanCon
            )
            scanCon.startsWith(QrLaunchType.Javascript.prefix) -> {
                scanCon.take(
                    displayTitleTextLimit
                )
            }
            scanCon.startsWith(QrLaunchType.CpFile.prefix) -> {
                createCopyFileTitle(scanCon)
            }
            scanCon.startsWith(QrLaunchType.JsDesc.prefix)
            -> extractTitleForJsDesc(scanCon)
            scanCon.startsWith(QrLaunchType.WIFI.prefix),
            scanCon.startsWith(QrLaunchType.WIFI.prefix.uppercase())
            -> createWifiTitle(scanCon)
            scanCon.startsWith(QrLaunchType.SMS.prefix),
            scanCon.startsWith(QrLaunchType.SMS.prefix.uppercase())
            -> createSmsTitle(scanCon)
            scanCon.startsWith(QrLaunchType.MAIL.prefix),
            scanCon.startsWith(QrLaunchType.MAIL.prefix.uppercase()),
            scanCon.startsWith(QrLaunchType.MAIL2.prefix),
            scanCon.startsWith(QrLaunchType.MAIL2.prefix.uppercase()),
            ->  createGmailTitle(scanCon)
            scanCon.startsWith(QrLaunchType.G_CALENDAR.prefix),
            scanCon.startsWith(QrLaunchType.G_CALENDAR.prefix.uppercase())
            -> createGcalendarTitle(scanCon)
            scanCon.startsWith(QrLaunchType.ON_GIT.prefix)
            -> createOnGitTitle(scanCon)
            else -> "Copy ok?: $scanCon"
        }
    }

    private fun createCopyFileTitle(
        scanCon: String
    ): String {
        val cpFileMap = QrMapper.makeCpFileMap(scanCon)
        val filePath = cpFileMap.get(CpFileKey.PATH.key)
        if(
            filePath.isNullOrEmpty()
        ){
            return scanCon.take(displayTitleTextLimit)
        }
        return "Copy from phone ok?: path: ${filePath}".take(
            displayTitleTextLimit
        )
    }


    private fun createWifiTitle(
        scanCon: String
    ): String {
        return QrMapper.getWifiWpaSsidAndPinPair(scanCon).let {
            "WIFI ssid: ${it.first} pin: ${it.second}"
        }.take(displayTitleTextLimit)
    }
    private fun createSmsTitle(
        scanCon: String
    ): String {
        return QrMapper.getSmsNumAndBody(scanCon).let {
            "SMS tel: ${it.first} body: ${it.second}"
        }.take(displayTitleTextLimit)
    }

    private fun createGmailTitle(
        scanCon: String,
    ): String {
        val gmailMap = QrMapper.makeGmailMap(scanCon)
        val subject = gmailMap?.get(GmailKey.SUBJECT.key)
        if(
            !subject.isNullOrEmpty()
        ) return "Gmail: $subject"
        val mailAd = gmailMap?.get(
            GmailKey.MAIL_AD.key
        )
        val body = gmailMap?.get(GmailKey.BODY.key)
        return "Ad ${mailAd} Body: ${body}"
            .take(displayTitleTextLimit)
    }

    private fun createGcalendarTitle(scanCon: String): String {
        val gcalendarMap = QrMapper.makeGCalendarMap(scanCon)
        val title = gcalendarMap.get(GCalendarKey.TITLE.key)
            ?: return scanCon.take(displayTitleTextLimit)
        return "calendar: $title".take(displayTitleTextLimit)
    }

    private fun createOnGitTitle(
        scanCon: String
    ): String {
        val onGitMap = QrMapper.makeOnGitMap(scanCon)
        val title = onGitMap.get(OnGitKey.NAME.key)
            ?: return scanCon.take(displayTitleTextLimit)
        return "Git download: $title".take(displayTitleTextLimit)
    }

    private fun extractTitleForJsDesc(scanCon: String): String {
        return scanCon
            .split(jsDescSeparator)
            .firstOrNull()?.trim()
            ?.removePrefix(QrLaunchType.JsDesc.prefix)
            ?.trim()
            ?: scanCon.take(
                displayTitleTextLimit
            )
    }

    private suspend fun makeDocFromUrl(
        targetUrl: String
    ): String {
        val htmlString =
            withContext(Dispatchers.IO) {
                CurlManager.get(
                    targetUrl,
                    "",
                    "",
                    2000
                ).let {
                    CurlManager.convertResToStrByConn(it)
                }
            }
        val doc = Jsoup.parse(htmlString)
        val titleSrc = doc.title()
        if(
            titleSrc.isEmpty()
        ) return "Url: ${targetUrl}"
        return "Url title: ${titleSrc}"
    }

}