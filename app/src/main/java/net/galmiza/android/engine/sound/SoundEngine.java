package net.galmiza.android.engine.sound;

public class SoundEngine {
    static {
        System.loadLibrary("sound-engine");
    }

    public native void fft(float[] var1, float[] var2, int var3, int var4);

    public native void initFSin();

    public native void toPolar(float[] var1, float[] var2, int var3);

    public native void toCartesian(float[] var1, float[] var2, int var3);

    public native void shortToFloat(short[] var1, float[] var2, int var3);

    public native void floatToShort(float[] var1, short[] var2, int var3);

    public native void clearShort(short[] var1, int var2);

    public native void clearFloat(float[] var1, int var2);

    public native void getMeanFrequency(float[] var1, float[] var2, int var3);

    public native void window(float[] var1, int var2);

    public native void windowRectangular(float[] var1, int var2);

    public native void windowTriangular(float[] var1, int var2);

    public native void windowWelch(float[] var1, int var2);

    public native void windowHanning(float[] var1, int var2);

    public native void windowHamming(float[] var1, int var2);

    public native void windowBlackman(float[] var1, int var2);

    public native void windowNuttall(float[] var1, int var2);

    public native void windowBlackmanNuttall(float[] var1, int var2);

    public native void windowBlackmanHarris(float[] var1, int var2);
}