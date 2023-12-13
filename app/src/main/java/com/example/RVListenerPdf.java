package com.example;

import com.example.camscanner.models.ModelPdf;
import com.example.camscanner.ui.pdf.AdapterPdf;

public interface RVListenerPdf {

    void onPdfClick(ModelPdf modelPdf, int position);
    void onPdfMoreClick(ModelPdf modelPdf, int position, AdapterPdf.HolderPdf holder);

}
