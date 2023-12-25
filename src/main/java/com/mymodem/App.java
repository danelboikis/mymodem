package com.mymodem;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;


public class App 
{
    public static void main( String[] args ) throws Exception
    {
        System.out.println("start");
        /*byte[] bs = new byte[]{0x0, (byte)0xff, 0x0};
        Modulator.modulate(bs);*/
        Demodulator.demodulate();
        System.out.println("finished");
    }
}
