package com.puutaro.commandclick.util.file

import android.graphics.Bitmap
import com.puutaro.commandclick.common.variable.variables.CommandClickScriptVariable
import com.puutaro.commandclick.common.variable.path.UsePath
import com.puutaro.commandclick.util.CcPathTool
import com.puutaro.commandclick.util.LogSystems
import org.apache.commons.io.FileUtils
import org.apache.commons.io.comparator.LastModifiedFileComparator
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.DigestInputStream
import java.security.MessageDigest


object FileSystems {

    fun createDirs(dirPath: String): Boolean {
        val dirPathObj = File(dirPath)
        try {
            if (dirPathObj.exists()) {
                return true
            }
            return dirPathObj.mkdirs()
        } catch(e: Exception) {
            LogSystems.stdErr(
                "${e.cause}, ${e.message}, ${e.stackTrace}"
            )
            return false
        }
    }

    fun createFiles(
        dirPath: String,
        fileName: String,
    ) {
        if(fileName == "-") return
        val filePath = File(dirPath, fileName)
        if(filePath.isDirectory) return
        if(filePath.exists()) return
        try {
            filePath.createNewFile()
        } catch (e: java.lang.Exception){
            LogSystems.stdErr(
                "${e.cause}, ${e.message}, ${e.stackTrace}"
            )
        }
    }

    fun writeFile(
        dirPath: String,
        fileName: String,
        contents: String
    ) {
        if(
            dirPath == "-"
            || dirPath.isEmpty()
        ) return
        if(
            fileName == "-"
            || fileName.isEmpty()
        ) return
        createDirs(dirPath)
        val filePath = File(dirPath, fileName)
        try {
            filePath.writeText(contents)
        } catch (e: java.lang.Exception){
            LogSystems.stdErr(
                "${e.cause}, ${e.message}, ${e.stackTrace}"
            )
        }
    }


    fun removeFiles(
        dirPath: String,
        fileName: String,
    ) {
        val filePath = File(dirPath, fileName)
        if(!filePath.exists()) return
        try {
            filePath.delete()
        } catch (e: java.lang.Exception){
            LogSystems.stdErr(
                "${e.cause}, ${e.message}, ${e.stackTrace}"
            )
        }
    }

    fun removeDir(
        dirPath: String,
    ) {
        val filePath = File(dirPath)
        if(!filePath.isDirectory) return
        try {
            filePath.deleteRecursively()
        } catch (e: java.lang.Exception){
            LogSystems.stdErr(
                "${e.cause}, ${e.message}, ${e.stackTrace}"
            )
        }
    }


    fun updateLastModified(
        dirPath: String,
        fileName: String
    ){
        if(
            fileName ==
            CommandClickScriptVariable.EMPTY_STRING
            || fileName ==
                    CommandClickScriptVariable.EMPTY_STRING +
            UsePath.SHELL_FILE_SUFFIX
        ) return
        val monitor1File = File(
            dirPath,
            fileName
        )
        if(
            !monitor1File.exists()
        ) {
           createFiles(
                dirPath,
                fileName
            )
            return
        }
        val time= System.currentTimeMillis()
        monitor1File.setLastModified(time)
    }

    fun updateWeekPastLastModified(
        dirPath: String,
        fileName: String
    ){
        if(
            fileName ==
            CommandClickScriptVariable.EMPTY_STRING
            || fileName ==
            CommandClickScriptVariable.EMPTY_STRING +
            UsePath.SHELL_FILE_SUFFIX
        ) return
        val monitor1File = File(
            dirPath,
            fileName
        )
        if(
            !monitor1File.exists()
        ) {
            createFiles(
                dirPath,
                fileName
            )
            return
        }
        val currentTime= System.currentTimeMillis()
        val weekPastMillis =  (24 * 7) * (60 * 60) * 1000
        val weekPastTime = currentTime - weekPastMillis
        monitor1File.setLastModified(weekPastTime)
    }

    fun sortedFiles(
        dirPath: String,
        reverse: String = String()
    ): List<String> {
        try {
            val dirfiles = File(dirPath).listFiles()
            if(dirfiles == null) return listOf(String())
            if (reverse.isEmpty()) {
                dirfiles.sortWith(LastModifiedFileComparator.LASTMODIFIED_COMPARATOR)
            } else {
                dirfiles.sortWith(LastModifiedFileComparator.LASTMODIFIED_REVERSE)
            }
            return dirfiles.map {
                it.name
            }
        }catch (e: java.lang.Exception){
            return emptyList()
        }
    }

