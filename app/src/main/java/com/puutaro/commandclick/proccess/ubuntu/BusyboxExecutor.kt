package com.puutaro.commandclick.proccess.ubuntu

import android.content.Context
import com.puutaro.commandclick.common.variable.network.UsePort
import com.puutaro.commandclick.common.variable.path.UsePath
import com.puutaro.commandclick.util.FileSystems
import com.puutaro.commandclick.util.NetworkTool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class BusyboxExecutor(
    private val context: Context?,
    private val ubuntuFiles: UbuntuFiles,
//    private val prootDebugLogger: ProotDebugLogger,
    private val busyboxWrapper: BusyboxWrapper = BusyboxWrapper(ubuntuFiles)
) {
    private val className = this::class.java.name
    private val cmdclickMonitorDirPath = UsePath.cmdclickMonitorDirPath

    fun executeScript(
        scriptCall: String,
        monitorFileName: String
    ) {
        val updatedCommand = busyboxWrapper.wrapScript(scriptCall)

        return runCommand(updatedCommand, monitorFileName)
    }

    fun executeCommand(
        command: String,
        monitorFileName: String
    ) {
        val updatedCommand = busyboxWrapper.wrapCommand(command)

        return runCommand(updatedCommand, monitorFileName)
    }

    private fun runCommand(
        command: List<String>,
        monitorFileName: String
    ) {
        val functionName = object{}.javaClass.enclosingMethod?.name
        if (!busyboxWrapper.busyboxIsPresent()) {
            return FileSystems.updateFile(
                cmdclickMonitorDirPath,
                monitorFileName,
                "${className} ${functionName} no busybox"
            )
        }

        val env = busyboxWrapper.getBusyboxEnv()
        val processBuilder = ProcessBuilder(command)
        processBuilder.directory(ubuntuFiles.filesDir)
        processBuilder.environment().putAll(env)
        processBuilder.redirectErrorStream(true)

        return try {
            val process = processBuilder.start()
            streaming(
                process,
                monitorFileName
            )
            val exitCode = process.waitFor()
            outputFailureStatus(
                process,
                exitCode,
                "$functionName",
                monitorFileName
            )
        } catch (err: Exception) {
            FileSystems.updateFile(
                cmdclickMonitorDirPath,
                monitorFileName,
                "$err"
            )
        }
    }

    fun executeKillApp(
        monitorFileName: String
    ){
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO){
                executeKillAllProcess(monitorFileName)
                delay(200)
            }
            withContext(Dispatchers.IO){
                executeProotCommand(
                    listOf("bash", "-c", "echo kill;kill -9 $(ps aux | awk '{print $2}' );kill end"),
                    monitorFileName = monitorFileName
                )
            }
        }
    }

    fun executeKillAllProcess(
        monitorFileName: String
    ){
        val packageName = context?.packageName ?: String()
        executeProotCommand(
            listOf("bash", "-c", "echo kill;kill -9 $(ps aux | grep -v \"$packageName\" | awk '{print $2}' );kill end"),
            monitorFileName = monitorFileName
        )
    }

    fun executeStartFrontProcess(
        monitorFileName: String
    ){
        executeProotCommand(
            listOf("bash", "/support/restartup.sh"),
            monitorFileName = monitorFileName
        )
    }
    fun executeKillFrontProcess(
        monitorFileName: String
    ){
        executeProotCommand(
            listOf("bash", "/support/kill_front_process.sh"),
            monitorFileName = monitorFileName
        )
    }

    fun executeKillSubFrontProcess(
        monitorFileName: String
    ){
        executeProotCommand(
            listOf("bash", "/support/kill_sub_front_process.sh"),
            monitorFileName = monitorFileName
        )
    }

    fun executeKillProcess(
        targetProcessName: String,
        monitorFileName: String
    ){
        val packageName = context?.packageName ?: String()
        executeProotCommand(
            listOf("bash", "-c", "echo kill;kill -9 $(ps aux | grep '${targetProcessName}' | grep bash | grep -v \"$packageName\" | awk '{print $2}' );kill end"),
            monitorFileName = monitorFileName
        )
    }

    fun executeKillProcessFromList(
        targetProcessNameList: List<String>,
        monitorFileName: String
    ){
        executeProotCommand(
            listOf("bash", "/support/killProcTree.sh") + targetProcessNameList,
            monitorFileName = monitorFileName
        )
    }

    fun executeProotCommand(
        command: List<String>,
//        filesystemDirName: String,
//        commandShouldTerminate: Boolean,
        env: HashMap<String, String> = hashMapOf(),
        monitorFileName: String,

    ) {
        val functionName = object{}.javaClass.enclosingMethod?.name
        when {
            !busyboxWrapper.busyboxIsPresent() -> {
                FileSystems.updateFile(
                    cmdclickMonitorDirPath,
                    monitorFileName,
                    "${className} ${functionName}, no busybox"
                )
                return
            }
            !busyboxWrapper.prootIsPresent() -> {
                FileSystems.updateFile(
                    cmdclickMonitorDirPath,
                    monitorFileName,
                    "${className} ${functionName}, no proot cmd"
                )
                return
            }
            !busyboxWrapper.executionScriptIsPresent() -> {
                FileSystems.updateFile(
                    cmdclickMonitorDirPath,
                    monitorFileName,
                    "${className} ${functionName}, no execution script"
                )
                return
            }
        }

        val updatedCommand = busyboxWrapper.addBusyboxAndProot(command)
        val filesystemDir = File(
            ubuntuFiles.filesOneRootfs.absolutePath
        )
        env.putAll(
            busyboxWrapper.getProotEnv(
                context,
                filesystemDir,
            )
        )

        val processBuilder = ProcessBuilder(updatedCommand)
        processBuilder.directory(ubuntuFiles.filesDir)
        processBuilder.environment().putAll(env)
        processBuilder.redirectErrorStream(true)

        try {
            val process = processBuilder.start()
            streaming(
                process,
                monitorFileName
            )
            val exitCode = process.waitFor()
            outputFailureStatus(
                process,
                exitCode,
                functionName,
                monitorFileName
            )
        } catch (err: Exception) {
            FileSystems.updateFile(
                cmdclickMonitorDirPath,
                monitorFileName,
                "${className} ${functionName} ${err}"
            )
        }
    }

    private fun outputFailureStatus(
        process: Process,
        exitCode: Int,
        functionName: String?,
        monitorFileName: String,
    ){
        if(exitCode == 0) return
        FileSystems.updateFile(
            cmdclickMonitorDirPath,
            monitorFileName,
            "${className} ${functionName} failure ${process.exitValue()}"
        )
    }

    private fun streaming(
        process: Process,
        monitorName: String
    ){
        val inputStream = process.inputStream
        val reader = inputStream.bufferedReader(Charsets.UTF_8)
        reader.forEachLine { line ->
            if(
                line.trim().isEmpty()
            ) return@forEachLine
            FileSystems.updateFile(
                cmdclickMonitorDirPath,
                monitorName,
                line
            )
        }
        if(process.inputStream != null){
            process.inputStream.close()
        }
        val errStream = process.errorStream
        val errReader = errStream.bufferedReader(Charsets.UTF_8)
        errReader.forEachLine { line ->
            if(
                line.trim().isEmpty()
            ) return@forEachLine
            FileSystems.updateFile(
                cmdclickMonitorDirPath,
                monitorName,
                line
            )
        }
        if(process.errorStream != null){
            process.errorStream.close()
        }
    }

}

