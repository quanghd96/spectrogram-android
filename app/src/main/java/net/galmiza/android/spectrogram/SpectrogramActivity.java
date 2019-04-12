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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.galmiza.android.engine.sound.SoundEngine;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point of the application
 * Handles:
 *  recording service
 *  fft processing by calling native functions
 *  view updates
 *  activity events:
 *   onCreate, onDestroy,
 *   onCreateOptionsMenu, onOptionsItemSelected, (top menu banner)
 *   onActivityResult (response from external activities)
 */
public class SpectrogramActivity extends AppCompatActivity {
	
	// Constant
	static final float PI = (float) Math.PI;
	static final int INTENT_SETTINGS = 0;
    static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 0;
	
	// Attributes
	private ActionBar actionBar;
	private FrequencyView frequencyView;
	private TimeView timeView;
	private ContinuousRecord recorder;
	private SoundEngine nativeLib;
	private Menu menu;
	private int samplingRate = 44100;
	private int fftResolution;
	
	// Buffers
	private List<short[]> bufferStack; // Store trunks of buffers
	private short[] fftBuffer; // buffer supporting the fft process
	private float[] re; // buffer holding real part during fft process
	private float[] im; // buffer holding imaginary part during fft process
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Share core
		Misc.setAttribute("activity", this);
		
		// Load preferences
		loadPreferences();
		
		// JNI interface
		nativeLib = new SoundEngine();
		nativeLib.initFSin();
		
		// Recorder & player
		recorder = new ContinuousRecord(samplingRate);
		
		// Create view for frequency display
		setContentView(R.layout.main);
		frequencyView = findViewById(R.id.frequency_view);
		timeView = findViewById(R.id.time_view);
		if (Misc.getPreference(this, "keep_screen_on", false))
			frequencyView.setKeepScreenOn(true);
		frequencyView.setFFTResolution(fftResolution);
        timeView.setFFTResolution(fftResolution);
		frequencyView.setSamplingRate(samplingRate);
		
		// Color mode
		boolean nightMode = Misc.getPreference(this, "night_mode", true);
		if (!nightMode)	{
        	frequencyView.setBackgroundColor(Color.WHITE);
        	timeView.setBackgroundColor(Color.WHITE);
        } else {
        	frequencyView.setBackgroundColor(Color.BLACK);
        	timeView.setBackgroundColor(Color.BLACK);
        }

        /*// Prepare screen
        getSupportActionBar().hide();
        if (util.Misc.getPreference(this, "hide_status_bar", false))
        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        
		// Action bar
		actionBar = getSupportActionBar();
		actionBar.setTitle(getString(R.string.app_name));
		actionBar.setSubtitle(getString(R.string.app_subtitle));

		// Request record audio permission
		if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            loadEngine();
            updateHeaders();
        } else {
			ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
		}
	}
	
	
	/**
	 * Update the text in frame headers
	 */
	private void updateHeaders() {
		
		// Time view
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		TextView time = findViewById(R.id.textview_time_header);
		time.setText(String.format(getString(R.string.view_header_time), df.format(1000.0f*fftBuffer.length/samplingRate)));
			
		// Freqnecy view
		TextView frequency = findViewById(R.id.textview_frequency_header);
		String window = Misc.getPreference(
				this,
				"window_type",
				getString(R.string.preferences_window_type_default_value));
		frequency.setText(String.format(
				getString(R.string.view_header_frequency), fftResolution, window));
		
		// Color
		boolean nightMode = Misc.getPreference(this, "night_mode", false);
		if (!nightMode) {
			time.setBackgroundColor(Color.LTGRAY);
			frequency.setBackgroundColor(Color.LTGRAY);
			time.setTextColor(Color.BLACK);
			frequency.setTextColor(Color.BLACK);
		} else {
			time.setBackgroundColor(Color.DKGRAY);
			frequency.setBackgroundColor(Color.DKGRAY);
			time.setTextColor(Color.WHITE);
			frequency.setTextColor(Color.WHITE);
		}
	}
	
	/**
	 * Control recording service
	 */
	private void startRecording() {
		recorder.start(recordBuffer -> getTrunks(recordBuffer));
	}
	private void stopRecording() {
		recorder.stop();
	}

