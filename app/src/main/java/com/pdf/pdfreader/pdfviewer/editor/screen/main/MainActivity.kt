package com.pdf.pdfreader.pdfviewer.editor.screen.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.ezteam.baseproject.adapter.BasePagerAdapter
import com.ezteam.baseproject.extensions.calculateTime
import com.ezteam.baseproject.extensions.launchActivity
import com.ezteam.baseproject.utils.PathUtils
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.ezteam.ezpdflib.listener.BroadcastSubmodule
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.navigation.NavigationBarView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.pdf.pdfreader.pdfviewer.editor.R
import com.pdf.pdfreader.pdfviewer.editor.common.BottomTab
import com.pdf.pdfreader.pdfviewer.editor.common.FileTab
import com.pdf.pdfreader.pdfviewer.editor.common.FunctionState
import com.pdf.pdfreader.pdfviewer.editor.common.PresKey
import com.pdf.pdfreader.pdfviewer.editor.common.SortState
import com.pdf.pdfreader.pdfviewer.editor.databinding.ActivityMainBinding
import com.pdf.pdfreader.pdfviewer.editor.dialog.AddToHomeDialog
import com.pdf.pdfreader.pdfviewer.editor.dialog.DefaultReaderRequestDialog
import com.pdf.pdfreader.pdfviewer.editor.dialog.SortDialog
import com.pdf.pdfreader.pdfviewer.editor.model.FileModel
import com.pdf.pdfreader.pdfviewer.editor.schedule.OneTimeScheduleWorker
import com.pdf.pdfreader.pdfviewer.editor.screen.PolicyActivity
import com.pdf.pdfreader.pdfviewer.editor.screen.base.CurrentStatusAdsFiles
import com.pdf.pdfreader.pdfviewer.editor.screen.base.PdfBaseActivity
import com.pdf.pdfreader.pdfviewer.editor.screen.create.BottomSheetCreatePdf
import com.pdf.pdfreader.pdfviewer.editor.screen.create.CreateSuccessActivity
import com.pdf.pdfreader.pdfviewer.editor.screen.file.ListAllFileFragment
import com.pdf.pdfreader.pdfviewer.editor.screen.file.ListFileExcelFragment
import com.pdf.pdfreader.pdfviewer.editor.screen.file.ListFilePPTFragment
import com.pdf.pdfreader.pdfviewer.editor.screen.file.ListFilePdfFragment
import com.pdf.pdfreader.pdfviewer.editor.screen.file.ListFileWordFragment
import com.pdf.pdfreader.pdfviewer.editor.screen.func.BottomSheetFavoriteFunction
import com.pdf.pdfreader.pdfviewer.editor.screen.func.BottomSheetMenuFunction
import com.pdf.pdfreader.pdfviewer.editor.screen.func.BottomSheetRecentFunction
import com.pdf.pdfreader.pdfviewer.editor.screen.language.LanguageActivity
import com.pdf.pdfreader.pdfviewer.editor.screen.search.SelectMultipleFilesActivity
import com.pdf.pdfreader.pdfviewer.editor.screen.search.SearchFileActivity
import com.pdf.pdfreader.pdfviewer.editor.screen.search.SettingActivity
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.ezteam.baseproject.iapLib.v3.BillingProcessor
import com.ezteam.baseproject.iapLib.v3.PurchaseInfo
import com.ezteam.baseproject.utils.IAPUtils
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.nlbn.ads.banner.BannerPlugin
import com.nlbn.ads.util.ConsentHelper
import com.pdf.pdfreader.pdfviewer.editor.BuildConfig
import com.pdf.pdfreader.pdfviewer.editor.dialog.DefaultReaderUninstallDialog
import com.pdf.pdfreader.pdfviewer.editor.dialog.SatisfactionDialog
import com.pdf.pdfreader.pdfviewer.editor.dialog.UpdateDialog
import com.pdf.pdfreader.pdfviewer.editor.dialog.UpdateDownloadedDialog
import com.pdf.pdfreader.pdfviewer.editor.notification.NotificationManager
import com.pdf.pdfreader.pdfviewer.editor.screen.overlay.ClearDefaultReaderOverlayActivity
import com.pdf.pdfreader.pdfviewer.editor.screen.iap.IapActivity
import com.pdf.pdfreader.pdfviewer.editor.service.NotificationForegroundService
import com.pdf.pdfreader.pdfviewer.editor.utils.AppUtils
import com.pdf.pdfreader.pdfviewer.editor.utils.FileSaveManager
import com.pdf.pdfreader.pdfviewer.editor.utils.FirebaseRemoteConfigUtil
import com.pdf.pdfreader.pdfviewer.editor.utils.createPdf.OnPDFCreatedInterface
import com.pdf.pdfreader.pdfviewer.editor.widgets.Widget1
import com.pdf.pdfreader.pdfviewer.editor.widgets.Widget2
import com.pdf.pdfreader.pdfviewer.editor.widgets.Widget3
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import office.file.ui.extension.visible
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.ezteam.baseproject.extensions.hasExtraKeyContaining
import com.pdf.pdfreader.pdfviewer.editor.screen.iap.IapActivityV2
import com.pdf.pdfreader.pdfviewer.editor.screen.reloadfile.FeatureRequestActivity
import com.pdf.pdfreader.pdfviewer.editor.screen.reloadfile.ReloadLoadingActivity


private const val ALL_FILES_FRAGMENT_INDEX = 0
private const val PDF_FILES_FRAGMENT_INDEX = 1
private const val WORD_FILES_FRAGMENT_INDEX = 2
private const val PPT_FILES_FRAGMENT_INDEX = 3
private const val EXCEL_FILES_FRAGMENT_INDEX = 4

