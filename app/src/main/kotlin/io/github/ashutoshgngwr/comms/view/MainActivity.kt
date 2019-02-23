package io.github.ashutoshgngwr.comms.view

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.github.ashutoshgngwr.comms.R
import io.github.ashutoshgngwr.comms.service.CommsService
import io.github.ashutoshgngwr.comms.service.worker.ReceiveAndPlayAudioWorker
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  companion object {
    const val RC_PERMISSION_RECORD_AUDIO = 0x29
  }

  private val updateReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      when (intent?.action) {
        CommsService.ACTION_COMMS_READY -> {
          hideProgress()
          showRecordAudioButton()
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    if (ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.RECORD_AUDIO
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.RECORD_AUDIO),
        RC_PERMISSION_RECORD_AUDIO
      )
    } else {
      startCommsService()
    }

    record_audio.setOnTouchListener { _, motionEvent ->
      when (motionEvent.action) {
        MotionEvent.ACTION_DOWN -> {
          sendAction(CommsService.ACTION_RECORD_START)
          false
        }
        MotionEvent.ACTION_UP -> {
          sendAction(CommsService.ACTION_RECORD_STOP)
          false
        }
        else -> {
          false
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    val intentFilter = IntentFilter()
    intentFilter.addAction(CommsService.ACTION_COMMS_READY)
    LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, intentFilter)
  }

  override fun onPause() {
    super.onPause()
    LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver)
  }

  override fun onDestroy() {
    super.onDestroy()
    stopService(Intent(this, CommsService::class.java))
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>, grantResults: IntArray
  ) {
    when (requestCode) {
      RC_PERMISSION_RECORD_AUDIO -> {
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
          startCommsService()
        } else {
          hideProgress()
          hideRecordAudioButton()
          showPermissionDeniedError()
        }
        return
      }
    }
  }

  private fun startCommsService() {
    hideRecordAudioButton()
    showProgress(R.string.searching_channels)
    startService(Intent(this, CommsService::class.java))
  }

  private fun sendAction(action: String) {
    LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(action))
  }

  private fun showProgress(msgId: Int) {
    progress_bar.visibility = View.VISIBLE
    progress_msg.visibility = View.VISIBLE
    progress_msg.setText(msgId)
  }

  private fun hideProgress() {
    progress_bar.visibility = View.GONE
    progress_msg.visibility = View.GONE
  }

  private fun showRecordAudioButton() {
    record_audio.visibility = View.VISIBLE
  }

  private fun hideRecordAudioButton() {
    record_audio.visibility = View.INVISIBLE
  }

  private fun showPermissionDeniedError() {
    permission_denied_error.visibility = View.VISIBLE
  }
}
