package com.pdf.pdfreader.pdfviewer.editor.screen.iap

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.ezteam.baseproject.extensions.hasExtraKeyContaining
import com.ezteam.baseproject.iapLib.v3.BillingProcessor
import com.ezteam.baseproject.iapLib.v3.Constants
import com.ezteam.baseproject.iapLib.v3.PurchaseInfo
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.pdf.pdfreader.pdfviewer.editor.R
import com.pdf.pdfreader.pdfviewer.editor.common.PresKey
import com.pdf.pdfreader.pdfviewer.editor.databinding.ActivityIapBinding
import com.pdf.pdfreader.pdfviewer.editor.screen.PolicyActivity
import com.pdf.pdfreader.pdfviewer.editor.screen.TermAndConditionsActivity
import com.pdf.pdfreader.pdfviewer.editor.screen.base.PdfBaseActivity
import com.pdf.pdfreader.pdfviewer.editor.screen.start.RequestAllFilePermissionActivity
import com.pdf.pdfreader.pdfviewer.editor.utils.AppUtils
import setSelectedCard
import java.util.Locale

class IapActivity : PdfBaseActivity<ActivityIapBinding>() {

    companion object {
        fun start(activity: FragmentActivity) {
            val pkg = activity.packageName

            activity.intent.data?.let {
                activity.intent.apply {
                    setClass(activity, IapActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: activity.intent.hasExtraKeyContaining(pkg).let { hasKey ->
                if (hasKey) {

                    activity.intent.apply {
                        setClass(activity, IapActivity::class.java)
                        flags = 0 // reset to 0 because sometime intent already has flags new task and kill activity before start
                    }
                    activity.startActivity(activity.intent)
                } else {
                    val intent = Intent(activity, IapActivity::class.java)
                    activity.startActivity(intent)
                }
            }
        }
    }

    override fun viewBinding(): ActivityIapBinding {
        return ActivityIapBinding.inflate(LayoutInflater.from(this))
    }

    private var isFreeTrialShowed = false

    private var isAnnualSelected = true
    private var isAnnualSelectable= true
    private var isMonthlySelectable= true

    private var yearlyPrice: String = ""
    private var monthlyPriceFromYear: String = ""

    private fun showAdsInterstitial(interstitialAd: InterstitialAd?) {
        Log.i("IapActivity", "showAdsInterstitial called with ad: $interstitialAd")
        Admob.getInstance().showInterAds(this@IapActivity, interstitialAd, object : AdCallback() {
            override fun onNextAction() {
                if (PreferencesUtils.getBoolean(PresKey.GET_START, true) && !IAPUtils.isPremium()) {
//                    if (!isFreeTrialShowed) {
//                        showFreeTrialDialog()
//                    } else {
//                        startRequestAllFilePermission()
//                    }
                    startRequestAllFilePermission()
                } else {
                    this@IapActivity.finish()
                }
            }
        })
    }

    override fun initView() {
//        window.statusBarColor = Color.parseColor("#1F0718")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        initIAP()

        applyGradientToTitle()

        // Ẩn btnClose ban đầu
        binding.btnClose.alpha = 0f
        binding.btnClose.postDelayed({
            binding.btnClose.animate().alpha(1f).setDuration(300).start()
        }, 3000)

        // Chọn gói mặc định
        setSelectedCard(
            binding.btnSubscribeAnnual,
            binding.endSection,
            binding.btnSubscribeMonthly,
            binding.endSectionMonthly
        )
        binding.btnFreeTrial.text = getString(R.string._3_days_free_trial)

        binding.btnFreeTrial.post {
            val buttonWidth = binding.btnFreeTrial.width
            val shineView = binding.shineView

            shineView.layoutParams.height = binding.btnFreeTrial.height

            val clipBounds = android.graphics.Rect(0, 0, buttonWidth, binding.btnFreeTrial.height)
            shineView.clipBounds = clipBounds

            val animator = ObjectAnimator.ofFloat(
                shineView,
                "translationX",
                -shineView.width.toFloat(),
                buttonWidth.toFloat()
            )
            animator.duration = 1250
            animator.repeatCount = ValueAnimator.INFINITE
            animator.start()
        }


    }

    private fun updateViewBaseOnPremiumState() {
            IAPUtils.getSubscriptionListingDetails(IAPUtils.KEY_PREMIUM) { productDetails ->
                if (productDetails.isNullOrEmpty()) {
                    // No details returned
                    Log.e("IapActivity", "No subscription details")
                    return@getSubscriptionListingDetails
                }
                val pd = productDetails[0] ?: return@getSubscriptionListingDetails
                val yearlyPlanOffer = pd.subscriptionOfferDetails
                    ?.find { it.basePlanId == IAPUtils.KEY_PREMIUM_YEARLY_PLAN }
                    ?: return@getSubscriptionListingDetails

                val yearlyPriceText = yearlyPlanOffer.pricingPhases.pricingPhaseList
                    .firstOrNull { it.priceAmountMicros > 0 }
                    ?.formattedPrice
                    ?: "-"

                val yearlyMicros = yearlyPlanOffer.pricingPhases.pricingPhaseList
                    .firstOrNull { it.priceAmountMicros > 0 }
                    ?.priceAmountMicros
                    ?: 0L

                val monthlyAmount = if (yearlyMicros > 0) {
                    (yearlyMicros / 12.0) / 1_000_000.0
                } else {
                    0.0
                }
                val currencyCode = yearlyPlanOffer.pricingPhases.pricingPhaseList
                    .firstOrNull { it.priceAmountMicros > 0 }
                    ?.priceCurrencyCode
                    ?: ""
                val monthlyPriceText = String.format(
                    Locale.getDefault(),
                    "%.2f %s",
                    monthlyAmount,
                    AppUtils.getCurrencySymbol(currencyCode)
                )

                // Bind to your UI
                binding.apply {
                    // e.g. "Yearly: $23.94"
                    priceDetail.text = getString(R.string.price_annual_note, yearlyPriceText)
                    // e.g. "Monthly: $1.99"
                    price.text = getString(R.string.price_monthly, monthlyPriceText)
                }

                yearlyPrice = yearlyPriceText
                monthlyPriceFromYear = monthlyPriceText

                // Enable or disable the annual button based on subscription state
                val isSubscribed = IAPUtils.isSubscribed(pd.productId)
                binding.btnSubscribeAnnual.apply {
                    isEnabled = !isSubscribed
                    alpha = if (isEnabled) 1f else 0.5f
                }



                // --- Monthly plan ---
                val monthlyOffer = pd.subscriptionOfferDetails
                    ?.find { it.basePlanId == IAPUtils.KEY_PREMIUM_MONTHLY_PLAN }

                if (monthlyOffer != null) {
                    val monthlyPhase = monthlyOffer.pricingPhases.pricingPhaseList.firstOrNull { it.priceAmountMicros > 0 }
                    val monthlyPriceText = monthlyPhase?.formattedPrice ?: "-"

                    binding.priceMonthly.text = getString(R.string.price_monthly, monthlyPriceText)

                    if (!IAPUtils.isSubscribed(pd.productId)) {
                        isMonthlySelectable = true
                        binding.btnSubscribeMonthly.isEnabled = true
                        binding.btnSubscribeMonthly.alpha = 1f

                    } else {
                        isMonthlySelectable = false
                        binding.btnSubscribeMonthly.isEnabled = false
                        binding.btnSubscribeMonthly.alpha = 0.5f
                    }
                }

                // finish
                if (isSubscribed) {
                    if (PreferencesUtils.getBoolean(PresKey.GET_START, true)) {
                        startRequestAllFilePermission()
                    } else {
                        finish()
                    }
                }
            }

    }

    private fun applyGradientToTitle() {
        binding.tvTitle.post {
            val width = binding.tvTitle.width
            if (width > 0) {
                val shader = LinearGradient(
                    0f, 0f, width.toFloat(), 0f,
                    intArrayOf(
                        Color.parseColor("#F8C83C"),
                        Color.parseColor("#FE2160")
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                binding.tvTitle.paint.shader = shader
                binding.tvTitle.invalidate()
            }
        }
        binding.tvPro.post {
            val width = binding.tvPro.width
            if (width > 0) {
                val shader = LinearGradient(
                    0f, 0f, width.toFloat(), 0f,
                    intArrayOf(
                        Color.parseColor("#FEB743"),
                        Color.parseColor("#FFB681")
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                binding.tvPro.paint.shader = shader
                binding.tvPro.invalidate()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        AppOpenManager.getInstance().disableAppResume()
        super.onCreate(savedInstanceState)
    }

    private fun showFreeTrialDialog() {
        try {
            val dialog = FreeTrialDialog()
            dialog.priceYear = yearlyPrice
            dialog.priceMonthAnnual = monthlyPriceFromYear
            dialog.positiveCallBack = {
                logEvent("purchase_year_pressed")
                IAPUtils.callSubscription(this@IapActivity, IAPUtils.KEY_PREMIUM, IAPUtils.KEY_PREMIUM_YEARLY_PLAN)
            }
            dialog.negativeCallBack = {
                if (PreferencesUtils.getBoolean(PresKey.GET_START, true)) {
                    logEvent("close_trial_dialog_start")
                    startRequestAllFilePermission()
                } else {
                    logEvent("close_trial_dialog")
                }
            }
            dialog.show(supportFragmentManager, "FreeTrialDialog")
        } catch (e: Exception) {
            Log.e("IapActivity", "Error showing free trial dialog: ${e.message}")
            if (PreferencesUtils.getBoolean(PresKey.GET_START, true)) {
                logEvent("close_trial_dialog_start_ex")
                startRequestAllFilePermission()
            } else {
                logEvent("close_free_trial_dialog_ex")
            }
        }
        isFreeTrialShowed = true
    }

    private fun startRequestAllFilePermission() {
        if(intent == null) intent = Intent(this, RequestAllFilePermissionActivity::class.java)
        if (PreferencesUtils.getBoolean(PresKey.GET_START, true) || !isAcceptManagerStorage()) {
            intent?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        RequestAllFilePermissionActivity.start(this)
        this@IapActivity.finish()
    }

    override fun initListener() {

        binding.btnClose.setOnClickListener {
            logEvent("close_IAP_activity")
            showAdsInterstitial(TemporaryStorage.interAdPreloaded)
        }

        binding.btnRestore.setOnClickListener {
            IAPUtils.loadOwnedPurchasesFromGoogleAsync {
                if (it) {
                    Toast.makeText(this, getString(R.string.restore_success), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.restore_fail), Toast.LENGTH_SHORT).show()
                }
                updateViewBaseOnPremiumState()
            }
        }

        binding.btnSubscribeAnnual.setOnClickListener {
            setSelectedCard(
                binding.btnSubscribeAnnual,
                binding.endSection,
                binding.btnSubscribeMonthly,
                binding.endSectionMonthly
            )
            isAnnualSelected = true
            binding.btnFreeTrial.text = getString(R.string._3_days_free_trial)
        }

        binding.btnSubscribeMonthly.setOnClickListener {
            setSelectedCard(
                binding.btnSubscribeMonthly,
                binding.endSectionMonthly,
                binding.btnSubscribeAnnual,
                binding.endSection
            )
            isAnnualSelected = false
            binding.btnFreeTrial.text = getString(R.string.continuee)
        }

        binding.termOfUse.setOnClickListener {
            TermAndConditionsActivity.start(this)
        }

        binding.privacyPolicy.setOnClickListener {
            PolicyActivity.start(this)
        }

        binding.shineFreeTrialContainer.setOnClickListener {
            logEvent("start_trial_IAP_pressed")
            IAPUtils.callSubscription(this@IapActivity, IAPUtils.KEY_PREMIUM, IAPUtils.KEY_PREMIUM_WEEKLY_PLAN)
//            if (isAnnualSelected) {
//                showFreeTrialDialog()
//            } else {
//                logEvent("purchase_month_pressed")
//                IAPUtils.callSubscription(this@IapActivity, IAPUtils.KEY_PREMIUM, IAPUtils.KEY_PREMIUM_MONTHLY_PLAN)
//            }
        }
    }

    override fun initData() {
        val borderWidth = 1f.dpToPx(binding.proContainer.context).toInt()
        val cornerRadius = 8f.dpToPx(binding.proContainer.context)

        // Create gradient border drawable (red -> yellow)
        val borderDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.parseColor("#99FEA745"),
                Color.parseColor("#00FFFFFF")
            )
        ).apply {
            this.cornerRadius = cornerRadius
        }

        val innerDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.parseColor("#CC302318"),
                Color.parseColor("#00FFFFFF")
            )
        ).apply {
            this.cornerRadius = cornerRadius
        }

        val layerDrawable = LayerDrawable(arrayOf(borderDrawable, innerDrawable)).apply {
            // Inset innerDrawable to create visible border
            setLayerInset(1, borderWidth, borderWidth, borderWidth, borderWidth)
        }
        // Apply backgrounds
        binding.proContainer.background = layerDrawable

        val borderWidth2 = 1f.dpToPx(binding.btnRestore.context).toInt()
        val cornerRadius2 = 27f.dpToPx(binding.btnRestore.context)

        val borderDrawable2 = GradientDrawable().apply {
            this.setColor(Color.parseColor("#99FFFFFF"))
            this.cornerRadius = cornerRadius2 - borderWidth2
        }

        val innerDrawable2 = GradientDrawable().apply {
            this.setColor(Color.parseColor("#4D3946"))
            this.cornerRadius = cornerRadius2 - borderWidth2
        }

        val layerDrawable2 = LayerDrawable(arrayOf(borderDrawable2, innerDrawable2)).apply {
            // Inset innerDrawable to create visible border
            setLayerInset(1, borderWidth2, borderWidth2, borderWidth2, borderWidth2)
        }
        // Apply backgrounds
        binding.btnRestore.background = layerDrawable2
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (PreferencesUtils.getBoolean(PresKey.GET_START, true)) {
            startRequestAllFilePermission()
        } else {
            this@IapActivity.finish()
        }
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
            Toast.makeText(this@IapActivity, getString(R.string.you_premium), Toast.LENGTH_SHORT).show()
            updateViewBaseOnPremiumState()
        }

        override fun onPurchaseHistoryRestored() {
            // Handle restored purchases here
            Toast.makeText(this@IapActivity, getString(R.string.restore_success), Toast.LENGTH_SHORT).show()
            updateViewBaseOnPremiumState()
        }

        override fun onBillingError(errorCode: Int, error: Throwable?) {
            Log.d("IapActivity", "Billing error: $errorCode, ${error?.message}")
            // Log or handle errors here
            if (errorCode == 1) { // user cancel
                if (PreferencesUtils.getBoolean(PresKey.GET_START, true)) {
                    logEvent("purchase_cancelled_start")
                    startRequestAllFilePermission()
                } else {
                    logEvent("purchase_cancelled")
                }
                return
            } else if (errorCode == 3) { // Billing service unavailable
                Toast.makeText(this@IapActivity, R.string.please_update_store, Toast.LENGTH_LONG).show()
                if (PreferencesUtils.getBoolean(PresKey.GET_START, true)) {
                    startRequestAllFilePermission()
                }
                return
            } else if (errorCode == 7) { // Items already owned
                return
            } else if (errorCode == Constants.BILLING_ERROR_FAILED_TO_ACKNOWLEDGE_PURCHASE) { // Items already owned
                Toast.makeText(this@IapActivity, getString(R.string.not_confirm_purchase), Toast.LENGTH_LONG).show()
                return
            } else {
                Toast.makeText(this@IapActivity, "Billing error code: $errorCode", Toast.LENGTH_LONG).show()
            }
        }

        override fun onBillingInitialized() {
            // Billing service is initialized, you can query products or subscriptions here
           // Toast.makeText(this@IapActivity, "Billing initialized", Toast.LENGTH_SHORT).show()
            IAPUtils.loadOwnedPurchasesFromGoogleAsync {
                updateViewBaseOnPremiumState()
            }
        }

    }

    private fun initIAP() {
        IAPUtils.initAndRegister(this, AppUtils.PUBLIC_LICENSE_KEY, iBillingHandler)
    }
}
// Extension function: convert dp to px
fun Float.dpToPx(context: Context): Float {
    return this * context.resources.displayMetrics.density
}
