package net.galmiza.android.engine.sound

class SoundEngine {
    external fun fft(var1: FloatArray?, var2: FloatArray?, var3: Int, var4: Int)
    external fun initFSin()
    external fun toPolar(var1: FloatArray?, var2: FloatArray?, var3: Int)
    external fun shortToFloat(var1: ShortArray?, var2: FloatArray?, var3: Int)
    external fun clearFloat(var1: FloatArray?, var2: Int)
    external fun windowRectangular(var1: FloatArray?, var2: Int)
    external fun windowTriangular(var1: FloatArray?, var2: Int)
    external fun windowWelch(var1: FloatArray?, var2: Int)
    external fun windowHanning(var1: FloatArray?, var2: Int)
    external fun windowHamming(var1: FloatArray?, var2: Int)
    external fun windowBlackman(var1: FloatArray?, var2: Int)
    external fun windowNuttall(var1: FloatArray?, var2: Int)
    external fun windowBlackmanNuttall(var1: FloatArray?, var2: Int)
    external fun windowBlackmanHarris(var1: FloatArray?, var2: Int)

    companion object {
        init {
            System.loadLibrary("sound-engine")
        }
    }
}