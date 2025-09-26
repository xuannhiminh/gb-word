package com.word.office.docx.viewer.document.editor.reader.screen.iap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.ezteam.baseproject.extensions.hasExtraKeyContaining
import com.ezteam.baseproject.iapLib.v3.BillingProcessor
import com.ezteam.baseproject.iapLib.v3.Constants
import com.ezteam.baseproject.iapLib.v3.PurchaseInfo
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.utils.PresKey
import com.ezteam.baseproject.utils.TemporaryStorage
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.google.firebase.analytics.FirebaseAnalytics.Param
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.word.office.docx.viewer.document.editor.reader.R
import com.word.office.docx.viewer.document.editor.reader.databinding.ActivityIapV3Binding
import com.word.office.docx.viewer.document.editor.reader.screen.base.PdfBaseActivity
import com.word.office.docx.viewer.document.editor.reader.screen.language.LanguageActivity
import com.word.office.docx.viewer.document.editor.reader.screen.main.MainActivity
import com.word.office.docx.viewer.document.editor.reader.screen.start.RequestAllFilePermissionActivity
import com.word.office.docx.viewer.document.editor.reader.utils.AppUtils
import com.word.office.docx.viewer.document.editor.reader.utils.FirebaseRemoteConfigUtil
import java.util.Locale


class IapActivityV2 : PdfBaseActivity<ActivityIapV3Binding>() {

