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
        String message = "0101010101010101010101010101010101000000";
        byte b0 = 0b00000000;
        byte b1 = 0b01010101;
        byte b2 = b1;
        byte b3 = b1;
        byte b4 = b1;
        byte b5 = 0b01000000;
        System.out.println(b1);
        System.out.println("start");
        byte[] bs = new byte[]{b0,b1,b2,b3,b4,b5};
        Modulator.modulate(bs);
        /*Demodulator.demodulate();
        System.out.println("finished");*/
    }
}
