package com.puutaro.commandclick.proccess.qr

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import coil.load
import com.puutaro.commandclick.R
import com.puutaro.commandclick.common.variable.intent.extra.FileUploadExtra
import com.puutaro.commandclick.common.variable.intent.scheme.BroadCastIntentSchemeForCmdIndex
import com.puutaro.commandclick.common.variable.network.UsePort
import com.puutaro.commandclick.common.variable.path.UsePath
import com.puutaro.commandclick.common.variable.variables.QrLaunchType
import com.puutaro.commandclick.fragment.CommandIndexFragment
import com.puutaro.commandclick.fragment.EditFragment
import com.puutaro.commandclick.proccess.edit.edit_text_support_view.WithIndexListView
import com.puutaro.commandclick.service.FileUploadService
import com.puutaro.commandclick.util.BitmapTool
import com.puutaro.commandclick.util.CcPathTool
import com.puutaro.commandclick.util.FileSystems
import com.puutaro.commandclick.util.Intent.IntentVariant
import com.puutaro.commandclick.util.NetworkTool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object QrDialogMethod {

    private val fileUploadService = FileUploadService::class.java
    private var imageDialogObj: Dialog? = null

    fun launchPassDialog(
        fragment: Fragment,
        currentAppDirPath: String,
        fannelName: String,
    ) {
        val context = fragment.context
            ?: return

        val cpQrStr = makeCpFileQrStr(
            fragment,
            currentAppDirPath,
            fannelName,
        )

        val passQrLogoDrawable = QrLogo(fragment).createMonochrome(
            cpQrStr
        ) ?: return
        val intent = Intent(
            context,
            fileUploadService
        )
        intent.putExtra(
            FileUploadExtra.CURRENT_APP_DIR_PATH_FOR_FILE_UPLOAD.schema,
            currentAppDirPath
        )
        context.let {
            ContextCompat.startForegroundService(context, intent)
        }
        imageDialogObj = Dialog(
            context
        )
        imageDialogObj?.setContentView(
            R.layout.image_dialog_layout
        )
        val titleTextView = imageDialogObj?.findViewById<AppCompatTextView>(
            R.id.image_dialog_title
        )
        titleTextView?.text = makePassDialogTitle(
            currentAppDirPath,
            fannelName,
        )
        val imageContentsView = imageDialogObj?.findViewById<AppCompatImageView>(
            R.id.image_dialog_image
        )
        val qrBitMap = passQrLogoDrawable.toBitmap(1000, 1000)
        imageContentsView
            ?.setImageBitmap(qrBitMap)
        setShareButton(
            fragment,
            qrBitMap
        )
        val cancelButton = imageDialogObj?.findViewById<AppCompatImageButton>(
            R.id.image_dialog_ok
        )
        cancelButton?.setOnClickListener {
            imageDialogObj?.dismiss()
        }
        imageDialogObj?.setOnCancelListener {
            imageDialogObj?.dismiss()
        }
//        imageDialogObj?.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
        imageDialogObj?.window?.setGravity(Gravity.BOTTOM)
        imageDialogObj?.show()
    }

    private fun makePassDialogTitle(
        currentAppDirPath: String,
        fannelName: String,
    ): String {
        val isAppDirAdmin = currentAppDirPath.removeSuffix("/") ==
                UsePath.cmdclickAppDirAdminPath
        return when(isAppDirAdmin){
            true
            -> "Pass AppDir: ${CcPathTool.makeFannelRawName(fannelName)}"
            else
            -> "Pass: ${fannelName}"
        }
    }

    private fun makeCpFileQrStr(
        fragment: Fragment,
        currentAppDirPath: String,
        fannelName: String,
    ): String {
        val context = fragment.context
        val ipV4Address = NetworkTool.getIpv4Address(context)
        val fannelRawName = CcPathTool.makeFannelRawName(fannelName)
        val isAppDirAdmin = currentAppDirPath.removeSuffix("/") ==
                UsePath.cmdclickAppDirAdminPath
        return when(isAppDirAdmin){
            true -> {
                val appDirPathForCpFile = "${UsePath.cmdclickAppDirPath}/$fannelRawName"
                QrLaunchType.CpFile.prefix + listOf(
                    "${CpFileKey.ADDRESS.key}=${ipV4Address}:${UsePort.COPY_FANNEL_PORT.num}",
                    "${CpFileKey.PATH.key}=$appDirPathForCpFile",
                    "${CpFileKey.CURRENT_APP_DIR_PATH_FOR_SERVER.key}=${appDirPathForCpFile}"
                ).joinToString(";")
            }
            else -> {
                QrLaunchType.CpFile.prefix + listOf(
                    "${CpFileKey.ADDRESS.key}=${ipV4Address}:${UsePort.COPY_FANNEL_PORT.num}",
                    "${CpFileKey.PATH.key}=$currentAppDirPath/$fannelRawName",
                ).joinToString(";")
            }
        }
    }

    private fun setShareButton(
        fragment: Fragment,
        myBitmap: Bitmap,
    ){
        val shareButton = imageDialogObj?.findViewById<AppCompatImageButton>(
            R.id.image_dialog_share
        )
        shareButton?.setOnClickListener {
            execShare(
                fragment,
                myBitmap
            )
        }
    }


    fun execShare(
        fragment: Fragment,
        myBitmap: Bitmap
    ){
        val activity = fragment.activity
            ?: return
        FileSystems.removeDir(
            UsePath.cmdclickTempCreateDirPath
        )
        FileSystems.createDirs(
            UsePath.cmdclickTempCreateDirPath
        )
        val imageName = BitmapTool.hash(
            myBitmap
        ) + ".png"
        val file = File(
            UsePath.cmdclickTempCreateDirPath,
            imageName
        )
        FileOutputStream(file).use { stream ->
            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        IntentVariant.sharePngImage(
            file,
            activity
        )
    }


    fun execChange(
        fragment: Fragment,
        currentAppDirPath: String,
        fannelName: String,
        dialogObj: Dialog?,
    ){

        val context = fragment.context ?: return
        val fannelRawName = CcPathTool.makeFannelRawName(fannelName)
        val fannelDirName = CcPathTool.makeFannelDirName(fannelName)
        val qrLogoPath = "${currentAppDirPath}/$fannelDirName/${UsePath.qrPngRelativePath}"
        CoroutineScope(Dispatchers.IO).launch {
            val previousChecksum = withContext(Dispatchers.IO){
                FileSystems.checkSum(
                    currentAppDirPath,
                    fannelName
                )
            }
            withContext(Dispatchers.IO) {
                QrLogo(fragment).createAndSaveRnd(
                    QrMapper.onGitTemplate.format(fannelRawName),
                    currentAppDirPath,
                    fannelName,
                )
            }
            withContext(Dispatchers.IO){
                for(i in 1..20){
                    val updateChecksum = FileSystems.checkSum(
                        currentAppDirPath,
                        fannelName
                    )
                    if(
                        updateChecksum != previousChecksum
                    ) break
                    withContext(Dispatchers.Main) toast@ {
                        if(i != 1) return@toast
                        Toast.makeText(
                            context,
                            "Change..",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    delay(100)
                }
            }
            withContext(Dispatchers.Main) {
                dialogObj?.findViewById<AppCompatImageView>(
                    R.id.qr_logo_dialog_top_image
                )?.load(qrLogoPath)
                when(fragment) {
                    is CommandIndexFragment -> {
                        val indexfannelListUpdateIntent = Intent()
                        indexfannelListUpdateIntent.action =
                            BroadCastIntentSchemeForCmdIndex.UPDATE_INDEX_FANNEL_LIST.action
                        context.sendBroadcast(indexfannelListUpdateIntent)
                    }
                    is EditFragment -> {
                        WithIndexListView.listIndexListUpdateFileList(
                            fragment,
                            WithIndexListView.makeFileList()
                        )
                    }
                }
            }
        }
    }
}