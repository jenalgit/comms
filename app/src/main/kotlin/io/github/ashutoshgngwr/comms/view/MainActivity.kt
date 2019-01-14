package io.github.ashutoshgngwr.comms.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.github.ashutoshgngwr.comms.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  companion object {
    const val RC_PERMISSION_RECORD_AUDIO = 0x29
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
        arrayOf(Manifest.permission.READ_CONTACTS),
        RC_PERMISSION_RECORD_AUDIO
      )
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>, grantResults: IntArray
  ) {
    when (requestCode) {
      RC_PERMISSION_RECORD_AUDIO -> {
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
          hideRecordAudioButton()
          showProgress(R.string.searching_channels)
        } else {
          hideProgress()
          hideRecordAudioButton()
          showPermissionDeniedError()
        }
        return
      }
    }
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
