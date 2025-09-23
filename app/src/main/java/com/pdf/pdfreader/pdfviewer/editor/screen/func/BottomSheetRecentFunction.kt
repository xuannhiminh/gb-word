package com.pdf.pdfreader.pdfviewer.editor.screen.func

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pdf.pdfreader.pdfviewer.editor.common.FunctionState
import com.pdf.pdfreader.pdfviewer.editor.databinding.BottomSheetRecentFuncBinding
import com.ezteam.baseproject.listener.EzItemListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetRecentFunction(
    var listener: EzItemListener<FunctionState>
) : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetRecentFuncBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetRecentFuncBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    private fun initListener() {
        binding.funcClearRecent.setOnClickListener {
            listener.onListener(FunctionState.CLEAR_RECENT)
            dismiss()
        }
    }
}