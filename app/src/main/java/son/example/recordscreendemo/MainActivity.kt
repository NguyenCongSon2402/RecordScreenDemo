package son.example.recordscreendemo

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import son.example.recordscreendemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ::handlePermissionResult
    )

    private val startMediaProjection = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ::launchRecordingService
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.start.setOnClickListener {
            checkPermissionOrStartRecording()
        }

        binding.stop.setOnClickListener {
            stopProjection()
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val path = intent.getStringExtra(MediaProjectionService.FILE_PATH)
                    binding.videoView.setVideoPath(path)
                    binding.videoView.isVisible = true
                    binding.videoView.start()
                }
            },
            IntentFilter(MediaProjectionService.RECORDING_COMPLETE_ACTION)
        )
    }

    private fun checkPermissionOrStartRecording() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startProjection()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                showPermissionRationale()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            startProjection()
        } else {
            showPermissionRationale()
        }
    }

    private fun showPermissionRationale() {
        Snackbar.make(
            this@MainActivity.window.decorView,
            "Audio permission is required",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Permissions") {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }.show()
    }

    private fun launchRecordingService(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            ContextCompat.startForegroundService(
                this,
                Intent(this, MediaProjectionService::class.java).apply {
                    putExtra(MediaProjectionService.SCREEN_CAPTURE_INTENT_RESULT_DATA, result.data)
                }
            )
        } else {
            showRecordingFailed()
        }
    }

    private fun showRecordingFailed() {
        Snackbar.make(this.window.decorView, "Recording Cancelled", Snackbar.LENGTH_LONG).show()
    }

    private fun startProjection() {
        val projectionManager =
            ContextCompat.getSystemService(this, MediaProjectionManager::class.java)
        if (projectionManager != null) {
            startMediaProjection.launch(projectionManager.createScreenCaptureIntent())
        }
    }

    private fun stopProjection() {
        stopService(Intent(this, MediaProjectionService::class.java))
    }
}