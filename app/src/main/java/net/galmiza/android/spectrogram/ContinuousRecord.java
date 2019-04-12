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

package net.galmiza.android.spectrogram;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

/**
 * Recording service
 * Methods prepare/release initiates/releases the service and must be the very first/last calls
 * Methods start/stop starts/stop the recording service in an independent thread
 * Recorded samples are sent to the listener passed as parameter of @method start
 */
public class ContinuousRecord {
	
	// Attributes
	private AudioRecord audioRecord;
	private int samplingRate;
	private int recordLength;
	private Thread thread;
	private boolean run;
	
	/**
	 * Constructor
	 */
	ContinuousRecord(int samplingRate) {
		this.samplingRate = samplingRate;
		run = false;
	}
	
	int getBufferLength() {
		return recordLength;
	}
	
	/**
	 * Initiate the recording service
	 * The service is then ready to start recording
	 * The buffer size can be forced to be multiple of @param multiple (size in sample count)
	 * @param multiple is ineffective if set to 1
	 */
	public void prepare(int multiple) {
		
		// Setup buffer size
		int BYTES_PER_SHORT = 2;
	    recordLength = AudioRecord.getMinBufferSize(samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)/BYTES_PER_SHORT;
	    
	    // Increase buffer size so that it is a multiple of the param
	    int r = recordLength % multiple;
	    if (r>0) recordLength += (multiple-r);
	    
	    // Log value
		//Log.d("ContinuousRecord","Buffer size = "+recordLength+" samples");
	    
	    // Init audio recording from MIC
	    audioRecord = new AudioRecord(AudioSource.MIC, samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, recordLength*BYTES_PER_SHORT);
	}
	
	/**
	 *  Listener prototype used in the @method start
	 */
	public static interface OnBufferReadyListener {
        void onBufferReady(short[] buffer);
    }
	
	/**
	 * Start recording in a independent thread
	 * @param listener is call every time a sample is ready
	 */
	public void start(final OnBufferReadyListener listener) {
		if (!run && audioRecord!=null) {
			run = true;
			//Log.d("ContinuousRecord","Starting service...");
			audioRecord.startRecording(); 
	       	final short[] recordBuffer = new short[recordLength];
	
	       	thread = new Thread(() -> {
				   while (run) {
						  audioRecord.read(recordBuffer, 0, recordLength);
						  listener.onBufferReady(recordBuffer);
				   }
			   });
	       	thread.start();
			//Log.d("ContinuousRecord","Service started");
		}
	}
	
	/**
	 * Stop recording
	 * Notifies the thread to stop and wait until it stops
	 * Also stops the recording service
	 */
	public void stop() {
		if (run && audioRecord!=null) {
			//Log.d("ContinuousRecord","Stopping service...");
			run = false;
			while (thread.isAlive())
				try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
			audioRecord.stop();
			//Log.d("ContinuousRecord","Service stopped");
		}
	}
	
	/**
	 * Destroys the recording service
	 * @method start and @method stop should then not be called
	 */
	public void release() {
		if (!run && audioRecord!=null)
			audioRecord.release();
   	}
}