// This class is intended to allow stubbing of elements that are unavailable during unit tests.
class BusyboxWrapper(private val ubuntuFiles: UbuntuFiles) {
    // For basic commands, CWD should be `applicationFilesDir`
    fun wrapCommand(command: String): List<String> {
        return listOf(ubuntuFiles.busybox.path, "sh", "-c", command)
    }

    fun wrapScript(command: String): List<String> {
        return listOf(ubuntuFiles.busybox.path, "sh") + command.split(" ")
    }

    fun getBusyboxEnv(): HashMap<String, String> {
        return hashMapOf(
                "LIB_PATH" to ubuntuFiles.supportDir.absolutePath,
                "ROOT_PATH" to ubuntuFiles.filesDir.absolutePath,
                "ROOTFS_PATH" to ubuntuFiles.filesOneRootfs.absolutePath,
        )
    }

    fun busyboxIsPresent(): Boolean {
        return ubuntuFiles.busybox.exists()
    }

    // Proot scripts expect CWD to be `applicationFilesDir/<filesystem`
    fun addBusyboxAndProot(command: List<String>): List<String> {
        return listOf(ubuntuFiles.busybox.absolutePath, "sh", "support/execInProot.sh") + command
    }

    fun getProotEnv(
        context: Context?,
        rootfsDir: File,
//        prootDebugLevel: String
    ): HashMap<String, String> {
        // TODO This hack should be removed once there are no users on releases 2.5.14 - 2.6.1
        handleHangingBindingDirectories(rootfsDir)
        val emulatedStorageBinding = "-b ${ubuntuFiles.emulatedUserDir.absolutePath}:/storage/internal"
        val externalStorageBinding = ubuntuFiles.sdCardUserDir?.run {
            "-b ${this.absolutePath}:/storage/sdcard"
        } ?: ""
        val bindings = "$emulatedStorageBinding $externalStorageBinding"
        return hashMapOf(
            "LD_LIBRARY_PATH" to ubuntuFiles.supportDir.absolutePath,
            "LIB_PATH" to ubuntuFiles.supportDir.absolutePath,
            "ROOT_PATH" to ubuntuFiles.filesDir.absolutePath,
            "ROOTFS_PATH" to rootfsDir.absolutePath,
//                "PROOT_DEBUG_LEVEL" to prootDebugLevel,
            "EXTRA_BINDINGS" to bindings,
            "OS_VERSION" to System.getProperty("os.version")!!,
            "IP_V4_ADDRESS" to NetworkTool.getIpv4Address(context),
            "PACKAGE_NAME" to context?.packageName.toString(),
            "UBUNTU_PC_PULSE_SET_SERVER_PORT" to UsePort.UBUNTU_PC_PULSE_SET_SERVER_PORT.num.toString(),
            "UBUNTU_PULSE_RECEIVER_PORT" to UsePort.UBUNTU_PULSE_RECEIVER_PORT.num.toString(),
            "HTTP2_SHELL_PORT" to UsePort.HTTP2_SHELL_PORT.num.toString(),
            "WEB_SSH_TERM_PORT" to UsePort.WEB_SSH_TERM_PORT.num.toString(),
            "DROPBEAR_SSH_PORT" to UsePort.DROPBEAR_SSH_PORT.num.toString(),
            "CMDCLICK_USER" to UbuntuInfo.user,
            "CREATE_IMAGE_SWITCH" to UbuntuInfo.createImageSwitch,
            "APP_ROOT_PATH" to UsePath.cmdclickDirPath,
            "HTTP2_SHELL_PATH" to "${UsePath.cmdclickTempCmdDirPath}/${UsePath.cmdclickTempCmdShellName}",
            "INTENT_MONITOR_PATH" to "${UsePath.cmdclickTempIntentMonitorDirPath}/${UsePath.cmdclickTmpIntentMonitorRequestFileName}",
            "MONITOR_DIR_PATH" to UsePath.cmdclickMonitorDirPath,
            "APP_DIR_PATH" to UsePath.cmdclickAppDirPath,
        )
    }

