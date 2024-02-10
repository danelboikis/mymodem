package com.mymodem;

import static com.mymodem.IntToBytes.intToBytes;
import static com.mymodem.StringToBytes.stringToBytes;


public class App 
{
    public static void main( String[] args ) throws Exception
    {
        System.out.println("countdown:");
        for (int i = 3; i >= 1 ; i--) {
            Thread.sleep(1000);
            System.out.println(i);
        }

        Modulator.modulate16bit(messageToBytes(ConfigFile.getConfigFile().getValue("message")));

        System.out.println("finished");

        /*String handshakeMessage = "01010101010101010101010101010101";
        byte pad = 0b00000000;
        byte b1 = 0b01010101;
        byte len = 0b00001000;
        byte message = 0b01011101;
        byte b5 = 0b00000000;
        System.out.println("start");
        byte[] bs = new byte[]{pad,b1,b1,b1,b1,len,message,pad,pad,pad,pad};
        Modulator.modulate(bs);*/
        /*byte b = 0b01010101;
        Modulator.modulate(new byte[]{b});*/
        /*byte b = 0b00000000;
        Modulator.modulate(new byte[]{b, b, b, b});*/
        /*Demodulator.demodulate();
        System.out.println("finished");*/

        /*byte b = 0b01111111;
        Modulator.modulate16bit(new byte[]{b, b});*/

        /*String message = "0101010101010101010101010101010101000000";
        byte b0 = 0b00000000;
        byte b1 = 0b01010101;
        byte b2 = b1;
        byte b3 = b1;
        byte b4 = b1;
        byte b5 = 0b01000000;
        System.out.println("start");
        byte[] bs = new byte[]{b0,b1,b2,b3,b4,b5};
        Modulator.modulate16bit(bs);*/
    }

    public static byte[] messageToBytes(String message) {
        byte b1 = 0b01010101;
        byte[] handshakeBytes = new byte[]{b1,b1,b1,b1};
        byte[] lenBytes = intToBytes(message.length());
        byte[] messageBytes = stringToBytes(message);

        byte[] bytes = new byte[handshakeBytes.length + lenBytes.length + messageBytes.length + 4];

        int index = 0;

        for (byte b : handshakeBytes) {
            bytes[index] = b;
            index++;
        }

        for (byte b : lenBytes) {
            bytes[index] = b;
            index++;
        }

        for (byte b : messageBytes) {
            bytes[index] = b;
            index++;
        }

        bytes = padBytes(bytes);

        return bytes;
    }

    public static byte[] padBytes(byte[] bytes) {
        byte pad = 0b00000000;
        byte[] paddedBytes = new byte[bytes.length + 4];
        int index = 0;
        paddedBytes[index] = pad;
        index++;
        paddedBytes[index] = pad;
        index++;

        for (byte b : bytes) {
            paddedBytes[index] = b;
            index++;
        }

        paddedBytes[index] = pad;
        index++;
        paddedBytes[index] = pad;
        index++;

        return paddedBytes;
    }
}
