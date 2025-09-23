package com.pdf.pdfreader.pdfviewer.editor.screen.base


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.ezteam.ezpdflib.activity.PdfDetailActivity
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.analytics.FirebaseAnalytics
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
import com.pdf.pdfreader.pdfviewer.editor.R
import com.pdf.pdfreader.pdfviewer.editor.common.LocaleManager
import com.pdf.pdfreader.pdfviewer.editor.dialog.DeleteDialog
import com.pdf.pdfreader.pdfviewer.editor.dialog.DetailFileDialog
import com.pdf.pdfreader.pdfviewer.editor.dialog.ReloadFileGuideDialog
import com.pdf.pdfreader.pdfviewer.editor.dialog.RenameDialog
import com.pdf.pdfreader.pdfviewer.editor.model.FileModel
import com.pdf.pdfreader.pdfviewer.editor.screen.rate.DialogRating
import com.pdf.pdfreader.pdfviewer.editor.screen.rate.DialogRatingState
import com.pdf.pdfreader.pdfviewer.editor.utils.AppUtils.Companion.PDF_DETAIL_EZLIB
import com.pdf.pdfreader.pdfviewer.editor.utils.FirebaseRemoteConfigUtil
import com.pdf.pdfreader.pdfviewer.editor.utils.createPdf.Config
import com.pdf.pdfreader.pdfviewer.editor.utils.createPdf.CreatePdf
import com.pdf.pdfreader.pdfviewer.editor.utils.createPdf.OnPDFCreatedInterface
import com.pdf.pdfreader.pdfviewer.editor.utils.createPdf.model.ImageToPDFOptions
import kotlinx.coroutines.launch
import office.file.ui.extension.openDocuments
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileOutputStream
import java.net.URLConnection

abstract class PdfBaseActivity<B : ViewBinding> : BaseActivity<B>(), IControl {
    companion object {
        const val EMAIL_FEEDBACK = "goldenboat.customerservices@gmail.com"
    }

    protected fun logEventBase(event: String, bundle: Bundle? = null) {
        FirebaseAnalytics.getInstance(this).logEvent(event, bundle ?: Bundle())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleManager.setLocale(this)
        super.onCreate(null)
    }

