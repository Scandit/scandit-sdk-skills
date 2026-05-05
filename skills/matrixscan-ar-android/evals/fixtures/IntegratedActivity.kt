package com.example.myapp

import android.content.Context
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.scandit.datacapture.barcode.ar.capture.BarcodeAr
import com.scandit.datacapture.barcode.ar.capture.BarcodeArListener
import com.scandit.datacapture.barcode.ar.capture.BarcodeArSession
import com.scandit.datacapture.barcode.ar.capture.BarcodeArSettings
import com.scandit.datacapture.barcode.ar.ui.BarcodeArRectangleHighlight
import com.scandit.datacapture.barcode.ar.ui.BarcodeArView
import com.scandit.datacapture.barcode.ar.ui.BarcodeArViewSettings
import com.scandit.datacapture.barcode.ar.ui.highlight.BarcodeArHighlightProvider
import com.scandit.datacapture.barcode.batch.data.TrackedBarcode
import com.scandit.datacapture.barcode.data.Barcode
import com.scandit.datacapture.barcode.data.Symbology
import com.scandit.datacapture.core.capture.DataCaptureContext
import com.scandit.datacapture.core.data.FrameData

class MainActivity : AppCompatActivity(), BarcodeArListener {

    private val dataCaptureContext =
        DataCaptureContext.forLicenseKey("-- ENTER YOUR SCANDIT LICENSE KEY HERE --")

    private val barcodeAr: BarcodeAr
    private lateinit var barcodeArView: BarcodeArView

    init {
        val settings = BarcodeArSettings().apply {
            enableSymbology(Symbology.EAN13_UPCA, true)
            enableSymbology(Symbology.CODE128, true)
        }
        barcodeAr = BarcodeAr(dataCaptureContext, settings)
        barcodeAr.addListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = FrameLayout(this)
        setContentView(container)
        val viewSettings = BarcodeArViewSettings()
        barcodeArView = BarcodeArView(container, barcodeAr, dataCaptureContext, viewSettings)
        barcodeArView.highlightProvider = HighlightProvider()
        barcodeArView.start()
    }

    override fun onResume() {
        super.onResume()
        barcodeArView.onResume()
    }

    override fun onPause() {
        super.onPause()
        barcodeArView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        barcodeAr.removeListener(this)
        barcodeArView.onDestroy()
    }

    override fun onSessionUpdated(
        barcodeAr: BarcodeAr,
        session: BarcodeArSession,
        frameData: FrameData
    ) {
        val added: List<TrackedBarcode> = session.addedTrackedBarcodes
        runOnUiThread {
            for (tracked in added) {
                // TODO: handle tracked.barcode.data and tracked.barcode.symbology
            }
        }
    }

    override fun onObservationStarted(barcodeAr: BarcodeAr) {}
    override fun onObservationStopped(barcodeAr: BarcodeAr) {}

    private inner class HighlightProvider : BarcodeArHighlightProvider {
        override fun highlightForBarcode(
            context: Context,
            barcode: Barcode,
            callback: BarcodeArHighlightProvider.Callback
        ) {
            callback.onData(BarcodeArRectangleHighlight(context, barcode))
        }
    }
}
