package com.word.office.docx.viewer.document.editor.reader.utils.createPdf;

public interface OnPDFCreatedInterface {
    void onPDFCreationStarted();
    void onPDFCreated(boolean success, String path);
}
