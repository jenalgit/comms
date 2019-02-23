package io.github.ashutoshgngwr.comms.service.worker

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.io.IOException
import java.net.*
import java.net.NetworkInterface.getNetworkInterfaces


class ReceiveAndPlayAudioWorker(address: InetAddress, port: Int, sessionId: Int) : Thread() {

  companion object {
    private val TAG = ReceiveAndPlayAudioWorker::class.java.simpleName
    private const val SAMPLE_RATE = 44100

    private val BUFFER_SIZE = AudioTrack.getMinBufferSize(
      SAMPLE_RATE,
      AudioFormat.CHANNEL_OUT_MONO,
      AudioFormat.ENCODING_PCM_16BIT
    )

    private val buffer = ByteArray(BUFFER_SIZE)

    private lateinit var selfAddress: String
    private lateinit var audioTrack: AudioTrack
    private lateinit var socket: DatagramSocket
  }

  init {
    selfAddress = getSelfAddress()
    audioTrack = AudioTrack(
      AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
        .build(),
      AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setSampleRate(SAMPLE_RATE)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .build(),
      BUFFER_SIZE,
      AudioTrack.MODE_STREAM,
      sessionId
    )

    socket = DatagramSocket(port, address)
    socket.broadcast = true
    socket.soTimeout = 5000
  }

  override fun run() {
    try {
      audioTrack.play()
      while (!interrupted()) {
        try {
          val packetIn = DatagramPacket(buffer, BUFFER_SIZE)
          socket.receive(packetIn)
          // TODO replays recorded audio on host device
          if (packetIn.address.hostAddress == selfAddress) {
            continue
          }

          audioTrack.write(packetIn.data, packetIn.offset, packetIn.length)
        } catch (_: SocketTimeoutException) {
          Log.i(TAG, "No message has been received in last ${socket.soTimeout}ms...")
        }
      }
    } catch (e: IOException) {
      Log.w(TAG, e)
    } catch (e: SocketException) {
      Log.w(TAG, e)
    } catch (e: InterruptedException) {
      Log.i(TAG, "interrupted! finishing cleanup...")
    } finally {
      audioTrack.stop()
      audioTrack.release()
      socket.close()
    }
  }

  fun stopWorker() {
    interrupt()
  }

  private fun getSelfAddress(): String {
    try {
      val interfaces = getNetworkInterfaces()
      for (i in interfaces) {
        val inetAddresses = i.interfaceAddresses
        for (inetAddress in inetAddresses) {
          if (!inetAddress.address.isLoopbackAddress && inetAddress.address.hostAddress.indexOf(':') < 0) {
            return inetAddress.address.hostAddress
          }
        }
      }
    } catch (ignored: Exception) {
    }
    return "192.168.43.1"
  }
}
