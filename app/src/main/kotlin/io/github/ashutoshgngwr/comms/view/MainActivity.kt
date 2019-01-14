package io.github.ashutoshgngwr.comms.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

  companion object {
    const val RC_PERMISSION_RECORD_AUDIO = 0x29
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
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
          // Permission was granted
        } else {
          // Permission was denied
        }
        return
      }
    }
  }
}
