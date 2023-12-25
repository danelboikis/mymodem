package com.mymodem;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;


public class Demodulator {
    private static double carrierFrequency = Double.valueOf(ConfigFile.getConfigFile().getValue("carrierFrequency"));
    private static double frequencyChange = Double.valueOf(ConfigFile.getConfigFile().getValue("frequencyChange"));

    private static int dataRate = Integer.valueOf(ConfigFile.getConfigFile().getValue("dataRate")); // time for one bit of data in ms
    private static int sampleRate = Integer.valueOf(ConfigFile.getConfigFile().getValue("sampleRate")); // number of samples taken per second

    private static int recTime = 3; // recording time in seconds

    private static double[] magnitudes = null;

    public static byte[] demodulate() throws Exception{
        byte[] data = new byte[recTime * sampleRate];
        int dataIndex = 0;
        int offset = 0;
        
        AudioFormat audioFormat = new AudioFormat(sampleRate,
                                                8,
                                                1,
                                                Boolean.valueOf(ConfigFile.getConfigFile().getValue("signed")), 
                                                Boolean.valueOf(ConfigFile.getConfigFile().getValue("bigEndian")));

        TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getTargetDataLine(audioFormat);
        
        //targetDataLine.flush();
        targetDataLine.open(audioFormat);
        targetDataLine.start();

        while (dataIndex < data.length) {
            offset = dataIndex;
            dataIndex += targetDataLine.read(data, offset, data.length);
        }

        targetDataLine.close();

        System.out.println("buffer after: " + Arrays.toString(data));
        
        System.out.println("data2");

        double values[] = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            double d = b;
            d = d / Byte.MAX_VALUE;
            values[i] = d;
        }
    
        int numSamples = (int) Math.ceil((((double) dataRate) / 1000.0) * sampleRate); // number of samples for one bit

        System.out.println("starting to convert to digital data......");

        char[] digitalValues = new char[values.length - numSamples + 1];

        for (int i = 0; i < digitalValues.length; i++) {
            digitalValues[i] = getDigitalBit(values, i, numSamples);
        }

        System.out.println("looking for message.............");
        
        String message = "0101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101";
        boolean found = false;
        int digValIn = 0;

        while (!found && (digValIn + numSamples * (message.length() - 1)) < digitalValues.length) {
            found = true;
            for (int i = 0; i < message.length() && found; i++) {
                if (digitalValues[digValIn + i * numSamples] != message.charAt(i)) {
                    found = false;
                }
            }
            
            digValIn++;
        }

        if (found) {
            System.out.println("message found at: " + (digValIn - 1));
        }
        else {
            System.out.println("message not found");
        }

        return null;
    }

    /*public static byte[] demodulate() throws Exception{
        byte[] data = new byte[recTime * sampleRate];
        int dataIndex = 0;
        int offset = 0;
        
        AudioFormat audioFormat = new AudioFormat(sampleRate,
                                                8,
                                                1,
                                                Boolean.valueOf(ConfigFile.getConfigFile().getValue("signed")), 
                                                Boolean.valueOf(ConfigFile.getConfigFile().getValue("bigEndian")));

        TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getTargetDataLine(audioFormat);
        
        //targetDataLine.flush();
        targetDataLine.open(audioFormat);
        targetDataLine.start();

        while (dataIndex < data.length) {
            offset = dataIndex;
            dataIndex += targetDataLine.read(data, offset, data.length);
        }

        System.out.println("buffer after: " + Arrays.toString(data));
        
        System.out.println("data2");

        double values[] = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            //System.out.println(b);
            double d = b;
            d = d / Byte.MAX_VALUE;
            //System.out.println(d);
            values[i] = d;
        }
        
        detectFrequency(values, 0, values.length);

        /*System.out.println("writing data....");

        try {
            // Open a FileWriter with the specified file path
            FileWriter fileWriter = new FileWriter("testFile.txt");

            // Wrap the FileWriter in a BufferedWriter for efficient writing
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Write some content to the file
            bufferedWriter.write(Arrays.toString(data2));
            bufferedWriter.newLine(); // Add a newline character

            // You can continue writing more content as needed

            // Close the BufferedWriter to flush and close the file
            bufferedWriter.close();

            System.out.println("Data has been written to the file.");

        } catch (IOException e) {
            // Handle exceptions, such as file not found or permission issues
            e.printStackTrace();
        }
        targetDataLine.close();
        return null;
    }*/

    private static char getDigitalBit(double[] value, int offset, int len) throws Exception {
        double detectedFrequency = detectFrequency(value, offset, len);

        if (detectedFrequency == carrierFrequency) {
            return '0';
        }
        else if (detectedFrequency == (carrierFrequency + frequencyChange)) {
            return '1';
        }

        throw new Exception("Something went wrong with the frequencies config");
    }

    private static double detectFrequency(double[] values, int offset, int len) {
        if (offset == 0) {
            System.out.println(values.length);
        }
        System.out.println(offset);
        int paddedValuesLen = Integer.highestOneBit(len) << 1; // rounds the length to be a power of 2
        double[] paddedValues = new double[paddedValuesLen];
        System.arraycopy(values, offset, paddedValues, 0, len); // copy array

        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] transformedComplex = transformer.transform(paddedValues, TransformType.FORWARD);

        // magnitude[i] will hold the magnitude of the frequency = (i * sampleRate / paddedValuesLen)
        double[] magnitudes = new double[paddedValues.length / 2]; // only positive frequencies
        for (int i = 0; i < magnitudes.length; i++) {
            magnitudes[i] = transformedComplex[i].abs();
        }

        int carrierFrequencyIndex = (int) Math.round(carrierFrequency * paddedValuesLen / sampleRate);
        int changedFrequencyIndex = (int) Math.round((carrierFrequency + frequencyChange) * paddedValuesLen / sampleRate);

        double mag1 = magnitudes[carrierFrequencyIndex];
        double mag2 = magnitudes[changedFrequencyIndex];

        double detectedFrequency = (mag1 > mag2) ? carrierFrequency : (carrierFrequency + frequencyChange);

        //System.out.println("detected frequency: " + detectedFrequency);
        //System.out.println("magnitude difference: " + Math.abs(mag1 - mag2));

        return detectedFrequency;
    }
}