    fun prootIsPresent(): Boolean {
        return ubuntuFiles.proot.exists()
    }

    fun executionScriptIsPresent(): Boolean {
        val execInProotFile = File(ubuntuFiles.supportDir, "execInProot.sh")
        return execInProotFile.exists()
    }

    // TODO this hack should be removed when no users are left using version 2.5.14 - 2.6.1
    private fun handleHangingBindingDirectories(filesystemDir: File) {
        // If users upgraded from a version 2.5.14 - 2.6.1, the storage directory will exist but
        // with unusable permissions. It needs to be recreated.
        val storageBindingDir = File(filesystemDir, "storage")
        val storageBindingDirEmpty = storageBindingDir.listFiles()?.isEmpty() ?: true
        if (storageBindingDir.exists() && storageBindingDir.isDirectory && storageBindingDirEmpty) {
            storageBindingDir.delete()
        }
        storageBindingDir.mkdirs()

        // If users upgraded from a version before 2.5.14, the old sdcard binding should be removed
        // to increase clarity.
        val sdCardBindingDir = File(filesystemDir, "sdcard")
        val sdCardBindingDirEmpty = sdCardBindingDir.listFiles()?.isEmpty() ?: true
        if (sdCardBindingDir.exists() && sdCardBindingDir.isDirectory && sdCardBindingDirEmpty) {
            sdCardBindingDir.delete()
        }
    }
}