package org;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class StorageManager {

    private static Storage storage;
    private String result;


    public static void registerStorage(Storage str){
        storage = str;
    }


    public StorageManager() {
   }




    public String configFile() {
        try {
            String configFilePath = "config.properties";
            FileInputStream propsInput = new FileInputStream(configFilePath);
            Properties prop = new Properties();
            prop.load(propsInput);

            System.out.println(prop.getProperty("STORAGE_SIZE"));
            String storage_ext = prop.getProperty("STORAGE_EXT");

            System.out.println(storage_ext);
            String[] ext_list = storage_ext.split(",");
            for(String s : ext_list) {
                System.out.println("ext: " + s);
            }
            return storage_ext + ext_list;
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Storage getStorage(String path){

        storage.setPath(path);
        return storage;
    }

    public String getResult() {
        return result;
    }
}
