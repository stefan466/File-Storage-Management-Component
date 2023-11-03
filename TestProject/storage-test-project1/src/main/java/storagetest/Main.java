package storagetest;

import org.Storage;
import org.StorageManager;
import org.googledrive.impl.GoogleDriveStorage;

import java.io.File;
import java.io.IOException;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws IOException {

       // String impl = "drive";
        String impl = "local";

        try {
            //Class.forName("org.googledrive.impl.GoogleDriveStorage"); // 11QdYzWAppCpEconzpFs6vQ5zCI4GDo64
            Class.forName("local_storage_impl.LocalStorageImplementation"); //Storage C:\Users\matij\Documents

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(args.length!=2){
            System.out.println("java -jar storage-test-1.0-SNAPSHOT <storageName> <storagePath>   ");
            System.out.println(args);
            // Storage 11QdYzWAppCpEconzpFs6vQ5zCI4GDo64

            System.exit(0);
        }

        String rootName = args[0];
        String rootPath = args[1];

        Storage storage =  StorageManager.getStorage(rootPath.concat(System.getProperty("file.separator")).concat(rootName));

        boolean valid = storage.initStorage(rootName, rootPath);
        if(!valid){
            System.out.println("Putanja koju ste prosledili nije validna za kreiranje novog skladista, niti na njoj postoji validno skladiste.");
            System.out.println("Pokusajte ponovo.");
            System.exit(0);
        }

        Scanner sc = new Scanner(System.in);
        CommandManager commandManager = new CommandManager(impl);

        while(true) {
            String line = sc.nextLine();

            String command = line.split(" ")[0];

            switch(command){

                case "/create" : commandManager.commandCreate(line, storage); break;
                case "/createMultiple" : commandManager.commandCreateMultiple(line, storage); break;
                case "/move" : commandManager.commandMove(line, storage); break;
                case "/copy" : commandManager.commandCopy(line, storage); break;
                case "/download" : commandManager.commandDownload(line, storage); break;
                case "/delete" : commandManager.commandDelete(line, storage); break;
                case "/list" : commandManager.commandList(line,storage); break;
                case "/contains" : commandManager.commandContains(line, storage); break;
                case "/finddir" : commandManager.commandFindDir(line, storage); break;
                case "/rename" : commandManager.commandRename(line, storage); break;
                case "/period" : commandManager.commandPeriod(line, storage); break;
                case "/help" : commandManager.commandHelp(); break;
                case "/exit" : System.exit(0);

                default :
                    System.out.println("Komanda nije validna, kucajte /help za pomoc.");
                    break;
            }
        }

    }
}