    fun showDirList(
        dirPath: String
    ): List<String> {
        val directories =  File(dirPath).list {
                dir, name ->
            File(dir, name).isDirectory
        } ?: return emptyList()
        return directories.toList()
    }

    fun filterSuffixJsFiles(
        dirPath: String,
        reverse: String = String()
    ): List<String> {
        return sortedFiles(
            dirPath,
            reverse
        ).filter {
            it.endsWith(
                UsePath.JS_FILE_SUFFIX
            )
        }
    }

    fun getRecentAppDirPath(): String {
        val cmdclickAppDirAdminPath = UsePath.cmdclickAppDirAdminPath
        val jsSuffix = UsePath.JS_FILE_SUFFIX
        val cmdclickSystemAppDirNameJs = "${UsePath.cmdclickSystemAppDirName}${jsSuffix}"
        val recentAppDirName = filterSuffixJsFiles(
            cmdclickAppDirAdminPath,
            "on"
        ).filter { it != cmdclickSystemAppDirNameJs }.firstOrNull()?.removeSuffix(
            jsSuffix
        ) ?: UsePath.cmdclickDefaultAppDirName
        return File(
            UsePath.cmdclickAppDirPath,
            recentAppDirName,
        ).absolutePath
    }

    fun filterSuffixShellOrJsFiles(
        dirPath: String,
        reverse: String = String()
    ): List<String> {
        return sortedFiles(
            dirPath,
            reverse
        ).filter {
            it.endsWith(
                UsePath.SHELL_FILE_SUFFIX
            ) || it.endsWith(
                UsePath.JS_FILE_SUFFIX
            )
        }
    }

    fun filterSuffixShellOrJsOrHtmlFiles(
        dirPath: String,
        reverse: String = String()
    ): List<String> {
        return sortedFiles(
            dirPath,
            reverse
        ).filter {
            it.endsWith(
                UsePath.SHELL_FILE_SUFFIX
            ) || it.endsWith(
                UsePath.JS_FILE_SUFFIX
            ) || it.endsWith(
                UsePath.HTML_FILE_SUFFIX
            )
        }
    }


