package com.example.intunedynamicfeaturebug

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import timber.log.Timber
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var dynamicFeatureSessionId = 0
    private lateinit var splitInstallManager: SplitInstallManager
    // Creates a listener for request status updates.
    private val listener = SplitInstallStateUpdatedListener { state ->
        if (state.sessionId() == dynamicFeatureSessionId) {
            when (state.status()) {
                SplitInstallSessionStatus.DOWNLOADING -> {
                    val totalBytes = state.totalBytesToDownload()
                    val progress = state.bytesDownloaded()
                    Timber.d("Dynamic feature download progress: $progress bytes so far/$totalBytes total bytes for ${(progress.toFloat() / totalBytes.toFloat()) * 100.0F}%")
                }
                SplitInstallSessionStatus.INSTALLED -> {
                    Timber.d("Dynamic feature module installed")
                    startActivity(Intent().setClassName(applicationContext.packageName,
                        "com.example.dynamicfeature.DynamicMainActivity"))
                }
                else -> Timber.d("Dynamic feature req status update: ${state.status()}")
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        splitInstallManager = SplitInstallManagerFactory.create(this)
        splitInstallManager.registerListener(listener)
        val launchBtn = findViewById<Button>(R.id.launch_btn)
        launchBtn.setOnClickListener {
            val req = SplitInstallRequest.newBuilder()
                .addModule("dynamicfeature")
                .build()
            splitInstallManager.startInstall(req)
                .addOnSuccessListener { sessionId: Int ->
                    Timber.d("dynamic feature req succeeded")
                    dynamicFeatureSessionId = sessionId
                }
                .addOnFailureListener { exception: Exception? ->
                    Timber.e(
                        exception,
                        "dynamic feature module install req failed"
                    )
                }
        }
    }
}