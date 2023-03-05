/**
 * Spectrogram Android application
 * Copyright (c) 2013 Guillaume Adam  http://www.galmiza.net/
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the use of this software.
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it freely,
 * subject to the following restrictions:
 * 1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */
package net.galmiza.android.spectrogram

import android.Manifest
import android.content.Context
import android.media.AudioRecord
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.MediaRecorder.AudioSource

/**
 * Recording service
 * Methods prepare/release initiates/releases the service and must be the very first/last calls
 * Methods start/stop starts/stop the recording service in an independent thread
 * Recorded samples are sent to the listener passed as parameter of @method start
 */
class ContinuousRecord
/**
 * Constructor
 */ internal constructor(private val samplingRate: Int, private val context: Context) {
    // Attributes
    private var audioRecord: AudioRecord? = null
    var bufferLength = 0
        private set
    private var thread: Thread? = null
    var isRun = false
        private set

    /**
     * Initiate the recording service
     * The service is then ready to start recording
     * The buffer size can be forced to be multiple of @param multiple (size in sample count)
     * @param multiple is ineffective if set to 1
     */
    fun prepare(multiple: Int) {

        // Setup buffer size
        val bytesPerShort = 2
        bufferLength = AudioRecord.getMinBufferSize(
            samplingRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ) / bytesPerShort

        // Increase buffer size so that it is a multiple of the param
        val r = bufferLength % multiple
        if (r > 0) bufferLength += multiple - r

        // Log value
        //Log.d("ContinuousRecord","Buffer size = "+recordLength+" samples");

        // Init audio recording from MIC
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        audioRecord = AudioRecord(
            AudioSource.MIC,
            samplingRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferLength * bytesPerShort
        )
    }

    /**
     * Listener prototype used in the @method start
     */
    interface OnBufferReadyListener {
        fun onBufferReady(buffer: ShortArray?)
    }

    /**
     * Start recording in a independent thread
     * @param listener is call every time a sample is ready
     */
    fun start(listener: OnBufferReadyListener) {
        if (isRun) {
            return
        }
        audioRecord?.let {
            isRun = true
            //Log.d("ContinuousRecord","Starting service...");
            it.startRecording()
            val recordBuffer = ShortArray(bufferLength)
            val thread = Thread {
                while (isRun) {
                    it.read(recordBuffer, 0, bufferLength)
                    listener.onBufferReady(recordBuffer)
                }
            }
            this.thread = thread
            thread.start()
            //Log.d("ContinuousRecord","Service started");
        }
    }

    /**
     * Stop recording
     * Notifies the thread to stop and wait until it stops
     * Also stops the recording service
     */
    fun stop() {
        if (!isRun) {
            return
        }

        audioRecord?.let { record ->
            //Log.d("ContinuousRecord","Stopping service...");
            isRun = false
            thread?.let {
                while (it.isAlive) try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            record.stop()
            //Log.d("ContinuousRecord","Service stopped");
        }
    }

    /**
     * Destroys the recording service
     * @method start and @method stop should then not be called
     */
    fun release() {
        if (isRun) {
            return
        }
        audioRecord?.release()
    }
}