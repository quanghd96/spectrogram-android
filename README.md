## Synopsis

[Spectrogram-Android] is a mobile application available on the Google Play store.   
It was initially developed as a basic tool to study outdoor sound properties.  
It is and will remain free to use and ad-free.

##### Key features  

  - Realtime time view and frequency view of surrouding sound
  - FFT size from 32 to 65536
  - Nine windowing functions
  - Night/day and portrait/landscape modes

##### Snapshots
  


## Installation and configuration

##### Installation

  - Install Android Studio 2.3.2, Android SDK and Android NDK
  - From the "Android SDK Manager", install the libraries
    - "Android Support Library (Obsolete)"
    - "Android Support Repository" 

Congratulations, you are ready to run the application!


## Implementation

The application follows Android guidelines in terms of Activity lifecycle, View inflation and Preferences management.  
It uses native code (C code) to reduce FFT computation time. A JNI bridge defines the binding between Java methods and C functions.  
The code is very minimalist. Below the exhaustive list of source files.  

**Edit**:  
Note that the C code and JNI bridge have been moved into the dedicated repository *sound-engine-android*. They are now packaged into .so files and a .jar file attached to this project.

##### Java side code

  - **SpectrogramActivity.java** (entry point of the application, renders TimeView and FrequencyView, has access to native code to process audio samples, subscribes to audio recording service)
  - **PreferencesActivity.java** (simple interface to manage user preferences)
  - **TimeView.java** (wave signal rendering, shown on top of the application)
  - **FrequencyView.java** (spectrogram rendering, shown below the wave signal)
  - **ContinuousRecord.java** (background service, continuously records audio, calls a callback for each sample recorded)
  - **Misc.java** (various function to ease preferences manipulation)

##### Native side code

  - **SoundEngine.java** (JNI bridge in Java, list of Java methods that are implemented in C)
  - **jni.c** (JNI bridge in C, allows Java calls to be routed to C functions)
  - **main.c** (FFT, windowing ... all sound processing functions)


Basically, an instance of ContinuousRecord is run at startup and continuously records audio samples from the microphone.   
Using a callback, each sample is processed within SpectrogramActivity. Results are then sent to TimeView and FrequencyView which update their respective content.  


## Development
Have killer features in mind you want to implement?  
You are very welcome to contribute to the project!  

## Todo
  - Add extra languages
  - Add frequency range filter
  - Add unit test cases (yeah I know...)
  - Add ability to play sound from the spectrogram
  - Add high resolution spectrogram export feature

## License
[zlib]

[zlib]: <https://en.wikipedia.org/wiki/Zlib_License>