class MainActivity : PdfBaseActivity<ActivityMainBinding>() {
    private val TAG = "MainActivity"
    private val viewModel by inject<MainViewModel>()
    private lateinit var adapter: BasePagerAdapter
    private val myBroadcastReceiver: BroadcastSubmodule by lazy {
        BroadcastSubmodule()
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private fun logEvent(event: String) {
        firebaseAnalytics.logEvent(event, Bundle())
    }
    companion object {
        const val CODE_ACTION_OPEN_DOCUMENT_FILE = 1000
        const val CODE_CHOOSE_IMAGE = 10001
        const val TAG_WORK = "Notification"

        fun start(activity: FragmentActivity) {
            val pkg = activity.packageName

            activity.intent.data?.let {
                activity.intent.apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    setClass(activity, MainActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: activity.intent.hasExtraKeyContaining(pkg).let { hasKey ->
                if (hasKey) {
                    activity.intent.apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        setClass(activity, MainActivity::class.java)
                    }
                    activity.startActivity(activity.intent)
                } else {
                    val intent = Intent(activity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    activity.startActivity(intent)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

//        AppOpenManager.getInstance().enableAppResume()
        handleIntentToMove()
        super.onCreate(savedInstanceState)
        handleIntentToShowUI()
    }

    /*
 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
        } else {
            requestPermission(
                {
                    result(it)
                },
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    * */
    private fun checkNotificationPermissionToShowUI() {
        val layoutParams = binding.recentlyAddedSection.layoutParams as ViewGroup.MarginLayoutParams
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            binding.notificationWarningSection.visibility = View.GONE
            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen._4sdp)
        } else {
            binding.notificationWarningSection.visibility = View.VISIBLE
            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen._8sdp)
        }
        binding.recentlyAddedSection.layoutParams = layoutParams
    }
    private fun checkFeatureRequestToShowUI() {
        val layoutParams = binding.featureRequestSection.layoutParams as ViewGroup.MarginLayoutParams

        var hasStoragePermission = true
        var hasNotificationPermission = true
        val timeEnterFiles = PreferencesUtils.getInteger(com.ezteam.baseproject.utils.PresKey.TIME_ENTER_FILES, 0)
        val notSubmitFeatureRequest = PreferencesUtils.getBoolean("NOT_SUBMIT_FEATURE_REQUEST", true)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            hasStoragePermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            hasStoragePermission = Environment.isExternalStorageManager()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = ContextCompat.checkSelfPermission(
                    this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        }
        Log.d("DEBUG", "hasStoragePermission=$hasStoragePermission, hasNotificationPermission=$hasNotificationPermission, timeEnterFiles=$timeEnterFiles")
        if ( FirebaseRemoteConfigUtil.getInstance().isRequestFeatureSettingOnOff()
            && hasStoragePermission
            && hasNotificationPermission
            && timeEnterFiles > 2
            && notSubmitFeatureRequest) {
            binding.featureRequestSection.visibility = View.VISIBLE
            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen._4sdp)
        } else {
            binding.featureRequestSection.visibility = View.GONE
            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen._4sdp)
        }

        binding.featureRequestSection.layoutParams = layoutParams
    }

    private fun checkStoragePermissionToShowUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.layoutPermission.visibility =
                if (Environment.isExternalStorageManager()) View.GONE else View.VISIBLE

        } else {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE

            val isPermissionGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            binding.layoutPermission.visibility = if (isPermissionGranted) View.GONE else View.VISIBLE
        }
    }
    private fun loadBannerAds(){
        if (Admob.getInstance().isLoadFullAds && !IAPUtils.isPremium()) {
            binding.bannerContainer.visible()
            val config = BannerPlugin.Config()
            config.defaultRefreshRateSec = 30
            config.defaultCBFetchIntervalSec = 30
            config.defaultAdUnitId = getString(R.string.banner_navbar)
            config.defaultBannerType = BannerPlugin.BannerType.Adaptive
            Admob.getInstance().loadBannerPlugin(
                this,
                findViewById(R.id.banner_container),
                findViewById(R.id.shimmer_container_banner),
                config
            )
        } else binding.bannerContainer.visibility = View.GONE

    }
//    private fun loadNativeNomedia() {
//    if (IAPUtils.isPremium()) {
//        binding.layoutNative.visibility = View.GONE
//        return
//    }

//        if (SystemUtils.isInternetAvailable(this)) {
//            binding.layoutNative.visibility = View.VISIBLE
//            val loadingView = LayoutInflater.from(this)
//                .inflate(R.layout.ads_native_loading_short, null)
//            binding.layoutNative.removeAllViews()
//            binding.layoutNative.addView(loadingView)
//
//            val callback = object : NativeCallback() {
//                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
//                    super.onNativeAdLoaded(nativeAd)
//
//                    val layoutRes = R.layout.ads_native_bot_no_media_short
//                    val adView = LayoutInflater.from(this@MainActivity)
//                        .inflate(layoutRes, null) as NativeAdView
//
//                    binding.layoutNative.removeAllViews()
//                    binding.layoutNative.addView(adView)
//
//                    // Gán dữ liệu quảng cáo vào view
//                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
//                }
//
//                override fun onAdFailedToLoad() {
//                    super.onAdFailedToLoad()
//                    binding.layoutNative.visibility = View.GONE
//                }
//            }
//
//            Admob.getInstance().loadNativeAd(
//                applicationContext,
//                getString(R.string.native_navbar),
//                callback
//            )
//        } else {
//            binding.layoutNative.visibility = View.GONE
//        }
//    }

    private var shouldLoadAdsMiddleFiles = true
    // if user open a file from outside app => Main Activity => File Detail (shouldn't load ads middle files in this case)

    private fun loadNativeAdsMiddleFiles() {
        if (!IAPUtils.isPremium() && SystemUtils.isInternetAvailable(this)) {
            viewModel.updateAdsFilesStatus(CurrentStatusAdsFiles(true, null))
            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    if (nativeAd != null) {
                        viewModel.updateAdsFilesStatus(CurrentStatusAdsFiles(true, nativeAd))
                    } else {
                        onAdFailedToLoad()
                    }
                }

                override fun onAdFailedToLoad() {
                    viewModel.updateAdsFilesStatus(CurrentStatusAdsFiles(false, null))
                }
            }

            Admob.getInstance().loadNativeAd(
                applicationContext,
                getString(R.string.native_between_files_home),
                callback
            )
        } else {
            viewModel.updateAdsFilesStatus(CurrentStatusAdsFiles(false, null))
        }
    }
    private lateinit var appUpdateManager : AppUpdateManager

    private var startDownload = false

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytes = state.bytesDownloaded()
                val total = state.totalBytesToDownload()
                Log.d("Update", "Downloading: $bytes/$total")
                if (!startDownload) {
                    startDownload = true
                    val intent = Intent(this, NotificationForegroundService::class.java).apply {
                        action = "${packageName}.WAIT_UPDATE_DOWNLOADED"
                    }
                    ContextCompat.startForegroundService(this, intent)
                    Toast.makeText(
                        this,
                        getString(R.string.sodk_editor_downloading),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            InstallStatus.DOWNLOADED -> {
                Log.d("Update", "Download complete")
                showRestartDialog(appUpdateManager)
                NotificationManager(this@MainActivity).showUpdateDownloadedNotification()
            }

            InstallStatus.INSTALLED -> {
                Log.d("Update", "Installed")
            }

            InstallStatus.FAILED -> {
                Log.e("Update", "Update failed")
            }

            else -> Unit
        }
    }

    private lateinit var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    private fun checkForUpdates() {
        val currentVersionCode = BuildConfig.VERSION_CODE
        val firebaseRemoteConfigUtil = FirebaseRemoteConfigUtil.getInstance()
        firebaseRemoteConfigUtil.fetchRemoteConfig { success ->
            Log.i("MainActivity", "Fetch remote config success: $success")
            if (success) {
                val minVersionCode = firebaseRemoteConfigUtil.getMinVersionCode()
                if (currentVersionCode < minVersionCode) {

                    appUpdateManager.appUpdateInfo
                        .addOnSuccessListener { info ->
                            Log.i(TAG, "Init View OK to check for updates: ${info.updateAvailability()}")
                            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                                val updateDialog = UpdateDialog()
                                updateDialog.appVersion = firebaseRemoteConfigUtil.getLatestVersion()
                                updateDialog.forceUpdate = firebaseRemoteConfigUtil.isForceUpdateRequired();
                                updateDialog.updateFeatures = firebaseRemoteConfigUtil.getUpdateFeatures()
                                updateDialog.userCount = firebaseRemoteConfigUtil.getUpdateUserCount()
                                updateDialog.listener = {
                                    appUpdateManager.startUpdateFlowForResult(
                                        info,
                                        activityResultLauncher,
                                        AppUpdateOptions.newBuilder(if (updateDialog.forceUpdate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE)
                                            .setAllowAssetPackDeletion(true)
                                            .build())
                                    if (updateDialog.forceUpdate) AppOpenManager.getInstance().disableAppResume()
                                }
                                try {
                                    updateDialog.show(supportFragmentManager, UpdateDialog::class.java.name)
                                } catch (e : Exception)
                                {
                                    Log.e("MainActivity", "Error showing update dialog: ${e.message}")
                                }
                            }
                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                getString(R.string.app_update_fail),
                                Toast.LENGTH_LONG
                            ).show();
                            Log.e(TAG, "Init View Failed to check for updates: ${e.message}")
                        }
                }
            } else {
                // Handle fetch config failure
            }
        }
    }

    override fun initView() {
        Log.d(TAG, "initView")
        appUpdateManager = AppUpdateManagerFactory.create(this)
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            // handle callback
            if (result.resultCode != RESULT_OK) {
                Log.d(TAG,"Update flow failed! Result code: " + result.resultCode);
                if(FirebaseRemoteConfigUtil.getInstance().isForceUpdateRequired()) {
                    Log.d(TAG, "Force update cancelled.")
                    Toast.makeText(this, getString(R.string.app_update_critical), Toast.LENGTH_LONG).show()
                    checkForUpdates()
                }

            } else {
                appUpdateManager.registerListener(installStateUpdatedListener)
            }
        }
        checkForUpdates()
