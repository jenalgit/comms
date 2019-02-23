package io.github.ashutoshgngwr.comms.service.worker

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

class RecordAndSendAudioWorker(destAddress: InetAddress, targetPort: Int) : Thread() {

  companion object {
    private val TAG = RecordAndSendAudioWorker::class.java.simpleName
    private const val SAMPLE_RATE = 44100

    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
      SAMPLE_RATE,
      AudioFormat.CHANNEL_IN_MONO,
      AudioFormat.ENCODING_PCM_16BIT
    )

    private val audioRecord = AudioRecord(
      MediaRecorder.AudioSource.DEFAULT,
      SAMPLE_RATE,
      AudioFormat.CHANNEL_IN_MONO,
      AudioFormat.ENCODING_PCM_16BIT,
      BUFFER_SIZE
    )

    private lateinit var socket: DatagramSocket
    private lateinit var address: InetAddress
    private var port: Int = 0
    private var isPaused = true
  }

  init {
    socket = DatagramSocket()
    socket.broadcast = true
    socket.soTimeout = 5000
    address = destAddress
    port = targetPort
  }

  override fun run() {
    try {
      val buffer = ByteArray(BUFFER_SIZE)
      audioRecord.startRecording()
      while (!interrupted()) {
        if (isPaused) {
          Thread.sleep(250)
          continue
        }
        val len = audioRecord.read(buffer, 0, BUFFER_SIZE)
        if (len > -1) {
          // todo process audio before sending
          socket.send(DatagramPacket(buffer, len, address, port))
        }
      }
    } catch (e: IOException) {
      Log.w(TAG, e)
    } catch (e: SocketException) {
      Log.w(TAG, e)
    } catch (e: InterruptedException) {
      Log.i(TAG, "interrupted! finishing cleanup...")
    } finally {
      audioRecord.stop()
      audioRecord.release()
      socket.close()
    }
  }

  fun pauseWorker() {
    isPaused = true
  }

  fun resumeWorker() {
    isPaused = false
  }

  fun stopWorker() {
    interrupt()
  }
}