    companion object {
        fun start(activity: FragmentActivity) {
            val pkg = activity.packageName

            activity.intent.data?.let {
                activity.intent.apply {
                    setClass(activity, IapActivityV2::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: activity.intent.hasExtraKeyContaining(pkg).let { hasKey ->
                if (hasKey) {
                    activity.intent.apply {
                        setClass(activity, IapActivityV2::class.java)
                        flags = 0 // reset to 0 because sometime intent already has flags new task and kill activity before start
                    }
                    activity.startActivity(activity.intent)
                } else {
                    val intent = Intent(activity, IapActivityV2::class.java)
                    activity.startActivity(intent)
                }
            }
        }
    }

    private var isFromSplash = false

    override fun viewBinding(): ActivityIapV3Binding {
        return ActivityIapV3Binding.inflate(LayoutInflater.from(this))
    }

//    private var isFreeTrialShowed = false

//    private var yearlyPrice: String = ""
//    private var monthlyPriceFromYear: String = ""

    override fun initView() {
//        window.statusBarColor = Color.parseColor("#1F0718")
//        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        initIAP()

        // Ẩn btnClose ban đầu
        binding.btnClose.alpha = 0f
        binding.btnClose.postDelayed({
            binding.btnClose.animate().alpha(1f).setDuration(300).start()
        }, 3000)
    }

    private fun updateViewBaseOnPremiumState() {
        IAPUtils.getSubscriptionListingDetails(IAPUtils.KEY_PREMIUM) { productDetails ->
            if (productDetails.isNullOrEmpty()) {
                // No details returned
                Log.e("IapActivity", "No subscription details")
                return@getSubscriptionListingDetails
            }
            val pd = productDetails[0] ?: return@getSubscriptionListingDetails

            // --- Weekly plan ---
            val weeklyOffer = pd.subscriptionOfferDetails
                ?.find { it.basePlanId == IAPUtils.KEY_PREMIUM_WEEKLY_PLAN }

            if (weeklyOffer != null) {
                val weeklyPhase = weeklyOffer.pricingPhases.pricingPhaseList.firstOrNull { it.priceAmountMicros > 0 }
                val monthlyPriceText = weeklyPhase?.formattedPrice ?: "-"
                binding.price.text = "$monthlyPriceText/Week after FREE 3-day Trial"

            }
            val isSubscribed = IAPUtils.isSubscribed(pd.productId)
            // finish
            if (isSubscribed) {
                navigateToNextScreen()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        // AppOpenManager.getInstance().disableAppResume()
        isFromSplash = intent.getBooleanExtra("${packageName}.isFromSplash", false)
        super.onCreate(savedInstanceState)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
    }




    private fun showLoadedAdsInterstitial(interstitialAd: InterstitialAd?, complete: () -> Unit) {
        Log.i("IapActivityV2", "showAdsInterstitial called with ad: $interstitialAd")
        Admob.getInstance().showInterAds(this, interstitialAd, object : AdCallback() {
            override fun onNextAction() {
                complete.invoke()
            }
        })
    }

    private fun navigateToNextScreen() {
        if (IAPUtils.isPremium()) {
            TemporaryStorage.interAdPreloaded = null
        }
        showLoadedAdsInterstitial(TemporaryStorage.interAdPreloaded) {
            if (!isFromSplash) {
                finish()
                return@showLoadedAdsInterstitial
            }
            if (PreferencesUtils.getBoolean(PresKey.GET_START, true)) {
                LanguageActivity.start(this)
            } else {
                MainActivity.start(this)
            }
            finish()
        }
    }


    override fun initListener() {

        binding.btnClose.setOnClickListener {
            navigateToNextScreen()
            logEvent("iap_close_pressed")
        }

//        binding.btnRestore.setOnClickListener {
//            IAPUtils.loadOwnedPurchasesFromGoogleAsync {
//                if (it) {
//                    Toast.makeText(this, getString(R.string.restore_success), Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this, getString(R.string.restore_fail), Toast.LENGTH_SHORT).show()
//                }
//                updateViewBaseOnPremiumState()
//            }
//        }


        binding.btnFreeTrial.setOnClickListener {
//            if (isAnnualSelected) {
//                showFreeTrialDialog()
//            } else {
//                logEvent("purchase_month_pressed")
//                IAPUtils.callSubscription(this@IapActivityV2, IAPUtils.KEY_PREMIUM, IAPUtils.KEY_PREMIUM_MONTHLY_PLAN)
//            }
//            IapRegistrationSuccessfulActivity.start(this)
            logEvent("purchase_week_pressed")
            IAPUtils.callSubscription(this@IapActivityV2, IAPUtils.KEY_PREMIUM, IAPUtils.KEY_PREMIUM_WEEKLY_PLAN)
        }
    }

    override fun initData() {
//        binding.termOfUse.paintFlags = binding.termOfUse.paintFlags or Paint.UNDERLINE_TEXT_FLAG
//        binding.privacyPolicy.paintFlags = binding.privacyPolicy.paintFlags or Paint.UNDERLINE_TEXT_FLAG
//        binding.btnRestore.paintFlags = binding.btnRestore.paintFlags or Paint.UNDERLINE_TEXT_FLAG

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        checkNightModeState()
    }
    private fun checkNightModeState() {
        findViewById<ImageView>(R.id.btnClose).setImageResource(R.drawable.icon_close_insert)
        //replaceIcons(binding.featureSection, R.drawable.icon_check_pro_night, R.drawable.icon_close_basic)
    }

    private fun replaceIcons(container: ViewGroup, checkIcon: Int, closeIcon: Int) {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is ViewGroup) {
                replaceIcons(child, checkIcon, closeIcon)
            } else if (child is ImageView) {
                val tag = child.tag
                if (tag == "check") {
                    child.setImageResource(checkIcon)
                } else if (tag == "close") {
                    child.setImageResource(closeIcon)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onBackPressed() {
        navigateToNextScreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        IAPUtils.unregisterListener(iBillingHandler)
        AppOpenManager.getInstance().enableAppResume()
        TemporaryStorage.interAdPreloaded = null
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private fun logEvent(event: String) {
        firebaseAnalytics.logEvent(event, Bundle())
    }

    var  iBillingHandler  = object : BillingProcessor.IBillingHandler {
        override fun onProductPurchased(
            productId: String,
            details: PurchaseInfo?
        ) {
            logEvent("purchase_success_$productId")
            if (FirebaseRemoteConfigUtil.getInstance().isLogPurchaseEvent()) {
                val params = Bundle()
                params.putString(Param.TRANSACTION_ID, details?.purchaseData?.orderId )
                firebaseAnalytics.logEvent(Event.PURCHASE, params)
            }
            Toast.makeText(this@IapActivityV2, getString(R.string.you_premium), Toast.LENGTH_SHORT).show()
            updateViewBaseOnPremiumState()
        }

        override fun onPurchaseHistoryRestored() {
            // Handle restored purchases here
            Toast.makeText(this@IapActivityV2, getString(R.string.restore_success), Toast.LENGTH_SHORT).show()
            updateViewBaseOnPremiumState()
        }

        override fun onBillingError(errorCode: Int, error: Throwable?) {
            Log.d("IapActivity", "Billing error: $errorCode, ${error?.message}")
            // Log or handle errors here
            if (errorCode == 1) { // user cancel
                if (PreferencesUtils.getBoolean(PresKey.GET_START, true)) {
                    logEvent("purchase_cancelled_start")
                    //startRequestAllFilePermission()
                } else {
                    logEvent("purchase_cancelled")
                }
                return
            } else if (errorCode == 3) { // Billing service unavailable
                Toast.makeText(this@IapActivityV2, R.string.please_update_store, Toast.LENGTH_LONG).show()
                if (PreferencesUtils.getBoolean(PresKey.GET_START, true)) {
                    navigateToNextScreen()
                }
                return
            } else if (errorCode == 7) { // Items already owned
                return
            } else if (errorCode == Constants.BILLING_ERROR_FAILED_TO_ACKNOWLEDGE_PURCHASE) { // Items already owned
                Toast.makeText(this@IapActivityV2, getString(R.string.not_confirm_purchase), Toast.LENGTH_LONG).show()
                return
            } else {
                Toast.makeText(this@IapActivityV2, "Billing error code: $errorCode", Toast.LENGTH_LONG).show()
            }
        }

        override fun onBillingInitialized() {
            // Billing service is initialized, you can query products or subscriptions here
            // Toast.makeText(this@IapActivityV2, "Billing initialized", Toast.LENGTH_SHORT).show()
            IAPUtils.loadOwnedPurchasesFromGoogleAsync {
                updateViewBaseOnPremiumState()
            }
        }

    }

    private fun initIAP() {
        IAPUtils.initAndRegister(this, AppUtils.PUBLIC_LICENSE_KEY, iBillingHandler)
    }
}