//        loadAds()
        initFavoriteListener()
//        showAppRating(false) {}
        binding.toolbar.tvAll.text =  getString(R.string.all)
        binding.toolbar.tvWord.text =  getString(R.string.word)
        binding.toolbar.tvPpt.text =  getString(R.string.ppt)
        binding.toolbar.tvExcel.text =  getString(R.string.excel)
        binding.toolbar.tvPdf.text =  getString(R.string.pdf)
        adapter = BasePagerAdapter(supportFragmentManager, ALL_FILES_FRAGMENT_INDEX)
        adapter.addFragment(ListAllFileFragment(viewModel.allFilesLiveData), ListAllFileFragment::class.java.name)
        adapter.addFragment(ListFilePdfFragment(viewModel.pdfFilesLiveData), ListFilePdfFragment::class.java.name)
        adapter.addFragment(ListFileWordFragment(viewModel.wordFilesLiveData), ListFileWordFragment::class.java.name)
        adapter.addFragment(ListFilePPTFragment(viewModel.pptFilesLiveData), ListFilePPTFragment::class.java.name)
        adapter.addFragment(ListFileExcelFragment(viewModel.excelFilesLiveData), ListFileExcelFragment::class.java.name)
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 5
//        adsCallbackListeners.add(adapter.getItem(ALL_FILES_FRAGMENT_INDEX) as IAdsControl)
//        adsCallbackListeners.add(adapter.getItem(PDF_FILES_FRAGMENT_INDEX) as IAdsControl)
//        adsCallbackListeners.add(adapter.getItem(WORD_FILES_FRAGMENT_INDEX) as IAdsControl)
//        adsCallbackListeners.add(adapter.getItem(EXCEL_FILES_FRAGMENT_INDEX) as IAdsControl)
//        adsCallbackListeners.add(adapter.getItem(PPT_FILES_FRAGMENT_INDEX) as IAdsControl)

        // Kotlin

    }

    private fun handleIntentToShowUI() {
        if(intent.getBooleanExtra("${this.packageName}.isFromWidget", false)
            || intent.getBooleanExtra("${this.packageName}.isFromReloadFileSuccess", false)) {
            Log.d(TAG, "isFromWidget open: " + intent.getIntExtra("${this.packageName}.whereToOpen", -1))
            when (intent.getIntExtra("${this.packageName}.whereToOpen", -1)) {
                R.id.ivRecent -> {
                    handleUIBaseOnBottomTab(R.id.navigation_recent)
                    handleUIBaseOnFileTab(binding.toolbar.tvAll)
                }
                R.id.ivBookmarks -> {
                    handleUIBaseOnBottomTab(R.id.navigation_favorite)
                    handleUIBaseOnFileTab(binding.toolbar.tvAll)
                }
                R.id.ivPdf -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                    handleUIBaseOnFileTab(binding.toolbar.tvPdf)
                    binding.viewPager.setCurrentItem(PDF_FILES_FRAGMENT_INDEX, false)
                }
                R.id.ivWord-> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                    handleUIBaseOnFileTab(binding.toolbar.tvWord)
                    binding.viewPager.setCurrentItem(WORD_FILES_FRAGMENT_INDEX, false)
                }
                R.id.ivExcel -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                    handleUIBaseOnFileTab(binding.toolbar.tvExcel)
                    binding.viewPager.setCurrentItem(EXCEL_FILES_FRAGMENT_INDEX, false)
                }
                R.id.ivPpt -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                    handleUIBaseOnFileTab(binding.toolbar.tvPpt)
                    binding.viewPager.setCurrentItem(PPT_FILES_FRAGMENT_INDEX, false)
                }
                R.id.searchBar -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                    handleUIBaseOnFileTab(binding.toolbar.tvAll)
                    SearchFileActivity.start(this)
                }
                R.id.ivEdit,  R.id.latest_file_item_1 -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                    handleUIBaseOnFileTab(binding.toolbar.tvAll)
                    lifecycleScope.launch(Dispatchers.IO) {
                        val latestFiles = viewModel.getLatestFile()

                        if (latestFiles.isNotEmpty()) {
//                            openFile(fileModel = latestFiles[0])
                            openFileFromSplash(latestFiles[0])
                        }
                    }
                }
                R.id.latest_file_item_2 -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                    handleUIBaseOnFileTab(binding.toolbar.tvAll)
                    lifecycleScope.launch(Dispatchers.IO) {
                        val latestFiles = viewModel.getLatestFile()

                        if (latestFiles.isNotEmpty()) {
//                            openFile(latestFile[1])
                            openFileFromSplash(latestFiles[1])

                        }
                    }
                }
                else -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                    handleUIBaseOnFileTab(binding.toolbar.tvAll)
                    binding.viewPager.setCurrentItem(ALL_FILES_FRAGMENT_INDEX, false)
                }
            }
        } else {
            when (viewModel.currentFileTab.value) {
                FileTab.ALL_FILE -> {
                    handleUIBaseOnFileTab(binding.toolbar.tvAll)
                }
                FileTab.PDF -> {
                    handleUIBaseOnFileTab(binding.toolbar.tvPdf)
                }
                FileTab.WORD -> {
                    handleUIBaseOnFileTab(binding.toolbar.tvWord)
                }
                FileTab.EXCEL -> {
                    handleUIBaseOnFileTab(binding.toolbar.tvExcel)
                }
                FileTab.PPT -> {
                    handleUIBaseOnFileTab(binding.toolbar.tvPpt)
                }
                else -> {
                    handleUIBaseOnFileTab(binding.toolbar.tvAll)
                }
            }
            when(viewModel.currentBottomTab.value) {
                BottomTab.HOME -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                }
                BottomTab.RECENT -> {
                    handleUIBaseOnBottomTab(R.id.navigation_recent)
                }
                BottomTab.FAVORITE -> {
                    handleUIBaseOnBottomTab(R.id.navigation_favorite)
                }
                else -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                }
            }
        }
    }

    private var isGoingToSettingToClearDefault = false

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
        if (isGoingToSettingToClearDefault){
            Log.i(TAG, "isGoingToSetting")
            startActivity(Intent(this@MainActivity, ClearDefaultReaderOverlayActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            })
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_OPEN,
                    0,  // enter: instant
                    0   // exit: instant
                );
            } else {
                overridePendingTransition(0, 0);
            }
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        }

    }
    override fun onResume() {
        super.onResume()

        binding.swipeRefresh.isRefreshing = false

        IAPUtils.initAndRegister(this, AppUtils.PUBLIC_LICENSE_KEY, object : BillingProcessor.IBillingHandler {
            override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
                Log.d(TAG, "onProductPurchased: $productId")
            }

            override fun onBillingInitialized() {
                Log.d(TAG, "onBillingInitialized")
                IAPUtils.loadOwnedPurchasesFromGoogleAsync {
                    val isPremium = IAPUtils.isPremium()
                    binding.toolbar.tvTitle.text =  handleAppNameSpannable(showIcon = isPremium)
                    binding.toolbar.ivIap.visibility = if (isPremium) View.GONE else View.VISIBLE

                    if (isPremium) {
                        binding.bannerContainer.visibility = View.GONE
                    } else {
                        val consentHelper = ConsentHelper.getInstance(applicationContext)

                        if (!TemporaryStorage.isObtainConsent) {
                            if (!consentHelper.canLoadAndShowAds()) consentHelper.reset()
                            consentHelper.obtainConsentAndShow(this@MainActivity){
                                TemporaryStorage.isObtainConsent = true
                            }
                        }
                    }
                }

                IAPUtils.unregisterListener(this)
            }

            override fun onBillingError(errorCode: Int, errorMessage: Throwable?) {
                Log.e(TAG, "onBillingError: $errorCode, $errorMessage")
            }

            override fun onPurchaseHistoryRestored() {
                Log.d(TAG, "onPurchaseHistoryRestored")
            }
        })

        object : CountDownTimer(500, 500) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                AppOpenManager.getInstance().enableAppResume()
            }
        }.start()

        val intent = Intent(this, NotificationForegroundService::class.java).apply {
            action = "${packageName}.STOP_WAIT_UPDATE_DOWNLOADED"
        }
        ContextCompat.startForegroundService(this, intent)

        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                Log.i(TAG, "onResume OK to check for updates: ${appUpdateInfo.updateAvailability()}")
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    if (FirebaseRemoteConfigUtil.getInstance().isForceUpdateRequired()) {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            activityResultLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build())
                        AppOpenManager.getInstance().disableAppResume()
                    } else {
                        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                            showRestartDialog(appUpdateManager)
                        }
                    }
                }
                Log.i(TAG, "onResume OK to check for updates: ${appUpdateInfo.updateAvailability()}")
            }.addOnFailureListener { e ->
                Log.e(TAG, "onResume Failed to check for updates: ${e.message}")
            }


        if (TemporaryStorage.timeEnterPdfDetail == 2 &&
            !TemporaryStorage.isShowedAddToHoneDialog &&
            (AppUtils.isWidgetNotAdded(this, Widget1::class.java) ||
                    AppUtils.isWidgetNotAdded(this, Widget2::class.java) ||
                    AppUtils.isWidgetNotAdded(this, Widget3::class.java))) {

            TemporaryStorage.isShowedAddToHoneDialog = true
            val dialog = AddToHomeDialog()
            dialog.show(this.supportFragmentManager, "AddToHomeDialog")
        }

        checkStoragePermissionToShowUI()
        checkNotificationPermissionToShowUI()
        checkFeatureRequestToShowUI()
       // loadNativeNomedia()
        loadBannerAds()
        if (shouldLoadAdsMiddleFiles) {
            loadNativeAdsMiddleFiles()
        } else {
            shouldLoadAdsMiddleFiles = true
        }
        val timeEnterApp = PreferencesUtils.getInteger(PresKey.TIME_ENTER_APP, 1)
        val notSubmitFeedback = PreferencesUtils.getBoolean("NOT_SUBMIT_FEEDBACK", true)
        if ( FirebaseRemoteConfigUtil.getInstance().isFeedbackSettingOnOff() // only show when remote config is on
            && (timeEnterApp == 1 || timeEnterApp % 3 == 0 || BuildConfig.DEBUG)  // only show dialog in first time or every 3 times
            && TemporaryStorage.timeEnterPdfDetail == 1  // only show dialog after user open pdf detail 1st time
            && notSubmitFeedback // only show dialog if user hasn't submitted feedback
            && !TemporaryStorage.isShowSatisfiedDialogInThisSession) { // only show dialog if it hasn't been shown in this session
            TemporaryStorage.isShowSatisfiedDialogInThisSession = true
            val satisfactionDialog = SatisfactionDialog()
            satisfactionDialog.show(supportFragmentManager, SatisfactionDialog::class.java.name)
        }
        if (!TemporaryStorage.isShowedDefaultReaderRequestDialogInThisSession || isGoingToSettingToClearDefault) {
            TemporaryStorage.isShowedDefaultReaderRequestDialogInThisSession = true
            isGoingToSettingToClearDefault = false
            Log.d(TAG, "time enter app = $timeEnterApp")
            if (timeEnterApp == 1 || timeEnterApp % 3 == 0 || BuildConfig.DEBUG) {
                val defaultPdfViewerResolveInfo = getDefaultPdfViewerClass()
                Log.i("DefaultReader", "defaultPdfViewer: $defaultPdfViewerResolveInfo")
                if (defaultPdfViewerResolveInfo?.activityInfo == null || defaultPdfViewerResolveInfo.activityInfo.name.contains("internal.app.ResolverActivity")) { // default reader isn't set => show dialog to set default
                    val dialog = DefaultReaderRequestDialog();
                    dialog.show(this.supportFragmentManager, "RequestDefaultReaderDialog")
                } else if(!defaultPdfViewerResolveInfo.activityInfo.name.contains(packageName) ) { // default reader is set but not our app => show dialog to clear default
                    val fragmentManager = supportFragmentManager
                    val existingDialog = fragmentManager.findFragmentByTag("DefaultReaderUninstallDialog")
                    if (existingDialog == null) {
                        val dialog = DefaultReaderUninstallDialog()
                        dialog.defaultPdfViewer = defaultPdfViewerResolveInfo
                        dialog.listener = {
                            isGoingToSettingToClearDefault = true
                        }
                        dialog.show(fragmentManager, "DefaultReaderUninstallDialog")
                    }
                } else { // default reader is our app => do nothing
                    Log.d("DefaultReader", "defaultPdfViewer: $defaultPdfViewerResolveInfo")
                    logEvent("app_default_reader")
                }
            }
        }
        val showReloadFileGuideTime = PreferencesUtils.getInteger("SHOW_RELOAD_FILE_GUIDE_TIME",0)
        if (showReloadFileGuideTime < FirebaseRemoteConfigUtil.getInstance().getTimeShowingReloadGuide() // only show guide 3 times
            && !TemporaryStorage.isShowedReloadGuideInThisSession // only show dialog if it hasn't been shown in this session
            && isAcceptManagerStorage()) { // only show guide if user has accepted storage permission
            handler.postDelayed(checkNoDialogToShowReloadGuideRunnable,
                FirebaseRemoteConfigUtil.getInstance().getDurationDelayShowingReloadGuide()) // delay 5s if it's not first time
        }
    }

    override fun onRestart() {
        super.onRestart()
//        if (PreferencesHelper.getBoolean(
//                PreferencesHelper.SHOW_SELECT_LANGUAGE_FIRST,
//                true
//            ) && intent.data == null
//        ) {
//            launchActivity<LanguageActivity> {}
//        }
    }

    private fun handleIntentToMove() {
        shouldLoadAdsMiddleFiles = true
        if (isFinishing) return
        if(intent.getBooleanExtra("${this.packageName}.isToSetDefaultReader", false)) {
            Log.d(TAG, "isToSetDefaultReader")
            return
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (!Environment.isExternalStorageManager()) {
//                toast(getString(R.string.content_access))
//                return;
//            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                toast(getString(R.string.content_access))
                return;
            }
        }


        val isFromNotification = intent.getBooleanExtra("${packageName}.isFromNotification", false)
        Log.d(TAG, "handleScreen isFromNotification: $isFromNotification")
        if(isFromNotification) {
            val filePath = intent.getStringExtra("${this.packageName}.filePath")
            Log.d(TAG, "handleScreen filePath: $filePath")
            if (!filePath.isNullOrEmpty()) {
                if (filePath.substringAfterLast(".") == "png" || filePath.substringAfterLast(".") == "jpg"
                    || filePath.substringAfterLast(".") == "jpeg") {
                    Log.d(TAG, "handleScreen open image: $filePath")
                    val listUri = ArrayList<String>();
                    try {
                        val file = File(filePath)
                        if (file.exists()) {
                            try {
                                val uri = PathUtils.getMediaStoreUri(this, file)
                                listUri.add(uri.toString())
                                showPopupCreatePdf(listUri)
                                shouldLoadAdsMiddleFiles = false
                            } catch (e: Exception) {
                                Log.e(TAG, "Error creating file: $filePath", e)
                            }
                        } else {
                            Log.e(TAG, "File does not exist: $filePath")
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "Error getting URI for file: $filePath", e)
                    }
                } else {
                    Log.d(TAG, "handleScreen open document: $filePath")
                    lifecycleScope.launch(Dispatchers.IO) {
                        val fileModel = viewModel.getFileModelByPath(filePath)
//                        openFile(fileModel)
                        openFileFromSplash(fileModel)
                        shouldLoadAdsMiddleFiles = false
                    }
                }

            }
            intent.removeExtra("${this.packageName}.isFromNotification")
            return
        }

        val isUri = intent.getBooleanExtra("${this@MainActivity.packageName}.uriToOpen", false)
        if (isUri) {
            if(intent.data != null) {
                openFile(intent.data!!)
                shouldLoadAdsMiddleFiles = false
                intent.data = null
                intent.removeExtra("${this@MainActivity.packageName}.uriToOpen")
                return
            }
        }


        intent.extras?.let {
            val fileModelToOpen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("${this.packageName}.fileModelToOpen", FileModel::class.java)
            } else {
                intent.getSerializableExtra("${this.packageName}.fileModelToOpen") as FileModel?
            }

            if(fileModelToOpen != null) {
                Log.d(TAG, "handleScreen fileModelToOpen: $fileModelToOpen")
//            openFile(fileModelToOpen)
                openFileFromSplash(fileModelToOpen)
                shouldLoadAdsMiddleFiles = false

            } else {
                Log.d(TAG, "handleScreen fileModelToOpen is null")
                intent.data?.let {
                    openFile(it)
                    shouldLoadAdsMiddleFiles = false
                }
            }
            intent.removeExtra("${this.packageName}.fileModelToOpen")
        }
    }

    private fun showRestartDialog(appUpdateManager: AppUpdateManager) {
        if (isFinishing || isDestroyed) return
        runOnUiThread {
            val updateDownloadedDialog = UpdateDownloadedDialog()
            updateDownloadedDialog.listener = {
                appUpdateManager.completeUpdate()
                NotificationManager(this@MainActivity).cancelUpdateDownloadedNotification()
            }
            try {
                updateDownloadedDialog.show(supportFragmentManager, "UpdateDownloadedDialog")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting cancelable: ${e.message}")
            }
        }
    }

    private fun scheduleOneTimeNotification() {
        val timeDelay = calculateTime(9)
        Log.e("TimeDelay", timeDelay.toString())
        val work =
            PeriodicWorkRequest.Builder(OneTimeScheduleWorker::class.java, 1, TimeUnit.DAYS)
                .setInitialDelay(timeDelay, TimeUnit.MILLISECONDS)
                .addTag(TAG_WORK)
                .build()

        val workManager = WorkManager.getInstance(this)
        try {
            workManager.cancelAllWorkByTag(TAG_WORK)
        } catch (e: Exception) {
        }

        workManager.enqueueUniquePeriodicWork(
            "Notification",
            ExistingPeriodicWorkPolicy.REPLACE,
            work
        )
    }

