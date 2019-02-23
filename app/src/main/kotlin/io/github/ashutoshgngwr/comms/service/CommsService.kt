package io.github.ashutoshgngwr.comms.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.github.ashutoshgngwr.comms.R
import io.github.ashutoshgngwr.comms.service.worker.ReceiveAndPlayAudioWorker
import io.github.ashutoshgngwr.comms.service.worker.RecordAndSendAudioWorker
import io.github.ashutoshgngwr.comms.view.MainActivity
import java.net.InetAddress
import java.net.NetworkInterface


class CommsService : Service() {

  companion object {
    private val TAG = CommsService::class.java.simpleName
    private const val ONGOING_NOTIFICATION_ID = 0x293
    private const val COMMS_PORT = 55756

    const val ACTION_COMMS_READY = "comms_ready"
    const val ACTION_RECORD_START = "record_start"
    const val ACTION_RECORD_STOP = "record_stop"

    private lateinit var recordAndSendAudioWorker: RecordAndSendAudioWorker
    private lateinit var receiveAndPlayAudioWorker: ReceiveAndPlayAudioWorker

    private lateinit var broadcastAddress: InetAddress
  }

  private val updateReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      when (intent?.action) {
        ACTION_RECORD_START -> {
          recordAndSendAudioWorker.resumeWorker()
          Log.d(TAG, "start recording and sending...")
        }

        ACTION_RECORD_STOP -> {
          recordAndSendAudioWorker.pauseWorker()
          Log.d(TAG, "stop recording and sending...")
        }
      }
    }
  }

  override fun onBind(p0: Intent?): IBinder? {
    return null
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    startForeground(ONGOING_NOTIFICATION_ID, buildForegroundNotification())
    return START_STICKY
  }

  override fun onCreate() {
    broadcastAddress = getBroadcastAddress()

    val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
    receiveAndPlayAudioWorker = ReceiveAndPlayAudioWorker(
      broadcastAddress,
      COMMS_PORT,
      audioManager.generateAudioSessionId()
    )
    receiveAndPlayAudioWorker.start()

    recordAndSendAudioWorker = RecordAndSendAudioWorker(broadcastAddress, COMMS_PORT)
    recordAndSendAudioWorker.start()

    val intentFilter = IntentFilter()
    intentFilter.addAction(ACTION_RECORD_START)
    intentFilter.addAction(ACTION_RECORD_STOP)
    LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, intentFilter)

    sendUpdate(ACTION_COMMS_READY)
  }

  override fun onDestroy() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver)
    receiveAndPlayAudioWorker.stopWorker()
    recordAndSendAudioWorker.stopWorker()
  }

  private fun sendUpdate(action: String) {
    LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(action))
  }

  private fun getBroadcastAddress(): InetAddress {
    val networkInterfaces = NetworkInterface.getNetworkInterfaces()
    for (i in networkInterfaces) {
      if (i.isLoopback) {
        continue
      }

      for (interfaceAddress in i.interfaceAddresses) {
        return interfaceAddress.broadcast ?: continue
      }
    }
    return InetAddress.getByName("255.255.255.255")
  }

  private fun buildForegroundNotification(): Notification? {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val channel = NotificationChannel(
        "default",
        getText(R.string.app_name),
        NotificationManager.IMPORTANCE_DEFAULT
      )
      channel.setSound(null, null)
      notificationManager.createNotificationChannel(channel)
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
      this, 0x39,
      Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
    )
    return NotificationCompat.Builder(this, "default")
      .setOngoing(true)
      .setContentTitle(getText(R.string.app_name))
      .setTicker(getText(R.string.app_name))
      .setContentIntent(pendingIntent)
      .build()
  }
}
