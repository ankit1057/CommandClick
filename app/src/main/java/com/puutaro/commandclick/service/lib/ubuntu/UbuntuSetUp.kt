package com.puutaro.commandclick.service.lib.ubuntu

import android.content.Context
import android.content.Intent
import com.puutaro.commandclick.common.variable.BroadCastIntentScheme
import com.puutaro.commandclick.common.variable.path.UsePath
import com.puutaro.commandclick.proccess.ubuntu.BusyboxExecutor
import com.puutaro.commandclick.proccess.ubuntu.UbuntuFiles
import com.puutaro.commandclick.proccess.ubuntu.UbuntuInfo
import com.puutaro.commandclick.util.AssetsFileManager
import com.puutaro.commandclick.util.FileSystems
import com.puutaro.commandclick.util.LinuxCmd
import com.puutaro.commandclick.util.NetworkTool
import com.puutaro.commandclick.util.ReadText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URL


object UbuntuSetUp {

    private val startupFilePath = "/support/startup.sh"
    fun set(
        context: Context?,
        monitorFileName: String,
    ): Job? {
       return try {
            CoroutineScope(Dispatchers.IO).launch {
                execSet(
                    context,
                    monitorFileName,
                )
            }
        } catch (e: Exception){
            FileSystems.updateFile(
                UsePath.cmdclickMonitorDirPath,
                monitorFileName,
                    "\n\n${e}"
            )
           null
        }
    }
    private suspend fun execSet(
        contextSrc: Context?,
        monitorFileName: String,
    ) {
        val context  = contextSrc
            ?: return
        val ubuntuFiles = UbuntuFiles(
            context,
        )
        try {
            downloadUbuntu(
                ubuntuFiles,
                monitorFileName
            )
        } catch (e: Exception){
            val setupIntent = Intent()
            setupIntent.action = BroadCastIntentScheme.ON_UBUNTU_SETUP_NOTIFICATION.action
            context.sendBroadcast(setupIntent)
            return
        }
        val busyboxExecutor = BusyboxExecutor(
            context,
            ubuntuFiles,
        )
        if(
            ubuntuFiles.ubuntuSetupCompFile.isFile
        ) {
            busyboxExecutor.executeProotCommand(
                listOf("bash", startupFilePath),
                monitorFileName = monitorFileName
            )
            return
        }
        val isWifi = NetworkTool.isWifi(context)
        if(!isWifi) {
            val wifiNotiIntent = Intent()
            wifiNotiIntent.action = BroadCastIntentScheme.WIFI_WAIT_NITIFICATION.action
            context.sendBroadcast(wifiNotiIntent)
            return
        }
        FileSystems.removeAndCreateDir(
            ubuntuFiles.filesOneRootfs.absolutePath
        )
        delay(200)
        FileSystems.updateFile(
            UsePath.cmdclickMonitorDirPath,
            monitorFileName,
            "\nbysubox instance start"
        )
        initSetup(
            context,
            ubuntuFiles,
            monitorFileName
        )
        FileSystems.updateFile(
            UsePath.cmdclickMonitorDirPath,
            monitorFileName,
            "\nextract file"
        )
        val err4 = LinuxCmd.execCommand(
            listOf(
                "chmod",
                "-R",
                "777",
                ubuntuFiles.supportDir
            ).joinToString("\t")
        )
        busyboxExecutor.executeScript(
            "support/extractRootfs.sh",
            monitorFileName
        )
        FileSystems.removeFiles(
            ubuntuFiles.filesDir.absolutePath,
            UbuntuFiles.rootfsTarGzName
        )
        ubuntuFiles.filesOneRootfsSupportDir.mkdirs()

        ubuntuFiles.filesOneRootfsSupportDir.mkdirs()
        AssetsFileManager.copyFileOrDirFromAssets(
            context,
            AssetsFileManager.ubunutSupportDirPath,
            "ubuntu_setup",
            ubuntuFiles.filesOneRootfs.absolutePath
        )
        ubuntuFiles.filesOneRootfsSupportCommonDir.mkdirs()
        ubuntuFiles.filesOneRootfsStorageDir.mkdir()
//        ulaFiles.filesOneRootfsStorageEmurated0Dir.mkdirs()
        ubuntuFiles.filesOneRootfsUsrLocalBinSudo.mkdirs()
        ubuntuFiles.filesOneRootfsEtcDProfileDir.mkdir()
        FileSystems.updateFile(
            UsePath.cmdclickMonitorDirPath,
            monitorFileName,
            "\nsupport copy start"
        )
        val rootfsSupportDir =  File("${ubuntuFiles.filesOneRootfs}/support")
        if(!rootfsSupportDir.isDirectory) rootfsSupportDir.mkdir()
        busyboxExecutor.executeProotCommand(
            listOf("bash", startupFilePath),
            monitorFileName = monitorFileName
        )
    }