//    private fun loadAds() {
//        binding.toolbar.ivRemoveAds.isVisible = !IAPUtils.getInstance().isPremium
//        AdmobNativeAdView.getNativeAd(
//            this,
//            R.layout.native_admod_home,
//            object : NativeAdListener() {
//                override fun onError() {
//
//                }
//
//                override fun onLoaded(nativeAd: RelativeLayout?) {}
//
//                override fun onClickAd() {
//                }
//
//                override fun onPurchased(nativeAd: RelativeLayout?) {
//                    super.onPurchased(nativeAd)
//                    binding.toolbar.ivRemoveAds.isVisible = false
//                }
//            })
//    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(checkNoDialogToShowReloadGuideRunnable)
    }

    private fun initFavoriteListener() {
        myBroadcastReceiver.apply {
            favoriteListener = { isFavorite, path ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val fileModel = viewModel.getFileModelByPath(path)
                    fileModel.isFavorite = isFavorite
                    viewModel.reactFavorite(fileModel)
                }
            }

            deleteListener = { path ->
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.deleteFile(viewModel.getFileModelByPath(path)) {
                        toast(resources.getString(R.string.delete_successfully))
                    }
                }
            }

            recentListener = { path ->
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.reactRecentFile(viewModel.getFileModelByPath(path), true)
                }
            }
            readingStatusListener = { path, isReadDone, currentPage ->
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.updateReadingStatus(viewModel.getFileModelByPath(path), isReadDone, currentPage)
                }
            }
        }
        val intentFilter = IntentFilter().apply {
            addAction(BroadcastSubmodule.ACTION_FAVORITE)
            addAction(BroadcastSubmodule.ACTION_DELETE)
            addAction(BroadcastSubmodule.ACTION_RECENT)
            addAction(BroadcastSubmodule.ACTION_READING_STATUS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(myBroadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(myBroadcastReceiver, intentFilter)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myBroadcastReceiver)
        TemporaryStorage.reset()
        PreferencesUtils.putInteger(PresKey.TIME_ENTER_APP,  PreferencesUtils.getInteger(PresKey.TIME_ENTER_APP, 1) + 1)
        TemporaryStorage.isShowedReloadGuideInThisSession = false
    }

    override fun initData() {
        val sortState = PreferencesUtils.getInteger(PresKey.SORT_STATE, 4)
        viewModel.sortFile(SortState.getSortState(sortState))
        binding.swipeRefresh.setProgressViewOffset(true, 150, 220)
        binding.swipeRefresh.setColorSchemeResources(
            R.color.primaryColor,
            R.color.primaryColor,
            R.color.primaryColor
        )
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent")
        val sortState = PreferencesUtils.getInteger(PresKey.SORT_STATE, 4)
        intent.let {
            setIntent(it)
            handleIntentToMove()
            handleIntentToShowUI()
        }
        if (sortState == 7) {
            binding.recentlyAddedSection.performClick()
        } else {
            handleSortAction(sortState)
        }

//        binding.toolbar.ivBack.visibility = View.GONE
//        binding.toolbar.ivFilter.visibility = View.VISIBLE
//        binding.toolbar.ivCheck.visibility = View.VISIBLE
//        binding.toolbar.chooseType.visibility = View.VISIBLE
//        binding.recentlyAddedSection.visibility = View.VISIBLE
//        binding.toolbar.tvTitle.text = handleAppNameSpannable()
//        handleUIBaseOnBottomTab(R.id.navigation_home, false)
//        handleUIBaseOnFileTab(binding.toolbar.tvAll)
//        viewModel.updateFileTab(FileTab.ALL_FILE)
//        handleSortAction(4)
    }

    override fun initListener() {
        binding.navView. setOnItemSelectedListener(onNavigationItemSelectedListener)
        binding.toolbar.ivFilter.setOnClickListener {
            val dialog = SortDialog()
            dialog.setOnSortSelectedListener(::handleSortAction)
            dialog.show(supportFragmentManager, "SortDialog")
        }

        viewModel.loadAddedTodayFiles.observe(this) { addedNumber ->
            if (addedNumber == 0) {
                binding.recentlyAddedNumber.visibility = View.GONE
            } else {
                if (viewModel.currentBottomTab.value == BottomTab.HOME && viewModel.sortStateObservable.value != SortState.DATE_TODAY)  binding.recentlyAddedNumber.visibility = View.VISIBLE
            }
            binding.recentlyAddedNumber.text = "+ $addedNumber"
        }

        binding.recentlyAddedSection.setOnClickListener {
            binding.toolbar.ivSearch.visibility = View.VISIBLE
            binding.toolbar.ivFilter.visibility = View.GONE
            binding.toolbar.ivCheck.visibility = View.GONE
            binding.navView.visibility=View.GONE
            TransitionManager.beginDelayedTransition(binding.root, AutoTransition())
            binding.recentlyAddedSection.visibility = View.GONE
            binding.toolbar.chooseType.visibility = View.GONE
            binding.toolbar.ivBack.visibility = View.VISIBLE
            binding.toolbar.tvTitle.text = resources.getString(R.string.recent_add)
            handleSortAction(7)
        }
        binding.toolbar.ivBack.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.root, AutoTransition())
            binding.toolbar.ivBack.visibility = View.GONE
            binding.toolbar.ivFilter.visibility = View.VISIBLE
            binding.toolbar.ivCheck.visibility = View.VISIBLE
            binding.toolbar.chooseType.visibility = View.VISIBLE
            binding.recentlyAddedSection.visibility = View.VISIBLE
            binding.toolbar.tvTitle.text = handleAppNameSpannable(showIcon = IAPUtils.isPremium())
            setOutRecently()
            handleSortAction(4)
            binding.navView.visibility = View.VISIBLE
        }

        binding.goSetting.setOnClickListener {
            Log.d(TAG, "binding.goSetting.setOnClickListener")
            AppOpenManager.getInstance().disableAppResume()
            requestPermissionStorage {
                if(it) {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    startActivity(intent)

                    lifecycleScope.launch {
                        viewModel.migrateFileData()
                    }

                } else {
                    checkStoragePermissionToShowUI()
                }
            }
        }

        viewModel.currentBottomTab.observe(this) { tab ->
            Log.d(this.javaClass.simpleName, "Current Bottom Tabs: $tab")
            // Update UI based on the new tab list
            when (tab) {
                BottomTab.HOME -> {
                    binding.recentlyAddedSection.visibility = View.VISIBLE
                    if(viewModel.loadAddedTodayFiles.value != 0) binding.recentlyAddedNumber.visibility = View.VISIBLE else View.GONE
//                    binding.toolbar.ivSetting.setOnClickListener {
//                        showMenuFunction()
//                    }
                    binding.toolbar.ivSetting.setOnClickListener {
                        SettingActivity.start(this)
                    }
                }
                BottomTab.RECENT -> {
                    binding.recentlyAddedSection.visibility = View.GONE
//                    binding.toolbar.ivSetting.setOnClickListener {
//                        showMenuFunction()
//                    }
                    binding.toolbar.ivSetting.setOnClickListener {
                        SettingActivity.start(this)
                    }
                }
                BottomTab.FAVORITE -> {
                    binding.recentlyAddedSection.visibility = View.GONE
//                    binding.toolbar.ivSetting.setOnClickListener {
//                        showMenuFunction()
//                    }
                    binding.toolbar.ivSetting.setOnClickListener {
                        SettingActivity.start(this)
                    }
                }
                else -> {
//                    binding.toolbar.ivSetting.setOnClickListener {
//                        showMenuFunction()
//                    }
                    binding.toolbar.ivSetting.setOnClickListener {
                        SettingActivity.start(this)
                    }
                }
            }
        }
        binding.toolbar.ivIap.setOnClickListener {
            IapActivityV2.start(this)
        }

        binding.toolbar.ivSearch.setOnClickListener {
            SearchFileActivity.start(this)
        }

        binding.buttonCreate.setOnClickListener {
            AppOpenManager.getInstance().disableAppResume()
            startChooseImageActivity()
        }

        binding.toolbar.tvAll.setOnClickListener {
            Log.d(TAG, "tvAll Clicked")
            binding.viewPager.currentItem = ALL_FILES_FRAGMENT_INDEX
            handleUIBaseOnFileTab(binding.toolbar.tvAll)

        }

        binding.toolbar.tvPdf.setOnClickListener {
            Log.d(TAG, "tvPdf Clicked")
            binding.viewPager.currentItem = PDF_FILES_FRAGMENT_INDEX
            handleUIBaseOnFileTab(binding.toolbar.tvPdf)
        }

        binding.toolbar.tvWord.setOnClickListener {
            Log.d(TAG, "tvWord Clicked")
            binding.viewPager.currentItem = WORD_FILES_FRAGMENT_INDEX
            handleUIBaseOnFileTab(binding.toolbar.tvWord)
        }

        binding.toolbar.tvExcel.setOnClickListener {
            Log.d(TAG, "tvExcel Clicked")
            binding.viewPager.currentItem = EXCEL_FILES_FRAGMENT_INDEX
            handleUIBaseOnFileTab(binding.toolbar.tvExcel)
        }

        binding.toolbar.tvPpt.setOnClickListener {
            Log.d(TAG, "tvPpt Clicked")
            binding.viewPager.currentItem = PPT_FILES_FRAGMENT_INDEX
            handleUIBaseOnFileTab(binding.toolbar.tvPpt)
        }

        binding.toolbar.ivCheck.setOnClickListener {



            val currentIndex = binding.viewPager.currentItem

            val fileTab = when (currentIndex) {
                ALL_FILES_FRAGMENT_INDEX -> FileTab.ALL_FILE
                PDF_FILES_FRAGMENT_INDEX -> FileTab.PDF
                WORD_FILES_FRAGMENT_INDEX -> FileTab.WORD
                EXCEL_FILES_FRAGMENT_INDEX -> FileTab.EXCEL
                PPT_FILES_FRAGMENT_INDEX -> FileTab.PPT
                else -> FileTab.ALL_FILE
            }

            SelectMultipleFilesActivity.start(this, fileTab = fileTab)
        }

