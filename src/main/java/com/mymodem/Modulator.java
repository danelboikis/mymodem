package com.mymodem;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;

public class Modulator {
    
    /** 
     * @param data - the data in bytes
     */
    public static void modulate(byte[] data) throws Exception{
        double carrierFrequency = Double.valueOf(ConfigFile.getConfigFile().getValue("carrierFrequency"));
        double frequencyChange = Double.valueOf(ConfigFile.getConfigFile().getValue("frequencyChange"));

        int dataRate = Integer.valueOf(ConfigFile.getConfigFile().getValue("dataRate")); // time for one bit of data in ms
        int sampleRate = Integer.valueOf(ConfigFile.getConfigFile().getValue("sampleRate")); // number of samples taken per second

        AudioFormat audioFormat = new AudioFormat(sampleRate,
                                                8,
                                                1,
                                                Boolean.valueOf(ConfigFile.getConfigFile().getValue("signed")), 
                                                Boolean.valueOf(ConfigFile.getConfigFile().getValue("bigEndian")));

        int numSamples = (int) Math.ceil((((double) dataRate) / 1000.0) * sampleRate); // number of samples for one bit

        SourceDataLine line = AudioSystem.getSourceDataLine(audioFormat);
        line.open(audioFormat);
        line.start();

        byte[] bData = new byte[data.length * 8 * numSamples]; // buffered data
        int bDataIndex = 0;

        for (byte b : data) {
            for (int i = 7; i >= 0; i--) { // for each bit
                double frequency = carrierFrequency + ((b >> i) & 0x1) * frequencyChange;
                
                for (int j = 0; j < numSamples; j++) {
                    double time = ((double) j) / ((double)sampleRate); // sample / (sample / second) = second

                    double value = Math.sin(2.0 * Math.PI * frequency * time); // wave
                    byte sample = (byte) (value * Byte.MAX_VALUE); // convert to sample size with 8 bits

                    bData[bDataIndex] = sample;
                    bDataIndex++;
                }
            }
        }

        line.write(bData, 0, bData.length);
        line.drain();
        line.close();

        writeToFile("output.wav", bData, audioFormat);
    }

    public static void modulate16bit(byte[] data) throws Exception{
        double carrierFrequency = Double.valueOf(ConfigFile.getConfigFile().getValue("carrierFrequency"));
        double frequencyChange = Double.valueOf(ConfigFile.getConfigFile().getValue("frequencyChange"));

        int dataRate = Integer.valueOf(ConfigFile.getConfigFile().getValue("dataRate")); // time for one bit of data in ms
        int sampleRate = Integer.valueOf(ConfigFile.getConfigFile().getValue("sampleRate")); // number of samples taken per second

        AudioFormat audioFormat = new AudioFormat(sampleRate,
                16,
                1,
                Boolean.valueOf(ConfigFile.getConfigFile().getValue("signed")),
                false);

        int numSamples = (int) Math.ceil((((double) dataRate) / 1000.0) * sampleRate); // number of samples for one bit

        SourceDataLine line = AudioSystem.getSourceDataLine(audioFormat);
        line.open(audioFormat);
        line.start();

        byte[] bData = new byte[data.length * 8 * numSamples * 2]; // buffered data
        int bDataIndex = 0;

        for (byte b : data) {
            for (int i = 7; i >= 0; i--) { // for each bit
                double frequency = carrierFrequency + ((b >> i) & 0x1) * frequencyChange;

                for (int j = 0; j < numSamples; j++) {
                    double time = ((double) j) / ((double)sampleRate); // sample / (sample / second) = second

                    double value = Math.sin(2.0 * Math.PI * frequency * time); // wave
                    short sample = (short) (value * Short.MAX_VALUE); // convert to sample size with 8 bits

                    bData[bDataIndex * 2] = (byte) sample;
                    bData[bDataIndex * 2 + 1] = (byte) (sample >> 8);
                    bDataIndex++;
                }
            }
        }

        line.write(bData, 0, bData.length);
        line.drain();
        line.close();

        writeToFile("output.wav", bData, audioFormat);
    }

    private static void writeToFile(String outputFile, byte[] bData, AudioFormat audioFormat) throws Exception{
        AudioInputStream audioInputStream = new AudioInputStream(
                new ByteArrayInputStream(bData),
                audioFormat,
                bData.length / audioFormat.getFrameSize()
        );

        File file = new File(outputFile);
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);

    }


}
