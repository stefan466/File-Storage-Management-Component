package local_storage_impl;

import org.Storage;
import org.StorageManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config extends StorageManager {
    public static void main(String[] args) {


//        StorageManager storageManager = new StorageManager();
//
//        String test = storageManager.getResult();
//        System.out.println(test);



//        try {
//            String configFilePath = "";
//            FileInputStream propsInput = new FileInputStream(configFilePath);
//            Properties prop = new Properties();
//            prop.load(propsInput);
//
//            System.out.println(prop.getProperty("STORAGE_SIZE"));
//            String storage_ext = prop.getProperty("STORAGE_EXT");
//            System.out.println(storage_ext);
//            String[] ext_list = storage_ext.split(",");
//            for(String s : ext_list) {
//                System.out.println("ext: " + s);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
