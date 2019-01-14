package io.github.ashutoshgngwr.comms.view

import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityTest : TestCase("Main activity test") {

  private var activity: MainActivity? = null

  @Before
  fun setup() {
    activity = Robolectric.setupActivity(MainActivity::class.java)
  }

  @Test
  @Throws(Exception::class)
  fun `activity should not be null`() {
    assertTrue(activity != null)
  }
}
