package com.pdf.pdfreader.pdfviewer.editor.screen.language

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.FragmentActivity
import com.pdf.pdfreader.pdfviewer.editor.databinding.ActivityLanguageBinding
import com.pdf.pdfreader.pdfviewer.editor.screen.main.MainActivity
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.animation.AnimationUtils
import com.ezteam.baseproject.extensions.hasExtraKeyContaining
import com.ezteam.baseproject.iapLib.v3.BillingProcessor
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.pdf.pdfreader.pdfviewer.editor.common.PresKey
import com.pdf.pdfreader.pdfviewer.editor.screen.start.IntroActivity
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.pdf.pdfreader.pdfviewer.editor.R
import com.pdf.pdfreader.pdfviewer.editor.screen.iap.IapActivity
import com.pdf.pdfreader.pdfviewer.editor.screen.iap.IapActivityV2
import com.pdf.pdfreader.pdfviewer.editor.screen.start.RequestAllFilePermissionActivity

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {

    private var adapter: Language2Adapter? = null
    private val datas = listOf(*Config.itemsLanguage)

    companion object {
        private const val TAG = "LanguageActivity"

        fun start(activity: FragmentActivity) {
            val pkg = activity.packageName

            activity.intent.data?.let {
                activity.intent.apply {
                    setClass(activity, LanguageActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: activity.intent.hasExtraKeyContaining(pkg).let { hasKey ->
                if (hasKey) {
                    activity.intent.apply {
                        setClass(activity, LanguageActivity::class.java)
                    }
                    activity.startActivity(activity.intent)
                } else {
                    val intent = Intent(activity, LanguageActivity::class.java)
                    activity.startActivity(intent)
                }
            }
        }
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private fun logEvent(firebaseAnalytic: FirebaseAnalytics, event: String, key: String, value: String) {
        firebaseAnalytic.logEvent(event, Bundle().apply {
            putString(key, value)
        })
    }

    override fun initView() {
        Log.i(TAG, "initView")

        if (TemporaryStorage.shouldLoadAdsLanguageScreen) {
            startDoneCountdown()
            loadNativeNomedia()
        } else {
            showIvDoneChecked()
        }

        val selected = PreferencesHelper.getString(
            PreferencesHelper.KEY_LANGUAGE, Config.itemsLanguage[0].value
        )

        adapter = Language2Adapter(datas, this, selected, object : Language2Adapter.OnLanguageSelectedListener {
            override fun onLanguageSelected(item: ItemSelected) {
//                startDoneCountdown()
//                loadNativeNomedia()
            }
        })
        binding.rcvData.adapter = adapter
        
        // Tối ưu hiệu suất RecyclerView
        binding.rcvData.setHasFixedSize(true)
        binding.rcvData.setItemViewCacheSize(20)
        binding.rcvData.isDrawingCacheEnabled = true
        binding.rcvData.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH

        binding.rcvData.isNestedScrollingEnabled = false
        binding.rcvData.setRecycledViewPool(androidx.recyclerview.widget.RecyclerView.RecycledViewPool().apply {
            setMaxRecycledViews(0, 20)
            setMaxRecycledViews(1, 10)
            setMaxRecycledViews(2, 10)
        })
        
        scrollToSelectedLanguageSmooth(selected)
        
        binding.edtSearchLanguage.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_search, 0, 0, 0
        )
        binding.edtSearchLanguage.addTextChangedListener(object : android.text.TextWatcher {
            private var searchJob: Handler? = null
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.removeCallbacksAndMessages(null)

                searchJob = Handler().apply {
                    postDelayed({
                        adapter?.filter(s?.toString() ?: "")
                        if (adapter?.itemCount == 0) {
                            binding.layoutEmpty.visibility = View.VISIBLE
                            binding.rcvData.visibility = View.GONE
                            binding.animationView.playAnimation()
                        } else {
                            binding.layoutEmpty.visibility = View.GONE
                            binding.rcvData.visibility = View.VISIBLE
                            binding.animationView.cancelAnimation()
                        }
                        val show = !s.isNullOrEmpty()
                        val endDrawable = if (show) getDrawable(R.drawable.ic_tool_close_round) else null
                        binding.edtSearchLanguage.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_search, 0,
                            if (show) R.drawable.ic_tool_close_round else 0, 0
                        )
                    }, 300)
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        binding.edtSearchLanguage.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val editText = binding.edtSearchLanguage
                val drawableEnd = editText.compoundDrawables[2]
                if (drawableEnd != null) {
                    val bounds = drawableEnd.bounds
                    val x = event.x.toInt()
                    val width = editText.width
                    val paddingEnd = editText.paddingEnd
                    if (x >= width - paddingEnd - bounds.width() && x <= width - editText.paddingEnd) {
                        editText.setText("")
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    override fun initListener() {
//        loadNative()
    }

    override fun viewBinding(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(LayoutInflater.from(this))
    }

    override fun initData() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        binding.ivDone.setOnClickListener { v: View? ->
            TemporaryStorage.shouldLoadAdsLanguageScreen = false
            PreferencesHelper.putString(PreferencesHelper.KEY_LANGUAGE, adapter!!.selected)
            if (PreferencesUtils.getBoolean(PresKey.GET_START, true)) {
                logEvent(firebaseAnalytics, "language_first_time_done", "button_action", "done_click")
               // startIntro()
                startRequestAllFilePermission()
//                if (!IAPUtils.isPremium() && BillingProcessor.isIabServiceAvailable(this)) {
//                    startIAP()
//                } else {
//                    startRequestAllFilePermission()
//                }
            } else {
                logEvent(firebaseAnalytics, "language_from_main_done", "button_action", "done_click")
                setLanguageUpDate(adapter!!.selected)
                onBackPressed()
            }
        }
    }

    private fun loadNativeNomedia() {
        if (IAPUtils.isPremium()) {
            binding.layoutNative.visibility = View.GONE
            showIvDoneChecked()
            return
        }

        if (!SystemUtils.isInternetAvailable(this)) {
            binding.layoutNative.visibility = View.GONE
            showIvDoneChecked()
            return
        }
        binding.layoutNative.visibility = View.VISIBLE
        Log.d(TAG, "loadNativeNomedia: TemporaryStorage.nativeAdPreload " + TemporaryStorage.nativeAdPreload)
        if (TemporaryStorage.nativeAdPreload != null) {
            if (isFinishing || isDestroyed) {
                TemporaryStorage.nativeAdPreload = null
                TemporaryStorage.isLoadingNativeAdsLanguage = false
                TemporaryStorage.callbackNativeAdsLanguage = {  }
                return
            }
            showIvDoneChecked()

            val adView = LayoutInflater.from(this@LanguageActivity)
                .inflate(R.layout.ads_native_bot_no_media_short, null) as NativeAdView
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(adView)
            Admob.getInstance().pushAdsToViewCustom(TemporaryStorage.nativeAdPreload, adView)
            TemporaryStorage.nativeAdPreload = null
            return
        } else if (TemporaryStorage.isLoadingNativeAdsLanguage) {
            Log.d(TAG, "loadNativeNomedia: TemporaryStorage.isLoadingNativeAdsLanguage " + TemporaryStorage.isLoadingNativeAdsLanguage)
            val loadingView = LayoutInflater.from(this)
                .inflate(R.layout.ads_native_loading_short, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            TemporaryStorage.callbackNativeAdsLanguage = fun(nativeAd: NativeAd?) {
                // This return only exits the function literal, not the enclosing Activity
                if (isFinishing || isDestroyed) return

                if (nativeAd != null) {
                    Log.i("LanguageActivity", "Native ad loaded while waiting")
                    TemporaryStorage.isLoadingNativeAdsLanguage = false

                    // Show a loading-complete indicator
                    showIvDoneChecked()

                    // Inflate and bind your NativeAdView
                    val adView = LayoutInflater.from(this@LanguageActivity)
                        .inflate(R.layout.ads_native_bot_no_media_short, null) as NativeAdView
                    binding.layoutNative.removeAllViews()
                    binding.layoutNative.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                } else {
                    Log.e("LanguageActivity", "Native ad failed to load while waiting")
                    TemporaryStorage.isLoadingNativeAdsLanguage = false

                    // Clear the callback so it won't be invoked again
                    TemporaryStorage.callbackNativeAdsLanguage = { }

                    // Hide the ad container
                    binding.layoutNative.visibility = View.GONE
                    showIvDoneChecked()
                }
            }

            return
        } else {
            val loadingView = LayoutInflater.from(this)
                .inflate(R.layout.ads_native_loading_short, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    if (isFinishing || isDestroyed) return
                    showIvDoneChecked()
                    val adView = LayoutInflater.from(this@LanguageActivity)
                        .inflate(R.layout.ads_native_bot_no_media_short, null) as NativeAdView
                    binding.layoutNative.removeAllViews()
                    binding.layoutNative.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                }

                override fun onAdFailedToLoad() {
                    if (isFinishing || isDestroyed) return
                    binding.layoutNative.visibility = View.GONE
                    showIvDoneChecked()
                }
            }

            Admob.getInstance().loadNativeAd(
                applicationContext,
                getString(R.string.native_language),
                callback
            )
        }

    }
    private fun showIvDoneLoading() {
        binding.ivDone.visibility = View.INVISIBLE
        binding.ivLoading.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_loading)
            val rotate = AnimationUtils.loadAnimation(this@LanguageActivity, R.anim.rotate_loading)
            startAnimation(rotate)
            isClickable = false
        }
    }

    private fun showIvDoneChecked() {
        doneCountDown?.cancel()
        binding.ivLoading.clearAnimation()
        binding.ivLoading.visibility = View.INVISIBLE
        binding.ivDone.apply {
            visibility = View.VISIBLE
            isClickable = true
        }
    }


    private var doneCountDown: CountDownTimer? = null

    private fun startDoneCountdown() {
        showIvDoneLoading()

        doneCountDown?.cancel()
        doneCountDown = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                showIvDoneChecked()
            }
        }
        doneCountDown?.start()
    }

    private fun setLanguageUpDate(language: String) {
        if(adapter!!.selected.contains("-")) {
            val language = adapter!!.selected.split("-")[0]
            val country = adapter!!.selected.split("-")[1]
            setLanguage(language, country)
        } else {
            setLanguage(adapter!!.selected)
        }
    }

    private fun openMain() {
        val mIntent = Intent(this, MainActivity::class.java)
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(mIntent)
        finish()
        setLanguageUpDate(adapter!!.selected)
    }
//    override fun onResume() {
//        super.onResume()
//        if (TemporaryStorage.shouldLoadAdsLanguageScreen) {
//            startDoneCountdown()
//            loadNativeNomedia()
//        } else {
//            showIvDoneChecked()
//        }
//    }
    private fun startIntro() {
        IntroActivity.start(this);
        setLanguageUpDate(adapter!!.selected)
        finish()
    }

    private fun startIAP() {
        IapActivityV2.start(this);
        setLanguageUpDate(adapter!!.selected)
        finish()
    }

    private fun startRequestAllFilePermission() {
        if(intent == null) intent = Intent(this, RequestAllFilePermissionActivity::class.java)
        if (PreferencesUtils.getBoolean(PresKey.GET_START, true) || !isAcceptManagerStorage()) {
            intent?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        RequestAllFilePermissionActivity.start(this)
        finish()
        setLanguageUpDate(adapter!!.selected)
    }

    override fun onStop() {
        super.onStop()
        TemporaryStorage.nativeAdPreload = null
        PreferencesUtils.putBoolean(PresKey.FIRST_TIME_OPEN_APP, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        TemporaryStorage.callbackNativeAdsLanguage = {}
        TemporaryStorage.isLoadingNativeAdsLanguage = false
    }


    private fun scrollToSelectedLanguageSmooth(selected: String) {
        binding.rcvData.post {
            val adapter = binding.rcvData.adapter as? Language2Adapter ?: return@post
            val displayList = adapter.getDisplayList()
            val selectedPosition = displayList.indexOfFirst { it.value == selected }

            if (selectedPosition != -1) {
                val layoutManager = binding.rcvData.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager
                layoutManager?.scrollToPositionWithOffset(selectedPosition, 0)
            }
        }
    }


}