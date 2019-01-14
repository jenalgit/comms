package io.github.ashutoshgngwr.comms.view

import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import io.github.ashutoshgngwr.comms.R
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class MainActivityTest : TestCase("Main activity test") {

  private lateinit var activity: MainActivity

  @Before
  fun setup() {
    activity = Robolectric.setupActivity(MainActivity::class.java)
  }

  @Test
  fun `progress components should be visible on start`() {
    assert(activity.findViewById<View>(R.id.progress_bar)?.visibility == View.VISIBLE)
    assert(activity.findViewById<View>(R.id.progress_msg)?.visibility == View.VISIBLE)
  }

  @Test
  fun `record audio button should not be visible on start`() {
    assert(activity.findViewById<View>(R.id.record_audio)?.visibility == View.GONE)
  }

  @Test
  fun `permission denied error should not be visible on start`() {
    assert(activity.findViewById<View>(R.id.permission_denied_error)?.visibility == View.GONE)
  }

  @Test
  fun `permission should not be granted by default`() {
    assert(activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
  }

  @Test
  fun `permission denied error should be visible on permission denial`() {
    activity.onRequestPermissionsResult(
      MainActivity.RC_PERMISSION_RECORD_AUDIO,
      arrayOf(Manifest.permission.RECORD_AUDIO),
      intArrayOf(PackageManager.PERMISSION_DENIED)
    )
    assert(activity.findViewById<View>(R.id.permission_denied_error)?.visibility == View.VISIBLE)
  }

  @Test
  fun `permission denied error should not be visible on permission grant`() {
    activity.onRequestPermissionsResult(
      MainActivity.RC_PERMISSION_RECORD_AUDIO,
      arrayOf(Manifest.permission.RECORD_AUDIO),
      intArrayOf(PackageManager.PERMISSION_GRANTED)
    )
    assert(activity.findViewById<View>(R.id.permission_denied_error)?.visibility == View.GONE)
  }
}
