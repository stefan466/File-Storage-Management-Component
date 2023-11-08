package org.googledrive.impl;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.Storage;
import org.StorageManager;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

import java.text.SimpleDateFormat;

import java.io.*;
import java.util.*;

import java.io.IOException;
import java.util.Arrays;


public class GoogleDriveStorage extends Storage{


    private static Drive service;

    private static String storageId;
    private static final String APPLICATION_NAME = "My project";

    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static HttpTransport HTTP_TRANSPORT;

    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);

    static {
        try {
            StorageManager.registerStorage(new GoogleDriveStorage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public GoogleDriveStorage() throws IOException {
    }

    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in = GoogleDriveStorage.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES).setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    @Override
    public boolean initStorage(String name, String path) {
        try {

            service = getDriveService();

        List<File> inLocation = new ArrayList<File>();
        String pageToken = null;
        do {
            FileList result = null;

                result = service.files().list()
                        .setQ("'" + path + "' in parents")
                        .setFields("nextPageToken, files(*)")
                        .setPageToken(pageToken)
                        .execute();

            inLocation.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        //ako vec postoji Storage
        for (File f: inLocation){
            if (f.getName().equals(name) &&
                    f.getMimeType().equals("application/vnd.google-apps.folder")){
                storageId = f.getId();
                System.out.println("Storage ID: " + storageId);
                
                return true;
            }
        }

        //ako ne postoji Storage
        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setParents(Collections.singletonList(path));
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        File file = null;
            file = service.files().create(fileMetadata)
                    .setFields("id, parents")
                    .execute();

        storageId = file.getId(); //!!!
        System.out.println("Storage ID: " + storageId);
            return true;



        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    
    //POMOCNE
    //vraca sve direktorijume u storage
    public List<File> listAllStorageFiles(){
        List<File> konacna = null;

        konacna = new ArrayList<>();
        try {

            File folder = service.files().get(storageId).execute();
            addToFinal1(folder, konacna);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return konacna;
    }
    //isto kao addtofinal samo za direktorijume
    public static void addToFinal1(File f, List konacna) throws IOException {

        FileList result = service.files()
                .list()
                .setQ("'" + f.getId() + "' in parents")
                .setFields("nextPageToken, files(*)")
                .execute();
        List<File> deca = result.getFiles();
        for (File f1: deca){
            konacna.add(f1);
            if (f1.getMimeType().equals("application/vnd.google-apps.folder")){
                addToFinal1(f1, konacna);
            }
        }

    }

    public boolean existsInStorage(String id){
        List<File> direktorijumi = listAllStorageFiles();
        List<String> idDirektorijuma = new ArrayList<>();

        for(File f: direktorijumi){
            idDirektorijuma.add(f.getId());
        }

        idDirektorijuma.add(storageId);

        if(idDirektorijuma.contains(id))
            return true;
        else
            return false;
    }
    
    
    

    //kreiranje foldera!
        @Override
    public  void createNewFile(String name, String destinationId) {
            
        if (existsInStorage(destinationId)) {
            try {

                File fileMetadata = new File();
                fileMetadata.setName(name);
                fileMetadata.setParents(Collections.singletonList(destinationId));
                fileMetadata.setMimeType("application/vnd.google-apps.folder");

                service.files().create(fileMetadata)
                        .setFields("id, parents")
                        .execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{
            System.out.println("Storage does not contain given ID(s).");
        }
        }

    @Override
    public void moveFile(String sourceId, String destinationId) {
        
        if (existsInStorage(destinationId) && existsInStorage(sourceId)) {
            
            copyFile(sourceId, destinationId);
            deleteFile(sourceId);
            
        }else{
            System.out.println("Storage does not contain given ID(s).");
        }
           

    }

    @Override
    public void copyFile(String sourceId, String destinationId) {
        
        if (existsInStorage(destinationId)) {

            File original = null;
            try {

                original = service.files().get(sourceId).execute();
                if (original == null) {
                    System.out.println("No files with that ID found.");
                    return;
                }


                if(!original.getMimeType().equals("application/vnd.google-apps.folder")) {
                    File fileMetadata = new File();
                    fileMetadata.setName(original.getName());
                    fileMetadata.setParents(Collections.singletonList(destinationId));
                    service.files()
                            .copy(original.getId(), fileMetadata).setFields("*")
                            .execute();
                }else{
                    System.out.println("Folders cannot be copied in  Google Drive API. [https://developers.google.com/drive/api/v3/reference/files/copy]");
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
      
        }else{
            System.out.println("Storage does not contain given folder ID.");
        }

    }

    // •	preuzimanje fajlova iz skladišta - zadaje se putanja koja može biti putanja
    // do direktorijuma ili do fajla i odgovarajući element se preuzima iz skladišta u
    // neki lokalni folder na zadatoj putanji (ako se radi o lokalnom skladištu, ta putanja
    // mora biti izvan skaldišta),
    @Override
    public void downloadFile(String sourceId) {
        if (existsInStorage(sourceId)) {
            File file;
            String fileName;

            try {

                file = service.files().get(sourceId).execute();
                fileName = file.getName();

                java.io.File f = new java.io.File("C:\\Users\\matij\\Documents\\"
                        + fileName);
                if (!f.exists()){
                    f.createNewFile();
                }
                OutputStream outputStream = new FileOutputStream(f);

                String realFileId = file.getId();
                service.files().get(realFileId)
                        .executeMediaAndDownloadTo(outputStream);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{
            System.out.println("Storage does not contain given ID(s).");
        }

       
    }
    //•	brisanje fajlova i direktorijuma iz skladišta,
        @Override
    public void deleteFile(String sourceId) {
            if (existsInStorage(sourceId)) {
                try {

                    String realFileId = sourceId;
                    service.files().delete(realFileId).execute();

                } catch (GoogleJsonResponseException e) {
                    System.err.println("Unable to delete file: " + e.getDetails());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }else{
                System.out.println("Storage does not contain given ID(s).");
            }

     
    }

    @Override
    public boolean uploadFile(String s) {
        return false;
    }

    //◦ vrati fajlove koji su kreirani/modifikovani u nekom periodu, u nekom direktorijumu, *
    @Override
    public List listFilesCreatedPeriod(String sourceId, String time) {

        List<File> files = new ArrayList<>();
        DateTime from, to;

        String[] datumi = time.split("-");


        try {
        if(existsInStorage(sourceId)){
            File source = null;

                source = service.files().get(sourceId).execute();

            System.out.println(source.getModifiedTime());
        }

    } catch (IOException e) {
        throw new RuntimeException(e);
    }

        return null;
    }


    //◦ vrati sve fajlove iz svih direktorijuma u nekom direktorijumu,
    @Override
    public List listAll(String sourceId) {
        List<File> konacna = new ArrayList<>();
        String id;
        if (sourceId.equals("nogivenid")) {
            id = storageId;
        } else {
            id = sourceId;
        }

        if (existsInStorage(id)) {

            konacna = new ArrayList<>();

            try {


                File folder = service.files().get(id).execute();

                if (folder == null) {
                    System.out.println("No files found with the given ID.");
                }

                if (folder.getMimeType().equals("application/vnd.google-apps.folder"))
                    addToFinal(folder, konacna);
                else
                    System.out.println("Given ID does not belong to a folder.");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }else{
            System.out.println("Storage does not contain given ID(s).");

        }
        return konacna;
    }
    //REKURZIVNA IDE UZ @ListAll
    public static void addToFinal(File f, List konacna) throws IOException {

        FileList result = service.files()
                .list()
                .setQ("'" + f.getId() + "' in parents")
                .setFields("nextPageToken, files(*)")
                .execute();
        List<File> deca = result.getFiles();
        for (File f1: deca){
            if (!f1.getMimeType().equals("application/vnd.google-apps.folder")){
                konacna.add(f1);
            }else{
                addToFinal(f1, konacna);
            }
        }

    }

    //vratiti sve fajlove u zadatom direktorijumu (vraća se naziv i metapodaci),
    @Override
    public List listFiles(String sourceId) {

        List<File> files = null;
        if (existsInStorage(sourceId)) {

            File dir;
            FileList result1 = null;
            try {

                dir = service.files().get(sourceId).execute();
                if (dir == null) {
                    System.out.println("No folder found with the given name.");
                }

                FileList result = service.files()
                        .list()
                        .setQ("mimeType != 'application/vnd.google-apps.folder' " +
                                "and '" + dir.getId() + "' in parents")
                        .setFields("nextPageToken, files(*)")
                        .execute();
                files = result.getFiles();

                if (files == null || files.isEmpty()) {
                    System.out.println("No files found at given location.");
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else {
            System.out.println("Storage does not contain given ID(s).");
        }

        return files;
    }
    
    //◦ vrati sve fajlove u zadatom direktorijumu i svim poddirektorijumima
    @Override
    public List listDirs(String sourceId) {
        List<File> konacna = null;
        if (existsInStorage(sourceId)) {
            konacna = new ArrayList<>();

            try {

                //izlistaj decu foldera
                FileList result1 = service.files()
                        .list()
                        .setQ("'" + sourceId + "' in parents")
                        .setFields("nextPageToken, files(*)")
                        .execute();
                List<File> deca = result1.getFiles();

                //upisi sve fajlove
                for (File file : deca) {
                    if (!file.getMimeType().equals("application/vnd.google-apps.folder")) {
                        konacna.add(file);
                    } else {
                        //udji jos 1 u dubinu
                        FileList result2 = service.files()
                                .list()
                                .setQ("'" + file.getId() + "' in parents")
                                .setFields("nextPageToken, files(*)")
                                .execute();
                        List<File> files2 = result2.getFiles();
                        for (File file1 : files2) {
                            if (!file1.getMimeType().equals("application/vnd.google-apps.folder"))
                                konacna.add(file1);
                        }

                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else {
            System.out.println("Storage does not contain given ID(s).");
        }

        return konacna;
    }


    //vrati sve fajlove u zadatom direktorijumu sa zadatim imenom
    @Override
    public List listByName(String sourceId, String name) {
        List<File> konacna = new ArrayList<>();
        if (existsInStorage(sourceId)) {
            List<File> lista = listAll(sourceId);
            for (File f : lista) {
                if (f.getName().equals(name))
                    konacna.add(f);
            }
        } else {
            System.out.println("Storage does not contain given ID(s).");
        }

        return konacna;
    }

    //•	preimenovanje fajlova i foldera u skladištu,
    @Override
    public void renameFile(String sourceId, String newName) {

        File file;
        if(existsInStorage(sourceId)) {
            try {

                file = service.files().get(sourceId).execute();
                if (file == null) {
                    System.out.println("No files found with that ID.");
                }

                file.setName(newName);
                service.files().update(sourceId, file).execute();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else {
            System.out.println("Storage does not contain given ID(s).");
        }



    }
    //vrati fajlove sa određenom ekstenzijom,
        @Override
    public List listFilesWithExt(String sourceId, String extenstion) {
            List<File> files = new ArrayList<>();
            if (existsInStorage(sourceId)) {
                try {

                    
                    File folder = service.files().get(sourceId).execute();

                    FileList result = service.files()
                            .list()
                            .setQ("fileExtension = '" + extenstion + "' and '"
                                    + folder.getId() + "' in parents")
                            .setFields("nextPageToken, files(*)")
                            .execute();
                    files = result.getFiles();
                    if (files == null || files.isEmpty()) {
                        System.out.println("No files with that extension found. ");
                    }

                    
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }else
                System.out.println("Storage does not contain given ID(s).");


            return files;
        }

    //vrati fajlove koji u svom imenu sadrže, počinju, ili se završavaju nekim
    //zadatim podstringom
    @Override
    public List listSubstringFiles(String sourceId, String substring) {
        List<File> files = null;
        if (substring == null){
            System.out.println("No substring read.");
            return new ArrayList();
        }
        if (existsInStorage(sourceId)) {
            try {

                File folder = service.files().get(sourceId).execute();

                FileList result = service.files()
                        .list()
                        .setQ("name contains '" + substring + "' and '"
                                + folder.getId() + "' in parents")
                        .setFields("nextPageToken, files(*)")
                        .execute();
                files = result.getFiles();
                if (files == null || files.isEmpty()) {
                    System.out.println("No files with name containing given substring found.");
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else
            System.out.println("Storage does not contain given ID(s).");

        return files;
    }

    //vratiti da li određeni direktorijum sadrži fajl sa određenim imenom,
    // ili više fajlova sa zadatom listom imena
    @Override
    public boolean containsFile(String sourceId, List<String> fileNames) {
        if (existsInStorage(sourceId)) {
            try {
                FileList result = service.files()
                        .list()
                        .setQ("'" + sourceId + "' in parents and" +
                                "mimeType != 'application/vnd.google-apps.folder'")
                        .setFields("nextPageToken, files(*)")
                        .execute();
                List<File> files = result.getFiles();
                List<String> childrenNames = new ArrayList<>();

                for(File f: files){
                    childrenNames.add(f.getName());
                }

                if(childrenNames.containsAll(fileNames))
                    return true;
                else return false;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else
            System.out.println("Storage does not contain given ID(s).");

        return false;
    }


    //vratiti u kom folderu se nalazi fajl sa određenim zadatim imenom
    @Override
    public String returnDir(String name) {
        FileList result;
        File file;

        //pretraga fajla po imenu
        File parent = null;
        try {
            result = service.files()
                    .list()
                    .setQ("name = '" + name + "'")
                    .setFields("nextPageToken, files(*)")
                    .execute();
            List<File> files = result.getFiles();
            file = files.get(0);
            if(existsInStorage(file.getId())) {

                //nalazenje fileId parenta
                List<String> parents = file.getParents();

                String parentID = parents.get(0);

                //pretraga roditelja po fileID
                parent = service.files().get(parentID).execute();
            }else {
                System.out.println("Storage does not contain files with given name.");

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (parent != null)
            return parent.getName() + ", ID: [" + parent.getId() + "]";
        else
            return "error";
    }

    //obezbediti zadavanje različitih kriterijuma sortiranja,
    // na primer po nazivu, datumu kreiranje ili modifikacije, rastuće ili opadajuće,
    @Override
    public List sortByName(String sourceId, String marker1, String order) {

        String[] markerSplit = marker1.split(" ");
        String marker = markerSplit[0];
        String substring = null;

        if(markerSplit.length > 1){
            substring = markerSplit[1];
        }
        String id;
        if (sourceId.equals("x"))
            id = storageId;
        else
            id = sourceId;

        List <File> lista = new ArrayList<>();
        if (existsInStorage(sourceId)){
            switch (marker){
                case "-all":

                    if (order.equals("asc")){
                        lista.clear();
                        lista = listAll(id);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o2.getName().compareTo(o1.getName());
                            }
                        });
                    }

                    break;
                case "-currdir":
                    if (order.equals("asc")){
                        lista.clear();
                        lista = listFiles(id);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o2.getName().compareTo(o1.getName());
                            }
                        });
                    }
                    break;
                case "-currdir+1":
                    if (order.equals("asc")){
                        lista.clear();
                        lista = listDirs(id);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o2.getName().compareTo(o1.getName());
                            }
                        });
                    }
                    break;
                case "-sub":
                    if (order.equals("asc")){
                        lista.clear();
                        lista = listSubstringFiles(id,substring);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o2.getName().compareTo(o1.getName());
                            }
                        });
                    }
                    break;
                case "":
                    if (order.equals("asc")){
                        lista.clear();
                        lista = listAll(storageId);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o2.getName().compareTo(o1.getName());
                            }
                        });
                    }
                    break;
            }
        }

            return lista;
        }
    @Override
    public List sortByDate(String sourceId, String marker1, String order) {
        List <File> lista = new ArrayList<>();
        String[] markerSplit = marker1.split(" ");
        String marker = markerSplit[0];
        String substring = null;
        if(markerSplit.length > 1){
            substring = markerSplit[1];
        }
        String id;
        if (sourceId.equals("x"))
            id =storageId;
        else
            id = sourceId;

        if (existsInStorage(sourceId)){
            switch (marker){
                case "-all":

                    if (order.equals("asc")){
                        lista.clear();
                        lista = listAll(id);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o1.getCreatedTime().getTimeZoneShift(),
                                        o2.getCreatedTime().getTimeZoneShift());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o2.getCreatedTime().getTimeZoneShift(),
                                        o1.getCreatedTime().getTimeZoneShift());
                            }
                        });
                    }

                    break;
                case "-currdir":
                    if (order.equals("asc")){
                        lista.clear();
                        lista = listFiles(id);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o1.getCreatedTime().getTimeZoneShift(),
                                        o2.getCreatedTime().getTimeZoneShift());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o2.getCreatedTime().getTimeZoneShift(),
                                        o1.getCreatedTime().getTimeZoneShift());
                            }
                        });
                    }
                    break;
                case "-currdir+1":
                    if (order.equals("asc")){
                        lista.clear();
                        lista = listDirs(id);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o1.getCreatedTime().getTimeZoneShift(),
                                        o2.getCreatedTime().getTimeZoneShift());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o2.getCreatedTime().getTimeZoneShift(),
                                        o1.getCreatedTime().getTimeZoneShift());
                            }
                        });
                    }
                    break;
                case "-sub":
                    if (order.equals("asc")){
                        lista.clear();
                        lista = listSubstringFiles(id,substring);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o1.getCreatedTime().getTimeZoneShift(),
                                        o2.getCreatedTime().getTimeZoneShift());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o2.getCreatedTime().getTimeZoneShift(),
                                        o1.getCreatedTime().getTimeZoneShift());
                            }
                        });
                    }
                    break;
                case "":
                    if (order.equals("asc")){
                        lista.clear();
                        lista = listAll(storageId);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o1.getCreatedTime().getTimeZoneShift(),
                                        o2.getCreatedTime().getTimeZoneShift());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o2.getCreatedTime().getTimeZoneShift(),
                                        o1.getCreatedTime().getTimeZoneShift());
                            }
                        });
                    }
                    break;
            }
        }

        return lista;
    }

    @Override
    public List sortByModification(String sourceId, String marker1, String order) {
        List <File> lista = new ArrayList<>();
        String[] markerSplit = marker1.split(" ");
        String marker = markerSplit[0];
        String substring = null;
        if(markerSplit.length > 1){
            substring = markerSplit[1];
        }
        String id;
        if (sourceId.equals("x"))
            id =storageId;
        else
            id = sourceId;

        if (existsInStorage(sourceId)){
            switch (marker){
                case "-all":

                    if (order.equals("asc")){
                        lista.clear();
                        lista = listAll(id);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o1.getModifiedTime().getTimeZoneShift(),
                                        o2.getModifiedTime().getTimeZoneShift());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o2.getModifiedTime().getTimeZoneShift(),
                                        o1.getModifiedTime().getTimeZoneShift());
                            }
                        });
                    }

                    break;
                case "-currdir":
                    if (order.equals("asc")){
                        lista.clear();
                        lista = listFiles(id);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o1.getModifiedTime().getTimeZoneShift(),
                                        o2.getModifiedTime().getTimeZoneShift());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o2.getModifiedTime().getTimeZoneShift(),
                                        o1.getModifiedTime().getTimeZoneShift());
                            }
                        });
                    }
                    break;
                case "-currdir+1":
                    if (order.equals("asc")){
                        lista.clear();
                        lista = listDirs(id);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o1.getModifiedTime().getTimeZoneShift(),
                                        o2.getModifiedTime().getTimeZoneShift());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o2.getModifiedTime().getTimeZoneShift(),
                                        o1.getModifiedTime().getTimeZoneShift());
                            }
                        });
                    }
                    break;
                case "-sub":
                    if (order.equals("asc")){
                        lista.clear();
                        lista = listSubstringFiles(id,substring);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o1.getModifiedTime().getTimeZoneShift(),
                                        o2.getModifiedTime().getTimeZoneShift());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o2.getModifiedTime().getTimeZoneShift(),
                                        o1.getModifiedTime().getTimeZoneShift());
                            }
                        });
                    }
                    break;
                case "":
                    if (order.equals("asc")){
                        lista.clear();
                        lista = listAll(storageId);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o1.getModifiedTime().getTimeZoneShift(),
                                        o2.getModifiedTime().getTimeZoneShift());
                            }
                        });
                    }else if (order.equals("desc")) {
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return Integer.compare(o2.getModifiedTime().getTimeZoneShift(),
                                        o1.getModifiedTime().getTimeZoneShift());
                            }
                        });
                    }
                    break;
            }
        }

        return lista;
    }


}