    private suspend fun downloadUbuntu(
        ubuntuFiles: UbuntuFiles,
        monitorFileName: String,
    ){
        val supportDirPath = ubuntuFiles.supportDir.absolutePath
        val downloadCompTxt = "downloadComp.txt"
        FileSystems.createDirs(
            supportDirPath
        )
        withContext(Dispatchers.IO) {
            if(
                UbuntuInfo.onForDev
            ) return@withContext
            if(
                File(
                    "${supportDirPath}/${downloadCompTxt}",
                ).isFile
            ) return@withContext
            FileSystems.removeFiles(
                UbuntuFiles.downloadDirPath,
                UbuntuFiles.rootfsTarGzName
            )
            // put your url.this is sample url.
            val url = URL(UbuntuInfo.arm64UbuntuRootfsUrl)
            val conection = url.openConnection()
            conection.connect()
            val lenghtOfFile = conection.contentLength
            // download the file
            val input = conection.getInputStream()
            //catalogfile is your destenition folder
            val output: OutputStream = FileOutputStream(
                UbuntuFiles.downloadRootfsTarGzPath
            )
            val data = ByteArray(1024)
            var total: Long = 0
            var count: Int
            var progress = 0L
            var previoutDisplayProgress: Long = 0
            while (input.read(data).also { count = it } != -1) {
                total += count
                previoutDisplayProgress = progress
                progress = total * 100 / lenghtOfFile
                output.write(data, 0, count)
                if(progress <= previoutDisplayProgress) continue
                FileSystems.updateFile(
                    UsePath.cmdclickMonitorDirPath,
                    monitorFileName,
                    "download ${progress}%"
                )
            }
            // flushing output
            output.flush()
            // closing streams
            output.close()
            input.close()
        }
        withContext(Dispatchers.IO){
            FileSystems.writeFile(
                supportDirPath,
                downloadCompTxt,
                String()
            )
        }
    }

    private fun initSetup(
        context: Context?,
        ubuntuFiles: UbuntuFiles,
        monitorFileName: String,
    ){
        FileSystems.createDirs(
            ubuntuFiles.supportDir.absolutePath
        )
        FileSystems.writeFile(
            UsePath.cmdclickMonitorDirPath,
            monitorFileName,
            "${
                ReadText(
                    UsePath.cmdclickMonitorDirPath,
                    monitorFileName,
                ).readText()
            }\nsupport copy start"
        )
        AssetsFileManager.copyFileOrDirFromAssets(
            context,
            AssetsFileManager.ubunutSupportDirPath,
            "ubuntu_setup",
            ubuntuFiles.supportDir.absolutePath
        )
        FileSystems.writeFile(
            UsePath.cmdclickMonitorDirPath,
            monitorFileName,
            "${
                ReadText(
                    UsePath.cmdclickMonitorDirPath,
                    monitorFileName,
                ).readText()
            }\nchmod start"
        )
        ubuntuFiles.supportDir.listFiles()?.forEach {
            ubuntuFiles.makePermissionsUsable(
                ubuntuFiles.supportDir.absolutePath,
                it.name
            )
        }
        FileSystems.createDirs(
            ubuntuFiles.filesOneRootfs.absolutePath
        )
        FileSystems.writeFile(
            UsePath.cmdclickMonitorDirPath,
            monitorFileName,
            "${
                ReadText(
                    UsePath.cmdclickMonitorDirPath,
                    monitorFileName,
                ).readText()
            }\nrootfs copy start"
        )
        FileSystems.copyFile(
            UbuntuFiles.downloadRootfsTarGzPath,
            "${ubuntuFiles.filesDir}/${UbuntuFiles.rootfsTarGzName}"
        )
        if(
            !UbuntuInfo.onForDev
        ) {
            FileSystems.removeFiles(
                UbuntuFiles.downloadDirPath,
                UbuntuFiles.rootfsTarGzName
            )
        }

        ubuntuFiles.makePermissionsUsable(
            ubuntuFiles.filesOneRootfs.absolutePath,
            UbuntuFiles.rootfsTarGzName
        )
        File(
            "${ubuntuFiles.filesOneRootfs.absolutePath}/.success_filesystem_extraction"
        ).createNewFile()
        FileSystems.createDirs(
            ubuntuFiles.emulatedUserDir.absolutePath
        )
        ubuntuFiles.sdCardUserDir?.absolutePath?.let {
            FileSystems.createDirs(
                it
            )
        }
        ubuntuFiles.setupLinks()
    }
}

enum class UbuntuStateType(
    val title: String,
    val message: String,
) {
    WAIT("Wait..", "Wait.."),
    UBUNTU_SETUP_WAIT(
        "Ubuntu Setup, Ok?",
        "Take 5 minutes for install"
    ),
    WIFI_WAIT("Connect wifi!", "Connect wifi! and restart"),
    ON_SETUP("Ubuntu Setup..", "Ubuntu Setup..(take 5 minutes)"),
    RUNNING("Ubuntu running..", "%d process.."),
}

enum class ButtonLabel(
    val label: String,
){
    CANCEL("CANCEL"),
    RESTART("RESTART"),
    SETUP("SETUP"),
    TERMINAL("TERMINAL"),
}