package com.pdf.pdfreader.pdfviewer.editor.screen

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import com.pdf.pdfreader.pdfviewer.editor.R
import com.pdf.pdfreader.pdfviewer.editor.databinding.ActivityPolicyBinding
import com.ezteam.baseproject.activity.BaseActivity
import com.pdf.pdfreader.pdfviewer.editor.databinding.ActivityTermAndConditionBinding
//import com.google.android.gms.ads.ez.EzAdControl
import java.io.IOException

class TermAndConditionsActivity : BaseActivity<ActivityTermAndConditionBinding>() {
    companion object {
        fun start(activity: FragmentActivity) {
            val intent = Intent(activity, TermAndConditionsActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        EzAdControl.getInstance(this).showAds()
    }

    override fun initView() {
        binding.toolbar.tvTitle.text = resources.getString(R.string.terms_and_conditions)

        try {
            val inputStream = assets.open("terms_and_conditions.html")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val str = String(buffer)
            binding.tvContent.text = Html.fromHtml(str)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun initListener() {
        binding.toolbar.icBack.setOnClickListener { finish() }
    }

    override fun initData() {}
    override fun viewBinding(): ActivityTermAndConditionBinding {
        return ActivityTermAndConditionBinding.inflate(LayoutInflater.from(this))
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}
}