    fun copyDirectory(
        sourceDirPath: String,
        destiDirPath: String,
    ){
        val from = File(sourceDirPath)
        val to = File(destiDirPath)
        try {
            from.copyRecursively(to, true)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    fun moveDirectory(
        sourceDirPath: String,
        destiDirPath: String,
    ){
        copyDirectory(
            sourceDirPath,
            destiDirPath
        )
        removeDir(sourceDirPath)
    }

    fun moveFile(
        sourceShellFilePath: String,
        destiShellFilePath: String
    ){
        if(
            !File(sourceShellFilePath).isFile
        ) return
        try {
            copyFile(
                sourceShellFilePath,
                destiShellFilePath,
            )
            val removeFileObj = File(sourceShellFilePath)
            val removeFileParentDirPath = removeFileObj.parent
                ?: return
            removeFiles(
                removeFileParentDirPath,
                removeFileObj.name
            )
        } catch (e: Exception) {
            return
        }
    }

    fun copyFile(
        sourceShellFilePath: String,
        destiShellFilePath: String
    ){
        if(
            !File(sourceShellFilePath).isFile
        ) return
        try {
            Files.copy(
                File(sourceShellFilePath).toPath(),
                File(destiShellFilePath).toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (e: Exception) {
            LogSystems.stdErr(
                "${e.cause}, ${e.message}, ${e.stackTrace}"
            )
            return
        }
    }

    fun removeAndCreateDir(
        dirPath: String
    ){
        removeDir(
            dirPath
        )
        createDirs(
            dirPath
        )
    }

    fun updateFile(
        dirPath: String,
        fileName: String,
        updateCon: String,
    ){
        val currentCon =
            ReadText(
                dirPath,
                fileName,
            ).readText()
        writeFile(
            dirPath,
            fileName,
            "${currentCon}\n${updateCon}"
        )
    }

    fun writeFromByteArray(
        dirPath: String,
        fileName: String,
        byteArrayCon: ByteArray,
    ){
        val file = File(dirPath, fileName)
        removeFiles(dirPath, fileName)
        createDirs(dirPath)
        if(file.isDirectory) return
        FileUtils.writeByteArrayToFile(file, byteArrayCon)
    }

    fun savePngFromBitMap(
        dirPath: String,
        fileName: String,
        bitmap: Bitmap
    ){
        try {
            createDirs(
                dirPath
            )
            removeFiles(
                dirPath,
                fileName
            )
            val maskFile = File(
                dirPath,
                fileName,
            )
            val outputStream = FileOutputStream(maskFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            //outputStream.flush()
            outputStream.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            LogSystems.stdErr(e.stackTrace.toString())
        }
    }

    fun checkSum(
        dirPath: String,
        fileName: String
    ): String {
        val targetFile = File(dirPath, fileName)
        val fullFilePath = "$dirPath/$fileName"
        if(
            !targetFile.isFile
        ) return fullFilePath
        try {
            val inputStream = FileInputStream(targetFile)
            return DigestInputStream(
                inputStream,
                MessageDigest.getInstance("MD5")
            ).use { input ->
                val buffer = ByteArray(1024 * 1024) // buffer size は任意のサイズ (ここでは1MB)
                var read = 0
                while (read != -1) {
                    // ファイル末尾まで読み込む
                    read = input.read(buffer)
                }
                input.messageDigest.digest().joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception){
            LogSystems.stdErr(e.toString())
            return fullFilePath
        }
    }

    fun execCopyFileWithDir(
        srcFileObj: File,
        destiFilePathObjSrc: File,
        isOverride: Boolean = false
    ): String {
        val sourceFileDirPath = srcFileObj.parent
            ?: return String()
        val sourceFilePath = srcFileObj.absolutePath
        val destiFileDirPath = destiFilePathObjSrc.parent
            ?: return String()
        val destiFilePath = when(
            destiFilePathObjSrc.isFile
            && !isOverride
        ) {
            true -> {
                val fileName = destiFilePathObjSrc.name
                val fileRawName = CcPathTool.makeFannelRawName(fileName)
                val extend = CcPathTool.subExtend(destiFilePathObjSrc.name)
                "${destiFileDirPath}/" +
                        "${fileRawName}_${CommandClickScriptVariable.makeCopyPrefix()}${extend}"
            }
            else ->
                destiFilePathObjSrc.absolutePath
        }
        copyFile(
            sourceFilePath,
            destiFilePath
        )
        val destiFilePathObj = File(destiFilePath)
        val sourceFannelDir =
            CcPathTool.makeFannelDirName(
                srcFileObj.name
            )
        val sourceFannelDirPath = "${sourceFileDirPath}/${sourceFannelDir}"
        val destiFannelDir = CcPathTool.makeFannelDirName(
            destiFilePathObj.name
        )
        val destiFannelDirPath = "${destiFileDirPath}/${destiFannelDir}"
        copyDirectory(
            sourceFannelDirPath,
            destiFannelDirPath
        )
        return destiFilePath
    }

    fun moveFileWithDir(
        srcFileObj: File,
        destiFilePathObj: File,
        isOverride: Boolean = false
    ){
        execCopyFileWithDir(
            srcFileObj,
            destiFilePathObj,
            isOverride
        )
        removeFileWithDir(
            srcFileObj,
        )
    }

    fun removeFileWithDir(
        srcFileObj: File,
    ){
        val parentDirPath = srcFileObj.parent
            ?: return
        val fannelDirName = CcPathTool.makeFannelDirName(srcFileObj.name)
        val fannelDirPath = "${parentDirPath}/${fannelDirName}"
        removeDir(
            fannelDirPath
        )
        removeFiles(
            parentDirPath,
            srcFileObj.name,
        )
    }

    fun switchLastModify(
        fromFileObj: File,
        toFileObj: File,
    ){
        if(
            !fromFileObj.isFile
            || !toFileObj.isFile
        ) return
        val fromLastModify = fromFileObj.lastModified()
        val toLastModify = toFileObj.lastModified()
        fromFileObj.setLastModified(toLastModify)
        toFileObj.setLastModified(fromLastModify)
    }
}