    /**
     * Handles response to permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadEngine();
                    updateHeaders();
                }
                return;
            }
        }
    }

	/**
	 * Handles interactions with the menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		this.menu = menu;
		menu.findItem(R.id.action_bar_menu_play).setVisible(false);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.action_bar_menu_settings:
			Intent intent = new Intent(this,PreferencesActivity.class);
			startActivityForResult(intent, INTENT_SETTINGS);
			return true;
		case R.id.action_bar_menu_play:
			menu.findItem(R.id.action_bar_menu_play).setVisible(false);
			menu.findItem(R.id.action_bar_menu_pause).setVisible(true);
			startRecording();
			return true;
		case R.id.action_bar_menu_pause:
			menu.findItem(R.id.action_bar_menu_pause).setVisible(false);
			menu.findItem(R.id.action_bar_menu_play).setVisible(true);
			stopRecording();
			return true;
		}
		return false;
	}
	
	/**
	 * Handles updates from the preference activity
	 */
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent intent) {
	    //if (resultCode == Activity.RESULT_OK) {
	    	if (requestCode == INTENT_SETTINGS) {
	    		
	    		// Stop and release recorder if running
	    		recorder.stop();
	    		recorder.release();
	    		
	    		// Update preferences
	    		loadPreferences();
	    		
	    		// Notify view
	    		frequencyView.setFFTResolution(fftResolution);
	    		timeView.setFFTResolution(fftResolution);

                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    loadEngine();
                    updateHeaders();
                }
	    		
	    		// Update color mode
	    		boolean nightMode = Misc.getPreference(this, "night_mode", false);
	            if (!nightMode)	{
	            	frequencyView.setBackgroundColor(Color.WHITE);
	            	timeView.setBackgroundColor(Color.WHITE);
	            } else {
	            	frequencyView.setBackgroundColor(Color.BLACK);
	            	timeView.setBackgroundColor(Color.BLACK);
	            }
	    	}
	    //}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Stop input streaming
		recorder.stop();
		recorder.release();
	}
	
	
	/**
	 * Make sure onDestroy is called
	 * NOTE: crash with illegalstateexception: no activity if this extends ActionBarActivity
	 */
	/*@Override
	public void onBackPressed() {
		Log.d("SpectrogramActivity","onBackPressed");
		onDestroy();
		finish();
	}*/
	


	/**
	 * Load preferences
	 */
	private void loadPreferences() {
		fftResolution = Integer.parseInt(Misc.getPreference(this, "fft_resolution", getString(R.string.preferences_fft_resolution_default_value)));
	}

	
	/**
	 * Initiates the recording service
	 * Creates objects to handle recording and FFT processing
	 */
	private void loadEngine() {
		
		// Stop and release recorder if running
		recorder.stop();
		recorder.release();
		
		// Prepare recorder
		recorder.prepare(fftResolution); // Record buffer size if forced to be a multiple of the fft resolution
		
		// Build buffers for runtime
		int n = fftResolution;
		fftBuffer = new short[n];
		re = new float[n];
		im = new float[n];
 		bufferStack = new ArrayList<short[]>();
		int l = recorder.getBufferLength()/(n/2);
		for (int i=0; i<l+1; i++) //+1 because the last one has to be used again and sent to first position
			bufferStack.add(new short[n/2]); // preallocate to avoid new within processing loop

        // Start recording
        startRecording();
        
		// Log
		//Log.d("recorder.getBufferLength()", recorder.getBufferLength()+" samples");
		//Log.d("bufferStack.size()", bufferStack.size()+" trunks");
	}
	
	
	/**
	 * Called every time the microphone record a sample
	 * Divide into smaller buffers (of size=resolution) which are overlapped by 50%
	 * Send these buffers for FFT processing (call to process())
	 */
	private void getTrunks(short[] recordBuffer) {
		int n = fftResolution;
		
		// Trunks are consecutive n/2 length samples		
		for (int i=0; i<bufferStack.size()-1; i++)
			System.arraycopy(recordBuffer, n/2*i, bufferStack.get(i+1), 0, n/2);
		
		// Build n length buffers for processing
		// Are build from consecutive trunks
		for (int i=0; i<bufferStack.size()-1; i++) {
			System.arraycopy(bufferStack.get(i), 0, fftBuffer, 0, n/2);
			System.arraycopy(bufferStack.get(i+1), 0, fftBuffer, n/2, n/2);
			process(); 
		}
		
		// Last item has not yet fully be used (only its first half)
		// Move it to first position in arraylist so that its last half is used
		short[] first = bufferStack.get(0);
		short[] last = bufferStack.get(bufferStack.size()-1);
		System.arraycopy(last, 0, first, 0, n/2);
	}
	
	/**
	 * Processes the sound waves
	 * Computes FFT
	 * Update views
	 */
	private void process() {
		int n = fftResolution;
		int log2_n = (int) (Math.log(n)/Math.log(2));

		nativeLib.shortToFloat(fftBuffer, re, n);
		nativeLib.clearFloat(im, n);	// Clear imaginary part
		timeView.setWave(re);
		
		// Windowing to reduce spectrum leakage
		String window = Misc.getPreference(
				this,
				"window_type",
				getString(R.string.preferences_window_type_default_value));
		
		if (window.equals("Rectangular"))			nativeLib.windowRectangular(re, n);
		else if (window.equals("Triangular"))		nativeLib.windowTriangular(re, n);
		else if (window.equals("Welch"))			nativeLib.windowWelch(re, n);
		else if (window.equals("Hanning"))			nativeLib.windowHanning(re, n);
		else if (window.equals("Hamming"))			nativeLib.windowHamming(re, n);
		else if (window.equals("Blackman"))			nativeLib.windowBlackman(re, n);
		else if (window.equals("Nuttall"))			nativeLib.windowNuttall(re, n);
		else if (window.equals("Blackman-Nuttall"))	nativeLib.windowBlackmanNuttall(re, n);
		else if (window.equals("Blackman-Harris"))	nativeLib.windowBlackmanHarris(re, n);
		
		nativeLib.fft(re, im, log2_n, 0);	// Move into frquency domain 
		nativeLib.toPolar(re, im, n);	// Move to polar base

		frequencyView.setMagnitudes(re);
		runOnUiThread(() -> {
			frequencyView.invalidate();
			timeView.invalidate();
		});
	}
	
	
	/**
	 * Switch visibility of the views as user click on view headers
	 */
	public void onTimeViewHeaderClick(View view) {
		System.out.println(timeView.getVisibility());
		if (timeView.getVisibility() == View.GONE)	timeView.setVisibility(View.VISIBLE);
		else										timeView.setVisibility(View.GONE);
	}
	public void onFrequencyViewHeaderClick(View view) {
		System.out.println(frequencyView.getVisibility());
		if (frequencyView.getVisibility() == View.GONE)	frequencyView.setVisibility(View.VISIBLE);
		else											frequencyView.setVisibility(View.GONE);
	}

}
