package com.example.camscanner.models;

import android.graphics.Bitmap;
import android.net.Uri;

public class ModelPdfView {

    Uri pdfUri;
    int pageNummber;
    int pageCount;
    Bitmap bitmap;

    public ModelPdfView(Uri pdfUri, int pageNummber, int pageCount, Bitmap bitmap) {
        this.pdfUri = pdfUri;
        this.pageNummber = pageNummber;
        this.pageCount = pageCount;
        this.bitmap = bitmap;
    }

    public Uri getPdfUri() {
        return pdfUri;
    }

    public void setPdfUri(Uri pdfUri) {
        this.pdfUri = pdfUri;
    }

    public int getPageNummber() {
        return pageNummber;
    }

    public void setPageNummber(int pageNummber) {
        this.pageNummber = pageNummber;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
