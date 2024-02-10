package com.mymodem;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConfigFile {
    private static ConfigFile configFile = null;

    private ConfigFile() {
    }

    public String getValue(String key) {
        String value = null;
        try {
            //FileReader reader = new FileReader(txtConfigFile);
            FileReader reader = new FileReader("config.properties");
            Properties props = new Properties();
            
            props.load(reader);

            value = props.getProperty(key);

            reader.close();
        }
        catch (FileNotFoundException ex) {

        }
        catch (IOException ex) {

        }

        return value;

    }

    public static ConfigFile getConfigFile() {
        if (configFile == null) {
            configFile = new ConfigFile();
        }

        return configFile;
    }
}
