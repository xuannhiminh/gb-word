package com.word.office.docx.viewer.document.editor.reader.dialog

import android.view.View

data class GuideStep(
    val targetView: View,
    val titleLines: List<String>,
    val arrowOffsetY: Float = 0f
)