//        binding.toolbar.ivRemoveAds.setOnClickListener {
//            launchActivity<PremiumActivityJava> { }
//        }
        binding.notificationWarningSection.setOnClickListener {
            requestNotificationPermission()
        }
        binding.featureRequestSection.setOnClickListener {
            FeatureRequestActivity.start(this)
        }
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            logEvent("refresh_files")
//            lifecycleScope.launch(Dispatchers.IO) {
//                if (isAcceptManagerStorage()) {
//                    viewModel.migrateFileData()
//                } else {
//                    viewModel.addSameFilesInternal()
//                    Log.w("SplashActivity", "Skipping migration, no storage permission")
//                }
//            }
            TemporaryStorage.isShowedReloadGuideInThisSession = true // if user refresh files by swipe, not show reload guide anymore
            val intent = Intent(this, ReloadLoadingActivity::class.java)
            startActivity(intent)
        }

    }
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                onNotificationPermissionGranted()
            } else {
                onNotificationPermissionDenied()
            }
        }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermissionFlow() {
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Already have it
                PreferencesUtils.putBoolean("NOTIFICATION", true)
                PreferencesUtils.putInteger("NOTIF_DENIAL_COUNT", 0)
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                // Show rationale, then request
                requestNotificationPermissionLauncher.launch(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )

            }

            else -> {
                // First-time request
                requestNotificationPermissionLauncher.launch(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }
    private fun requestNotificationPermission() {
        Log.e("GetStartActivity", "Request notification permission SDK: ${Build.VERSION.SDK_INT}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val denialCount = PreferencesUtils.getInteger("NOTIF_DENIAL_COUNT", 0)
            if (denialCount >= 2) {
                AppOpenManager.getInstance().disableAppResume()
                requestPermissionNotificationGoToSetting {
                    if (it) {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                        onNotificationPermissionGranted()
                    } else {
                        checkNotificationPermissionToShowUI()
                        checkFeatureRequestToShowUI()
                    }
                }
            } else {
                requestNotificationPermissionFlow()
            }
        } else {
            Log.d("MainActivity", "Notification permission not required for SDK < 33")
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                AppOpenManager.getInstance().disableAppResume()
                requestPermissionNotificationGoToSetting {
                    if(it) {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                        onNotificationPermissionGranted()

                    } else {
                        checkNotificationPermissionToShowUI()
                        checkFeatureRequestToShowUI()
                    }
                }
            } else {
                onNotificationPermissionGranted()
            }
        }
    }

    private fun onNotificationPermissionGranted() {
        PreferencesUtils.putBoolean("NOTIFICATION", true)
        // Proceed with showing notifications
        //Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        Log.d("MainActivity", "Notification permission granted")
        try {
            startForegroundService( Intent(this, NotificationForegroundService::class.java))
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error starting service: ${e.message}")
        }
    }

    private fun onNotificationPermissionDenied() {
        // Show a message or guide the user to settings
        //Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        PreferencesUtils.putBoolean("NOTIFICATION", false)
        PreferencesUtils.putInteger("NOTIF_DENIAL_COUNT",
            PreferencesUtils.getInteger("NOTIF_DENIAL_COUNT", 0) + 1
        )
        try {
            startForegroundService( Intent(this, NotificationForegroundService::class.java))
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error starting service: ${e.message}")
        }
        Log.e("MainActivity", "Notification permission denied")
    }
    private fun handleUIBaseOnFileTab(selectedTextView: TextView) {
        when(selectedTextView) {
            binding.toolbar.tvAll -> viewModel.updateFileTab(FileTab.ALL_FILE)
            binding.toolbar.tvPdf -> viewModel.updateFileTab(FileTab.PDF)
            binding.toolbar.tvWord -> viewModel.updateFileTab(FileTab.WORD)
            binding.toolbar.tvPpt -> viewModel.updateFileTab(FileTab.PPT)
            binding.toolbar.tvExcel -> viewModel.updateFileTab(FileTab.EXCEL)
        }

        val allTextViews = listOf(
            binding.toolbar.tvAll,
            binding.toolbar.tvPdf,
            binding.toolbar.tvWord,
            binding.toolbar.tvExcel,
            binding.toolbar.tvPpt
        )

        // Reset tất cả về màu và kiểu chữ mặc định
        for (textView in allTextViews) {
            textView.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL)
            textView.setTextColor(getColor(R.color.dark_gray))
            textView.background = null
        }
        val selectedColor = when (selectedTextView) {
            binding.toolbar.tvPpt -> R.color.orange
            binding.toolbar.tvExcel -> R.color.green
            binding.toolbar.tvWord -> R.color.blue
            binding.toolbar.tvPdf -> R.color.primaryColor
            else -> R.color.red
        }
        val underlineResource = if (selectedTextView == binding.toolbar.tvPpt) {
            R.drawable.underline_orange
        } else if (selectedTextView == binding.toolbar.tvWord){
            R.drawable.underline_blue
        } else if (selectedTextView == binding.toolbar.tvExcel){
            R.drawable.underline_green
        } else {
            R.drawable.underline
        }

        // Cập nhật màu, kiểu chữ và underline cho item được chọn
        selectedTextView.setTypeface(null, Typeface.BOLD)
        selectedTextView.setTextColor(ContextCompat.getColor(this, selectedColor))
        selectedTextView.setBackgroundResource(underlineResource)
    }


    override fun viewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(LayoutInflater.from(this))
    }

    private fun showMenuFunction() {
        val bottomSheetMenu = BottomSheetMenuFunction(::onSelectedFunction)
        bottomSheetMenu.show(supportFragmentManager, BottomSheetMenuFunction::javaClass.name)
    }

    private fun showRecentFunction() {
        val bottomSheetRecentFunction = BottomSheetRecentFunction(::onSelectedFunction)
        bottomSheetRecentFunction.show(
            supportFragmentManager,
            BottomSheetRecentFunction::javaClass.name
        )
    }

    private fun showFavoriteFunc() {
        val bottomSheetFavoriteFunction = BottomSheetFavoriteFunction(::onSelectedFunction)
        bottomSheetFavoriteFunction.show(
            supportFragmentManager,
            BottomSheetFavoriteFunction::javaClass.name
        )
    }

    private fun onSelectedFunction(state: FunctionState) {
        when (state) {
            FunctionState.BROWSE_FILE -> {
                AppOpenManager.getInstance().disableAppResume()
                browserFile()
            }

            FunctionState.RATE_US -> {
                AppOpenManager.getInstance().disableAppResume()
                openAppOnStore()
            }

            FunctionState.FEEDBACK -> {
                sendFeedback()
            }

            FunctionState.SHARE_APP -> {
                shareApp()
            }

            FunctionState.PRIVACY_POLICY -> {
                PolicyActivity.start(this)
            }

            FunctionState.CHANGE_LANGUAGE -> {
                changeLanguage()
            }

            FunctionState.CLEAR_RECENT -> {
                showDialogConfirm(
                    resources.getString(R.string.clear_recent),
                    resources.getString(R.string.clear_recent_message)
                ) {
                    viewModel.deleteAllRecent()
                }
            }

            FunctionState.CLEAR_FAVORITE -> {
                showDialogConfirm(
                    resources.getString(R.string.clear_favourite),
                    resources.getString(R.string.clear_fav_message)
                ) {
                    viewModel.deleteAllFavorite()
                }
            }

            else -> {
            }
        }
    }
    private fun handleSortAction(id: Int): Boolean {
        return when (id) {
            3 -> {
                viewModel.sortFile(SortState.DATE)
                //loadNativeAdsMiddleFiles()
                false
            }
            4 -> {
                viewModel.sortFile(SortState.DATE_DESC)
                //loadNativeAdsMiddleFiles()
                false
            }
            1 -> {
                viewModel.sortFile(SortState.NAME)
                //loadNativeAdsMiddleFiles()
                false
            }
            2 -> {
                viewModel.sortFile(SortState.NAME_DESC)
                //loadNativeAdsMiddleFiles()
                false
            }
            5 -> {
                viewModel.sortFile(SortState.SIZE)
                //loadNativeAdsMiddleFiles()
                false
            }
            6 -> {
                viewModel.sortFile(SortState.SIZE_DESC)
                //loadNativeAdsMiddleFiles()
                false
            }
            7 -> {
                viewModel.sortFile(SortState.DATE_TODAY)
                true  // Return true when sorting by DATE_TODAY
            }
            else -> false
        }
    }


    private fun browserFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        startActivityForResult(intent, CODE_ACTION_OPEN_DOCUMENT_FILE)
        AppOpenManager.getInstance().disableAppResume()
    }

    private fun changeLanguage() {
        launchActivity<LanguageActivity> { }
    }

    private fun startChooseImageActivity() {
        Intent().apply {
            type = "image/*"
            action = Intent.ACTION_PICK
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(
                Intent.createChooser(this, title),
                CODE_CHOOSE_IMAGE
            )
        }
    }
    private fun setOutRecently() {
        val currentIndex = binding.viewPager.currentItem
        val fileTab = when (currentIndex) {
            ALL_FILES_FRAGMENT_INDEX -> FileTab.ALL_FILE
            PDF_FILES_FRAGMENT_INDEX -> FileTab.PDF
            WORD_FILES_FRAGMENT_INDEX -> FileTab.WORD
            EXCEL_FILES_FRAGMENT_INDEX -> FileTab.EXCEL
            PPT_FILES_FRAGMENT_INDEX -> FileTab.PPT
            else -> FileTab.ALL_FILE
        }

        viewModel.getCurrentFiles(fileTab).value?.let { it1 ->
            viewModel.setNotRecently(
                it1
            )
        }

    }

    private fun showPopupCreatePdf(lstUri: ArrayList<String>) {
        val bottomSheetCreatePdf = BottomSheetCreatePdf(complete = { fileName, password,size ->
            showHideLoading(true)
            createPdf(lstUri, fileName, password, size, object : OnPDFCreatedInterface {
                override fun onPDFCreationStarted() {

                }

                override fun onPDFCreated(success: Boolean, path: String?) {
                    Log.d("File create", path ?: "")
                    path?.let {
                        val uriFile = FileSaveManager.saveFileStorage(
                            this@MainActivity,
                            path
                        )
                        uriFile?.let {
                            val realPath =
                                PathUtils.getRealPathFromUri(this@MainActivity, it)
                            viewModel.createFile(realPath) {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    CreateSuccessActivity.start(
                                        this@MainActivity,
                                        it,
                                        lstUri.size,
                                        lstUri[0]
                                    )
                                }
                            }
                        } ?: {
                            toast(getString(R.string.app_error))
                        }
                    }

                    showHideLoading(false)
                }
            })
        })
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                bottomSheetCreatePdf.show(supportFragmentManager, BottomSheetCreatePdf::javaClass.name)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Error showing BottomSheetCreatePdf: ${e.message}")
            }
        },500)
    }
    private var lastClickedTime: Long = 0
    private val clickDelay = 600L

    private val onNavigationItemSelectedListener =
        NavigationBarView.OnItemSelectedListener { item ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickedTime < clickDelay) {
                return@OnItemSelectedListener false
            }
            lastClickedTime = currentTime
            handleUIBaseOnBottomTab(item.itemId, false)
            when (item.itemId) {
                R.id.navigation_home -> {
                    return@OnItemSelectedListener true
                }

                R.id.navigation_recent -> {
                    return@OnItemSelectedListener true
                }

                R.id.navigation_favorite -> {
                    return@OnItemSelectedListener true
                }
            }
            false
        }

    private fun handleUIBaseOnBottomTab(id: Int, alsoSelect: Boolean = true) {
        if (alsoSelect) {
            binding.navView.selectedItemId = id
            return
        }

        TransitionManager.beginDelayedTransition(binding.root, AutoTransition())

        when (id) {
            R.id.navigation_home -> {
                viewModel.updateBottomTab(BottomTab.HOME)
                checkStoragePermissionToShowUI()
                checkNotificationPermissionToShowUI()
                checkFeatureRequestToShowUI()
                binding.toolbar.apply {
                    tvTitle.text = handleAppNameSpannable(showIcon = IAPUtils.isPremium())
                    ivSearch.visibility = View.VISIBLE
                    ivFilter.visibility = View.VISIBLE
                    ivCheck.visibility = View.VISIBLE
                    ivBack.visibility = View.GONE
                }

                binding.recentlyAddedSection.visibility = View.VISIBLE
                binding.recentlyAddedNumber.visibility =
                    if (viewModel.loadAddedTodayFiles.value != 0) View.VISIBLE else View.GONE

                if (viewModel.sortStateObservable.value == SortState.DATE_TODAY) {
                    handleSortAction(4)
                }
            }

            R.id.navigation_recent -> {
                viewModel.updateBottomTab(BottomTab.RECENT)

                binding.toolbar.apply {
                    tvTitle.text = getString(R.string.title_recent)
                    ivSearch.visibility = View.VISIBLE
                    ivFilter.visibility = View.GONE
                    ivCheck.visibility = View.GONE
                    ivBack.visibility = View.GONE
                }

                binding.recentlyAddedSection.visibility = View.GONE
            }

            R.id.navigation_favorite -> {
                viewModel.updateBottomTab(BottomTab.FAVORITE)

                binding.toolbar.apply {
                    tvTitle.text = getString(R.string.title_fav)
                    ivSearch.visibility = View.GONE
                    ivFilter.visibility = View.GONE
                    ivCheck.visibility = View.GONE
                    ivBack.visibility = View.GONE
                }

                binding.recentlyAddedSection.visibility = View.GONE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CODE_CHOOSE_IMAGE) {
                data?.let {
                    val lstData = ArrayList<String>()
                    it.clipData?.let { clipData ->
                        for (i in 0 until clipData.itemCount) {
                            val item = clipData.getItemAt(i)
                            val realPath = PathUtils.getPath(this, item.uri)
                            realPath?.let {
                                if (realPath.isNotEmpty() && File(realPath).exists()) {
                                    lstData.add(item.uri.toString())
                                }
                            }
                        }
                    } ?: it.data?.let { data ->
                        if (File(PathUtils.getRealPathFromUri(this, data)).exists()) {
                            lstData.add(data.toString())
                        }
                    }

                    if (lstData.size > 0) {
                        showPopupCreatePdf(lstData)
                    }
                }
            }
        }
    }

//    override fun onBackPressed() {
////        showAppRating(true) {
//            finish()
//        }
//    }

    private fun getDefaultPdfViewerClass(): ResolveInfo? {
        val fileName = "file_example_PDF.pdf"
        val assetManager = assets
        val file = File(File(filesDir, "defaultFiles").apply { mkdirs() }, fileName)

        // Copy file từ assets nếu chưa tồn tại
        if (!file.exists()) {
            try {
                assetManager.open(fileName).use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        val uri = Uri.fromFile(file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        Log.d("DefaultReader", "resolveInfo: $resolveInfo")

        return resolveInfo
    }
}