    override fun shareFile(fileModel: FileModel) {
        if (fileModel.file.exists()) {
            val uri = FileProvider.getUriForFile(
                this, applicationContext.packageName.toString() + ".provider",
                fileModel.file
            )
            val share = Intent()
            share.action = Intent.ACTION_SEND
            share.type = "application/pdf"
            share.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(share)
        }
    }
    fun shareFiles(fileModels: List<FileModel>) {
        val uris = ArrayList<Uri>()

        for (fileModel in fileModels) {
            if (fileModel.file.exists()) {
                val uri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.provider",
                    fileModel.file
                )
                uris.add(uri)
            }
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = "*/*" // Chia sẻ nhiều loại file
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Cấp quyền truy cập
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_file)))
    }

    override fun showDialogConfirm(title: String, message: String, onConfirm: () -> Unit) {
        DeleteDialog()
            .setTitle(title)
            .setMessage(message)
            .setOnConfirmListener { onConfirm() }
            .show(supportFragmentManager, "DeleteDialog")
    }

    open fun showDetailFile(fileModel: FileModel) {
        val detailPageDialog = DetailFileDialog(fileModel = fileModel);
        detailPageDialog.show(supportFragmentManager, "DetailPageDialog")
    }

    private fun showReloadGuide() {
        try {
            val swipesRefresh = findViewById<View>(R.id.swipe_refresh)
            ReloadFileGuideDialog(this, swipesRefresh).show()
            val showReloadGuideTime = PreferencesUtils.getInteger("SHOW_RELOAD_FILE_GUIDE_TIME",0)
            PreferencesUtils.putInteger("SHOW_RELOAD_FILE_GUIDE_TIME", showReloadGuideTime + 1)
            if (showReloadGuideTime >= 1) {
                TemporaryStorage.isShowedReloadGuideInThisSession = true
            }
        } catch (e: Exception) {
            Log.e("showReloadGuide", "Error show guide reloadingFile $e" )
        }
    }

    protected val handler = Handler(Looper.getMainLooper())

    protected val checkNoDialogToShowReloadGuideRunnable = object : Runnable {
        override fun run() {

            if (isAnyDialogFragmentShowing()) {
                Log.d("DialogChecker", "A dialog is currently showing")
                handler.postDelayed(this, FirebaseRemoteConfigUtil.getInstance().getDurationDelayShowingReloadGuide())
            } else {
                Log.d("DialogChecker", "No dialog is showing")
                showReloadGuide()
            }
        }
    }

    fun openFileFromSplash(fileModel: FileModel) {
        lifecycleScope.launch {
            openDocumentActivity(fileModel)
        }
    }

    override fun openFile(fileModel: FileModel) {
        if (fileModel.isAds) {
            return
        }

        handler.removeCallbacks(checkNoDialogToShowReloadGuideRunnable)

        lifecycleScope.launch {
            showAdsInterstitial(R.string.inter_filedetail) {
                showHideLoading(true)
                Handler(Looper.getMainLooper()).postDelayed({
                    openDocumentActivity(fileModel)
                }, 300)
                Handler(Looper.getMainLooper()).postDelayed({
                    showHideLoading(false)
                },3000)
            }
        }
    }
    protected fun showAdsInterstitial(idAds: Int,complete: () -> Unit) {
        if (IAPUtils.isPremium() || !Admob.getInstance().isLoadFullAds || !ConsentHelper.getInstance(this.applicationContext).canRequestAds()) {
            return complete()
        }

        val interCallback: AdCallback = object : AdCallback() {
            override fun onNextAction() {
                return complete()
            }
            override fun onAdFailedToLoad(var1: LoadAdError?) {
                Log.e("TAG", "onAdFailedToLoad: ${var1?.message}")
                return complete()
            }
        }
        Admob.getInstance().loadAndShowInter(this,
            getString(idAds),
            100, 8000, interCallback)
    }

    private fun openDocumentActivity(fileModel: FileModel) {
        logEventBase("file_size_bytes",
            Bundle().apply {
                putDouble("file_size_bytes", fileModel.size?.toDouble() ?: 0.0)
                putString("file_type", fileModel.fileExtension)
            }
        )
        TemporaryStorage.timeEnterPdfDetail++;
        if (FirebaseRemoteConfigUtil.getInstance().getPDFDetailType() == PDF_DETAIL_EZLIB
            && FilenameUtils.getExtension(fileModel.path) == "pdf") {
            PdfDetailActivity.start(this, fileModel.path,  fileModel.isFavorite, fileModel.isReadDone, true)
            return
        }
        openDocuments( path = fileModel.path,
            lastPage = if (fileModel.currentPage != -1) fileModel.currentPage else 0,
            isFavorite = fileModel.isFavorite ,
            isReadDone = fileModel.isReadDone)
    }


    private fun copyUriToTempFile(context: Context, uri: Uri): File? {
        try {
            val contentResolver = context.contentResolver
            val fileName = getFileName(context, uri) ?: return null
            val tempFile = File(context.cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        returnCursor?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    override fun openFile(uri: Uri) {
        val tempFile = copyUriToTempFile(this, uri)
        tempFile?.let {
            if (FirebaseRemoteConfigUtil.getInstance().getPDFDetailType() == PDF_DETAIL_EZLIB
                && FilenameUtils.getExtension(tempFile.path) == "pdf") {
                PdfDetailActivity.start(this, tempFile.path,  false, false, false)
                return@let
            }

            openDocuments( path = tempFile.path,
                lastPage = 0,
                isFavorite = false ,
                isReadDone = false,
                allowEdit = false)
        }
    }

    fun guessMimeTypeFromStream(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                URLConnection.guessContentTypeFromStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        // First try content resolver
        context.contentResolver.getType(uri)?.let { return it }

        // Then fallback to file extension
        MimeTypeMap.getFileExtensionFromUrl(uri.toString())?.let { ext ->
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.lowercase())
        }

        // Lastly guess from stream
        return guessMimeTypeFromStream(context, uri)
    }


    //    fun openUri(uri: Uri) {
//        if (getMimeType(this, uri) == "application/pdf") {
//            PdfDetailActivity.start(this, uri, true, 1000)
//        } else {
//            val intent = Intent(this, OpenFileActivity::class.java)
//            intent.setAction(Intent.ACTION_VIEW)
//            intent.data = uri
//            intent.putExtra("STARTED_FROM_EXPLORER", true)
//            intent.putExtra("START_PAGE", 0)
//            intent.putExtra("ALLOW_EDIT", false)
//            intent.putExtra("LAST_PAGE", 0)
////            sendBroadcast(
////                Intent().apply {
////                    action = BroadcastSubmodule.ACTION_RECENT
////                    putExtra(BroadcastSubmodule.PATH, path)
////                }
////            )
//
////            if (copyString != "") {
////                intent.putExtra("content", copyString)
////            }
//
////            if (list.isNotEmpty()) {
////                intent.putStringArrayListExtra("list_img", list)
////            }
//            Log.d("openDocuments", "startActivityForResult")
//            this.startActivityForResult(intent, 1000)
//        }
//    }
    override fun showRenameFile(fileName: String, complete: (String) -> Unit) {
        RenameDialog()
            .setFileName(fileName)
            .setOnRenameListener { newName ->
                complete(newName)
            }
            .show(supportFragmentManager, "RenameDialog")
    }

    override fun openAppOnStore() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        showHideLoading(false)
    }

    override fun sendFeedback() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(EMAIL_FEEDBACK))
            putExtra(Intent.EXTRA_SUBJECT,resources.getString(R.string.feedback_title))
            putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.feedback_content))
        }
        try {
            startActivity(Intent.createChooser(intent, "Choose your application to send mail"))
        } catch (e: Exception) {
        }
    }

    override fun shareApp() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Check out the App at: https://play.google.com/store/apps/details?id=$packageName"
        )
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    override fun showAppRating(isHardShow: Boolean, complete: () -> Unit) {
        DialogRating.ExtendBuilder(this)
            .setHardShow(isHardShow)
            .setListener { status ->
                when (status) {
                    DialogRatingState.RATE_BAD -> {
                        toast(resources.getString(R.string.thank_for_rate))
                        complete()
                    }
                    DialogRatingState.RATE_GOOD -> {
                        openAppOnStore()
                        complete()
                    }
                    DialogRatingState.COUNT_TIME -> complete()
                }
            }
            .build()
            .show()
    }

    private fun getHomePath(): String {
        return filesDir.path + "/PdfPro/"
    }

    private fun checkFileExist(fileName: String): Boolean {
        val path = "${getHomePath()}$fileName.pdf"
        val file = File(path)
        return file.exists()
    }

    fun createPdf(
        lstUri: ArrayList<String>,
        fileName: String,
        password: String? = null,
        size: String,
        onPDFCreated: OnPDFCreatedInterface
    ) {
        ImageToPDFOptions().apply {
            imagesUri = lstUri
            pageSize = when (size) {
                "A4" -> Config.PdfLib.DEFAULT_PAGE_SIZE
                "Letter" -> "LETTER"
                else -> Config.PdfLib.DEFAULT_PAGE_SIZE
            }
            imageScaleType = Config.PdfLib.IMAGE_SCALE_TYPE_ASPECT_RATIO
            pageColor = resources.getColor(R.color.white)
            if (!TextUtils.isEmpty(password)) {
                isPasswordProtected = true
                setPassword(password)
            }
            if (!checkFileExist(fileName)) {
                outFileName = fileName
                CreatePdf(this@PdfBaseActivity, this, getHomePath(), onPDFCreated).execute()
            } else {
                android.app.AlertDialog.Builder(this@PdfBaseActivity)
                    .setMessage(getString(R.string.replace_old_file))
                    .setPositiveButton(R.string.ok) { _, _ ->
                        outFileName = fileName
                        CreatePdf(
                            this@PdfBaseActivity,
                            this,
                            getHomePath(),
                            onPDFCreated
                        ).execute()
                    }
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
            }
        }
    }

}