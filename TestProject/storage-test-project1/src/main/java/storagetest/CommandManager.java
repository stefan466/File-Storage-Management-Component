package storagetest;

import com.google.api.services.drive.model.File;
import org.Storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class CommandManager {

    private static String impl;
    public CommandManager(String impl) {
        this.impl = impl;
    }

    public static void commandCreate(String line, Storage storage){
        String[] arguments = line.split(" ");

        if(arguments.length == 3){
            String fileName = arguments[1];
            String newFilePath = arguments[2];

            storage.createNewFile(fileName, newFilePath);
        } else {
            System.out.println("Pogresno napisana komanda! /help");
        }
    }

    public static void commandCreateMultiple(String line, Storage storage){
        String[] arguments = line.split(" ");

        if(arguments.length == 2){
            String toParse = arguments[1];

            parseMultipleSyntax("", toParse, storage);
        } else if(arguments.length == 3){
            String toParse = arguments[1];
            String path = arguments[2];

            parseMultipleSyntax(path, toParse, storage);

        } else {
            System.out.println("Komanda /createMultiple nije dobro napisana");
        }
    }

    public static void commandMove(String line, Storage storage){
        String[] arguments = line.split(" ");
        List<String> paths = new ArrayList<>();

        for(int i = 1 ; i < arguments.length - 1 ; i++){

            paths.add(arguments[i]);
        }

        String destinationPath = arguments[arguments.length-1];

        storage.moveFiles(paths, destinationPath);
    }

    public static void commandCopy(String line, Storage storage){
        String[] arguments = line.split(" ");
        List<String> paths = new ArrayList<>();

        for(int i = 1 ; i < arguments.length - 1 ; i++){

            paths.add(arguments[i]);
        }

        String destinationPath = arguments[arguments.length - 1];

        storage.copyFiles(paths, destinationPath);

    }

    public static void commandDownload(String line, Storage storage){
        String[] arguments = line.split(" ");
        List<String> paths = new ArrayList<>();

        for(int i = 1 ; i < arguments.length ; i++){

            paths.add(arguments[i]);
        }

        storage.downloadFiles(paths);
    }

    public static void commandDelete(String line, Storage storage){
        String[] arguments = line.split(" ");
        List<String> paths = new ArrayList<>();

        for(int i = 1 ; i < arguments.length ; i++){

            paths.add(arguments[i]);
        }

        storage.deleteFiles(paths);
    }
    public static void commandContains(String line, Storage storage){
        String[] arguments = line.split(" ");
        String path = arguments[1];
        String args2 = "";

        for (int i = 2; i < arguments.length; i++){

            if (i == arguments.length - 1)
                args2 += arguments[i];
            else
                args2 += arguments[i] + " ";
        }

        String[] names = args2.split(",");
        List<String> namesList = Arrays.asList(names);


        boolean contains = storage.containsFile(path, namesList);

        if (contains)
            System.out.println("Zadati direktorijum sadrzi trazeni fajl/fajlove.");
        else
            System.out.println("Zadati direktorijum ne sadrzi trazeni fajl/fajlove.");
    }

    public static void commandFindDir(String line, Storage storage){
        String[] arguments = line.split(" ");
        String name = "";

        for(int i = 1; i < arguments.length; i++){
            if (i == arguments.length - 1){
                name += arguments[i];
            }else
                name += arguments[i] + " ";
        }
        String dirName = storage.returnDir(name);

        if (dirName.equals("error")){
            System.out.println("Neuspesno pronalazenje fajla.");
        }else{
            System.out.println("Trazeni fajl se nalazi u direktorijumu:\n" + dirName);
        }
    }

    public  static void commandRename(String line, Storage storage){
        String[] arguments = line.split(" ");

        storage.renameFile(arguments[1], arguments[2]);
    }
    public static void commandList(String line, Storage storage) throws IOException {

        List<File> listaDrive = new ArrayList<>();
        List<java.io.File> listaLocal = new ArrayList<>();

        String[] arguments = line.split(" ");
        String choice;
        Scanner sc = new Scanner(System.in);

        do{
            System.out.println("Sort by? name/date/lastMod/no");
             choice = sc.nextLine();
        } while (!(choice.equals("name") || choice.equals("date") ||choice.equals("lastMod") || choice.equals("no")));


        String order = "asc";
        if(choice.equals("name") || choice.equals("date") || choice.equals("lastMod")) {
            do{
            System.out.println("Order? asc/desc");
            order = sc.nextLine();
            } while (!(order.equals("asc") || order.equals("desc")));
        }

        String filter;
        System.out.println("Which parimeters of the file do you wish to see? [id,name,...] {name, id, size, datecreated, datemodified}");
        filter = sc.nextLine();
        filter.replace(" ", "");
        String[] parimeters = filter.split(",");
        String ispis;


        if (impl == "local") {

            if (arguments.length == 1) {

                switch (choice){
                    case "no":
                        listaLocal.clear();
                        listaLocal = storage.listAll("nogivenid");
                        break;
                    case "name":
                        listaLocal.clear();
                        listaLocal = storage.sortByName("","", order);
                        break;
                    case "date":
                        listaLocal.clear();
                        listaLocal = storage.sortByDate("", "", order);
                        break;
                    case "lastMod":
                        listaLocal.clear();
                        listaLocal = storage.sortByModification("", "", order);
                        break;
                }


            } else if (arguments.length == 2) {

                listaLocal.clear();
                listaLocal = checkMarkers1("", arguments[1], storage, choice, order);

            } else if (arguments.length == 3) {

                String path = arguments[1];
                listaLocal.clear();
                listaLocal = checkMarkers1(path, arguments[2], storage, choice, order);

            }else if (arguments.length == 4) {

                String path = arguments[1];
                listaLocal.clear();
                listaLocal = checkMarkers1(path, arguments[2] + " " + arguments[3], storage, choice, order);

            }
        /*    List<BasicFileAttributes> listatr = new ArrayList<>();
            BasicFileAttributes attr;
                for (java.io.File f : listaLocal) {
                    attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                    listatr.add(attr);
                }*/


                for (java.io.File f : listaLocal) {
                    ispis = "";
                    for (int i = 0; i < parimeters.length; i++) {

                        if (i != parimeters.length - 1) {
                            switch (parimeters[i]) {
                                case "id":
                                    ispis += "Path: " + f.getPath()+ ",    ";
                                    break;
                                case "name":
                                    ispis += "Name: " + f.getName() + ",    ";
                                    break;
                                case "datecreated":
                                    ispis += "Date Created: " + f + ",   "; //TODO
                                    break;
                                case "datemodified":
                                    ispis += "Date Modified: " + new Date(f.lastModified()) + ",    ";
                                    break;
                                case "size":
                                    ispis += "Size: " + f + ",     "; //TODO
                                    break;
                            }
                        } else {
                            switch (parimeters[i]) {
                                case "id":
                                    ispis += "Path: " + f.getPath();
                                    break;
                                case "name":
                                    ispis += "Name: " + f.getName();
                                    break;
                                case "datecreated":
                                    ispis += "Date Created: " + f; //TODO
                                    break;
                                case "datemodified":
                                    ispis += "Date Modified: " + new Date(f.lastModified());
                                    break;
                                case "size":
                                    ispis += "Size: " + f; //TODO
                                    break;
                            }
                        }


                    }
                    System.out.println(ispis);
                }
        }else if (impl == "drive"){

            if (arguments.length == 1) {

                switch (choice){
                    case "no":
                        listaDrive.clear();
                        listaDrive = storage.listAll("nogivenid");
                        break;
                    case "name":
                        listaDrive.clear();
                        listaDrive = storage.sortByName("x","", order);
                        break;
                    case "date":
                        listaDrive.clear();
                        listaDrive = storage.sortByDate("x", "", order);
                        break;
                    case "lastMod":
                        listaDrive.clear();
                        listaDrive = storage.sortByModification("x", "", order);
                        break;

                }


            } else if (arguments.length == 2) {

                listaDrive.clear();
                listaDrive = checkMarkers1("", arguments[1], storage, choice, order);

            } else if (arguments.length == 3) {

                String path = arguments[1];
                listaDrive.clear();
                listaDrive = checkMarkers1(path, arguments[2], storage, choice, order);

            }else if (arguments.length == 4) {

                String path = arguments[1];
                listaDrive.clear();
                listaDrive = checkMarkers1(path, arguments[2] + " " + arguments[3], storage, choice, order);

            }

            for (File f : listaDrive) {
                ispis = "";
                for (int i = 0; i < parimeters.length; i++) {

                    if (i != parimeters.length - 1) {
                        switch (parimeters[i]) {
                            case "id":
                                ispis += "ID: " + f.getId() + ",    ";
                                break;
                            case "name":
                                ispis += "Name: " + f.getName() + ",    ";
                                break;
                            case "datecreated":
                                ispis += "Date Created: " + f.getCreatedTime().toString() + ",   ";
                                break;
                            case "datemodified":
                                ispis += "Date Modified: " + f.getModifiedTime().toString() + ",    ";
                                break;
                            case "size":
                                ispis += "Size: " + f.getSize() + ",     ";
                                break;
                        }
                    } else {
                        switch (parimeters[i]) {
                            case "id":
                                ispis += "ID: " + f.getId();
                                break;
                            case "name":
                                ispis += "Name: " + f.getName();
                                break;
                            case "datecreated":
                                ispis += "Date Created: " + f.getCreatedTime().toString();
                                break;
                            case "datemodified":
                                ispis += "Date Modified: " + f.getModifiedTime().toString();
                                break;
                            case "size":
                                ispis += "Size: " + f.getSize();
                                break;
                        }
                    }


                }
                System.out.println(ispis);
            }
        }

    }

    public static void commandPeriod(String line, Storage storage){
        String[] arguments = line.split(" ");
        String path = arguments[1];
        String period = arguments[2];

        List<File> lista = storage.listFilesCreatedPeriod(path, period);

    }

    public static void commandHelp(){//1Cbxi2LYwMKPU6p77zZoa2wZ9907FlUhv

        System.out.println("Lista komandi:");
        System.out.println("    /create         [fileName] [destinationPath/ID]         : Kreiranje novog direktorijuma.");
        System.out.println("    /createMultiple [fileName<numFrom..numTo>] [destinationPath/ID]: Kreiranje vise novih direktorijuma.");
        System.out.println("    /move           [filePath/ID]... [destinationPath/ID]   : Premestanje jednog ili vise fajlova/direktorijuma u neki drugi direktorijum.");
        System.out.println("    /copy           [filePath/ID]... [destinationPath/ID]   : Kopiranje jednog ili vise fajlova/direktorijuma u neki drugi direktorijum.");
        System.out.println("    /download       [filePath/ID]...                        : Skidanje jednog ili vise fajlova/direktorijuma sa skladista.");
        System.out.println("    /delete         [filePath/ID]...                        : Brisanje jednog ili vise fajlova/direktorijuma.");
        System.out.println("    /contains       [filePath/ID] [fileNames]               : Proveravanje da li dati direktorijum sadrzi fajl sa zadatim imenom ili fajlove sa imenima iz zadate liste.");
        System.out.println("    /finddir        [fileName]                              : Ispis direktorijuma u kome se nalazi fajl sa zadatim imenom.");
        System.out.println("    /rename         [filePath/ID] [newFileName]             : Promena imena zadatom fajlu."); //TODO
        System.out.println("    /listPeriod     [filePath/ID] [periodFrom - periodTo]   : Izlistavanje fajlova u skladistu koju su kreirani u zadatom periodu."); //
        System.out.println("    /list           [filePath/ID]                           : Izlistavanje svih direktorijuma i fajlova unutar prosledjenog direktorijuma.");
        System.out.println("    /list           [filePath/ID] [marker]                  : Izlistavanje direktorijuma i fajlova unutar prosledjenog direktorijuma u zavisnosti od markera.");
        System.out.println("                                                              Markeri:");
        System.out.println("                                                                    -all               : izlistava sve fajlove iz svih direktorijuma u nekom direktorijumu");
        System.out.println("                                                                    -currdir           : izlistava sve fajlove u zadatom direktorijumu");
        System.out.println("                                                                    -currdir+1         : izlistava fajlove u direktorijumu +1 nivo ispod");
        System.out.println("                                                                    -sub [substring]   : izlistava fajlove u direktorijumu +1 nivo ispod");
        System.out.println("                                                                    -\"fileName\"      : izlistava sve fajlove koji ime isto kao prosledjeno");
        System.out.println("                                                                    -.extension        : izlistava sve fajlove cija je ekstenzija ista kao prosledjena");
        System.out.println("    /exit                                                   : Gasenje programa.");

    }




    public static List checkMarkers1(String path, String marker1, Storage storage, String choice, String order){

        String[] markerSplit = marker1.split(" ");
        String marker = markerSplit[0];
        String substring = null;

        if(markerSplit.length > 1){
           substring = markerSplit[1];
        }

        if(marker.startsWith("-")) {
            if (marker.startsWith("-\"")) {

                if(marker.equals("-\"")){
                    System.out.println("Marker nije potpun. Marker za izlistavanje po nazivu mora biti zapisan u obliku: -\"fileName\"");
                }else {

                    String fileName = marker.substring(2, marker.length() - 1);
                    List <File> lista = storage.listByName(path, fileName);

                    if (lista.isEmpty()){
                        System.out.println("Ne postoje fajlovi sa zadatim imenom.");
                    }else {
                        for(File f: lista){
                            System.out.println("Fajlovi u zadatom direktorijumu sa imenom '" + fileName + "': ");
                            System.out.println(f.getName() + "ID: [" + f.getId() + "]");
                            return lista;
                        }
                    }

                }
            } else if(marker.startsWith("-.")){

                if(marker.equals("-.")){
                    System.out.println("Marker nije potpun. Marker za izlistavanje po ekstenziji mora biti zapisan u obliku: -.extension");
                } else {

                    String extension = marker.substring(1);
                    List<File> lista = storage.listFilesWithExt(path, extension);

                    if (lista.isEmpty()){
                        System.out.println("Ne postoje fajlovi sa zadatom ekstenzijom.");
                    }else {
                        System.out.println("Fajlovi u zadatom direktorijumu sa ekstenijom '" + extension + "': ");

                        return lista;
                    }

                }
            } else if(marker.equals("-all") || marker.equals("-currdir+1") || marker.equals("-currdir") || marker.equals("-sub")) {
                List<File> lista = new ArrayList<>();
                switch (marker) {

                    case "-all":
                        switch (choice){
                            case "no":
                                lista.clear();
                                lista = storage.listAll(path);
                                if (lista.isEmpty()){
                                    System.out.println("Ne postoje fajlovi ni u jednom direktorijumu iz zadatog direktorijuma.");
                                    return new ArrayList();
                                }else {
                                        System.out.println("Fajlovi u svim direktorijumima iz zadatog direktorijuma: ");
                                        return lista;
                                }
                               // break;
                            case "name":
                                lista.clear();
                                lista = storage.sortByName(path, marker1, order);
                                if (lista.isEmpty()){
                                    System.out.println("Ne postoje fajlovi ni u jednom direktorijumu iz zadatog direktorijuma.");
                                }else {
                                    System.out.println("Fajlovi u svim direktorijumima iz zadatog direktorijuma sortirani po imenu: ");
                                    return lista;
                                }
                                break;
                            case "date":
                                lista.clear();
                                lista = storage.sortByDate(path, marker1, order);
                                if (lista.isEmpty()){
                                    System.out.println("Ne postoje fajlovi ni u jednom direktorijumu iz zadatog direktorijuma.");
                                }else {
                                        System.out.println("Fajlovi u svim direktorijumima iz zadatog direktorijuma sortirani po datumu: ");
                                        return lista;
                                }
                                break;
                            case "lastMod":
                                lista.clear();
                                lista = storage.sortByModification(path, marker1, order);
                                if (lista.isEmpty()){
                                    System.out.println("Ne postoje fajlovi u zadatom direktorijumu.");
                                }else {
                                        System.out.println("Fajlovi u svim direktorijumima iz zadatog direktorijuma\nsortirani po vremenu kada su zadnji put modifikovani: ");
                                        return lista;
                                }
                                break;
                        }
                        break;
                    case "-currdir":
                        switch (choice){
                            case "no":
                                lista.clear();
                                lista = storage.listFiles(path);
                                if (lista.isEmpty()){
                                    System.out.println("Ne postoje fajlovi u zadatom direktorijumu.");
                                    return new ArrayList();
                                }else {
                                    System.out.println("Fajlovi u zadatom direktorijumu: ");

                                    return lista;
                                }
                                //break;

                            case "name":
                                lista.clear();
                                lista = storage.sortByName(path, marker1, order);
                                if (lista.isEmpty()){
                                    System.out.println("Ne postoje fajlovi u zadatom direktorijumu.");
                                }else {

                                    System.out.println("Fajlovi u zadatom direktorijumu sortirani po imenu: ");
                                    return lista;
                                }
                                break;

                            case "date":
                                lista.clear();
                                lista = storage.sortByDate(path, marker1, order);
                                if (lista.isEmpty()){
                                    System.out.println("Ne postoje fajlovi u zadatom direktorijumu.");
                                }else {
                                    System.out.println("Fajlovi u zadatom direktorijumu sortirani po datumu: ");
                                    return lista;
                                }
                                break;

                            case "lastMod":
                                lista.clear();
                                lista = storage.sortByModification(path, marker1, order);
                                if (lista.isEmpty()){
                                    System.out.println("Ne postoje fajlovi u zadatom direktorijumu.");
                                }else {
                                    System.out.println("Fajlovi u zadatom direktorijumu sortirani po vremenu kada su zadnji put modifikovani: ");
                                    return lista;
                                }
                                break;
                        }
                        break;
                    case "-currdir+1":
                        switch (choice) {
                            case "no":

                                lista.clear();
                                lista = storage.listDirs(path);
                                if (lista.isEmpty()) {
                                    System.out.println("Ne postoje fajlovi u zadatom direktorijumu niti u poddirektorijumima.");
                                } else {
                                        System.out.println("Fajlovi u zadatom direktorijumu i svim poddirektorijumima: ");
                                        return lista;
                                }
                                break;
                            case "name":
                                lista.clear();
                                lista = storage.sortByName(path, marker1, order);
                                if (lista.isEmpty()) {
                                    System.out.println("Ne postoje fajlovi u zadatom direktorijumu niti u poddirektorijumima.");
                                } else {

                                    System.out.println("Fajlovi u zadatom direktorijumu i svim poddirektorijumima sortirani po imenu: ");
                                    return lista;
                                }
                                break;
                            case "date":
                                lista.clear();
                                lista = storage.sortByDate(path, marker1, order);
                                if (lista.isEmpty()) {
                                    System.out.println("Ne postoje fajlovi u zadatom direktorijumu niti u poddirektorijumima.");
                                } else {
                                    System.out.println("Fajlovi u zadatom direktorijumu i svim poddirektorijumima sortirani po datumu: ");
                                    return lista;

                                }
                                break;
                            case "lastMod":
                                lista.clear();
                                lista = storage.sortByModification(path, marker1, order);
                                if (lista.isEmpty()) {
                                    System.out.println("Ne postoje fajlovi u zadatom direktorijumu.");
                                } else {

                                    System.out.println("Fajlovi u zadatom direktorijumu i svim poddirektorijumima sortirani po vremenu kada su zadnji put modifikovani: ");
                                    return lista;
                                }
                        }
                        break;
                    case "-sub":
                        switch (choice){
                            case "no":

                                lista.clear();
                                lista = storage.listSubstringFiles(path, substring);
                                if (lista.isEmpty()) {
                                    System.out.println("Ne postoje fajlovi u zadatom direktorijumu niti u poddirektorijumima.");
                                } else {
                                    return lista;
                                }
                                break;
                            case "name":
                                lista.clear();
                                lista = storage.sortByName(path, marker1, order);
                                if (lista.isEmpty()) {
                                    System.out.println("Ne postoje fajlovi u zadatom direktorijumu niti u poddirektorijumima.");
                                } else {

                                    System.out.println("Fajlovi u zadatom direktorijumu i svim poddirektorijumima sortirani po imenu: ");
                                    return lista;
                                }
                                break;
                            case "date":
                                lista.clear();
                                lista = storage.sortByDate(path, marker1, order);
                                if (lista.isEmpty()) {
                                    System.out.println("Ne postoje fajlovi u zadatom direktorijumu niti u poddirektorijumima.");
                                } else {
                                    System.out.println("Fajlovi u zadatom direktorijumu i svim poddirektorijumima sortirani po datumu: ");
                                    return lista;

                                }
                                break;
                            case "lastMod":
                                lista.clear();
                                lista = storage.sortByModification(path, marker1, order);
                                if (lista.isEmpty()) {
                                    System.out.println("Ne postoje fajlovi u zadatom direktorijumu.");
                                } else {

                                    System.out.println("Fajlovi u zadatom direktorijumu i svim poddirektorijumima sortirani po vremenu kada su zadnji put modifikovani: ");
                                    return lista;
                                }
                        }
                        break;

                }

            } else {
                System.out.println("Marker nije validan! Lista validnih markera: -all | -currdir | -currdir+1 | -\"fileName\" | -.extension");
                return null;
            }
        } else {
            System.out.println("Pogresan poziv /list komande. Za detaljnije objasnjenje komande ukucajte /help");
            return null;
        }
        return null;
    }

    public static void parseMultipleSyntax(String path, String toParse, Storage storage) {

        if (toParse.contains("<") && toParse.contains(">")) {
            //s<1..20>.json
            String[] s1 = toParse.split("<");

            String fileName = s1[0]; //s
            String countAndExtension = s1[1]; //1..20>.json

            if (countAndExtension.endsWith(">")) {
                String[] s3 = countAndExtension.split(">");
                String count = s3[0];

                String[] numbers = count.split("\\.\\.");

                int from = Integer.parseInt(numbers[0]); //1
                int to = Integer.parseInt(numbers[1]); //20

                storage.createFromTo(from, to, fileName, path, "");
            } else {
                String[] s2 = countAndExtension.split(">");
                String count = s2[0]; //1..20
                String extension = s2[1]; //.json


                String[] numbers = count.split("\\.\\.");

                int from = Integer.parseInt(numbers[0]); //1
                int to = Integer.parseInt(numbers[1]); //20

                storage.createFromTo(from, to, fileName, path, extension);
            }
        } else {
            System.out.println("Pogresno napisana komanda! /help");
        }
    }
}
