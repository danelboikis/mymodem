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
    public static byte[] demodulate() throws Exception{
        double carrierFrequency = Double.valueOf(ConfigFile.getConfigFile().getValue("carrierFrequency"));
        double frequencyChange = Double.valueOf(ConfigFile.getConfigFile().getValue("frequencyChange"));

        int dataRate = Integer.valueOf(ConfigFile.getConfigFile().getValue("dataRate")); // time for one bit of data in ms
        int sampleRate = Integer.valueOf(ConfigFile.getConfigFile().getValue("sampleRate")); // number of samples taken per second

        int recTime = 5; // recording time in seconds
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

        while (dataIndex < 1) {
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

        int paddedValuesLen = Integer.highestOneBit(values.length) << 1; // rounds the length to be a power of 2
        double[] paddedValues = new double[paddedValuesLen];
        System.arraycopy(values, 0, paddedValues, 0, values.length); // copy array

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

        System.out.println("detected frequency: " + detectedFrequency);
        System.out.println("magnitude difference: " + Math.abs(mag1 - mag2));

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
        }*/
        targetDataLine.close();
        return null;
    }
}
