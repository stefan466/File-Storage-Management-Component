package local_storage_impl;

import org.Storage;
import org.StorageManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;

public class LocalStorageImplementation extends Storage {

    static {
        StorageManager.registerStorage(new LocalStorageImplementation());
    }

    public LocalStorageImplementation() {

    }

    @Override
    public boolean initStorage(String name, String path) {
        File root = new File(path + File.separator + name);

        if(root.exists()){
            File[] files = root.listFiles();

            if(files.length != 0) {

                boolean valid = false;

                for (File file : files) {
                    if (file.getName().equals("config.properties")) {
                        valid = true;
                        break;
                    }
                }

                if(valid){
                    System.out.println("Uspesna konekcija na vec postojece skladiste!");
                    System.out.println("Za pomoc ukucajte /help");
                    return true;
                }else {
                    System.out.println("Konekcija neuspesna!");
                    return false;
                }
            } else {

                File config = new File(root, "config.properties");
                try {
                    config.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Uspesno kreiranje skladista na novom folderu!");
                System.out.println("Za pomoc ukucajte /help");

                return true;
            }
        }else {

            root.mkdir();

            File config = new File(root, "config.properties");
            try {
                config.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Uspesno kreiranje novog foldera kao skladiste!");
            System.out.println("Za pomoc ukucajte /help");
            return true;
        }

    }

    @Override
    public void createNewFile(String newFilePath, String destPath) {
        if(newFilePath.equals("config.properties")) {
            System.out.println("Nije dozvoljeno kreiranje fajla pod nazivom config.properties");
            return;
        }

        if(destPath.equals("") || destPath.startsWith(File.separator)) {
            String rootPath = super.getPath();

            File newFile = new File(rootPath.concat(destPath).concat(System.getProperty("file.separator")).concat(newFilePath));
            if(newFile.exists()) {
                System.out.println("Fajl sa imenom " + newFile + " vec postoji.");
            } else {
                if(newFilePath.contains(".")) {
                    try {
                        boolean success = newFile.createNewFile();
                        if(success) {
                            System.out.println("Uspesno kreiran fajl na putanji " + newFile.getPath());
                        }

                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    newFile.mkdir();
                    System.out.println("Uspesno kreiran fajl na putanji " + newFile.getPath());
                }
            }
        } else if(!destPath.startsWith(File.separator)) {
            System.out.println("Pogresno zadata putanja destinacije. Putanja mora poceti separatorom");
        }

    }
    @Override
    public void moveFile(String path, String destinationPath) {
        if(path.equals("config.properties")){
            System.out.println("Nije dozvoljeno premestanje fajla config.properties!");
            return;
        }

        if(!destinationPath.startsWith(File.separator)){
            System.out.println("Pogreno zadata putanja destinacije. Putanja mora poceti separatorom.");
            return;
        }

        String rootPath = super.getPath();
        File sourceFile = new File(rootPath.concat(System.getProperty("file.separator")).concat(path));

        if(!sourceFile.exists()) {
            System.out.println("Fajl " + sourceFile.getPath() + " ne postoji");
        }

        String pattern = Pattern.quote(System.getProperty("file.separator"));
        String[] parsedPath = sourceFile.getPath().split(pattern);
        String fileName = parsedPath[parsedPath.length-1];


        File destFile = new File(rootPath.concat(destinationPath));

        if(!destFile.exists()) {
            System.out.println("Zadata putanja na kojoj se premesta fajl/fajlovi ne postoji. Kreira se direktorijum sa nazivom " + destinationPath.substring(1));
            destFile.mkdir();
        }

        File newSourceFile = new File(rootPath.concat(destinationPath).
                concat(System.getProperty("file.separator")).
                concat(fileName));

        boolean valid = true;
        for(String name : destFile.list()) {
            if(name.equals(newSourceFile.getName())) {
                valid=false;
                break;
            }
        }
        if(valid) {
            sourceFile.renameTo(newSourceFile);
            System.out.println("Fajl (" + sourceFile.getPath() + ") je uspesno premesten na putanju " + newSourceFile.getPath());
        } else {
            System.out.println("Na putanji " + destFile.getPath() + " postoji fajl sa istim imenom.");
        }



    }

    @Override
    public void copyFile(String sourcePath, String destPath) {
        if(sourcePath.equals("config.properties")) {
            System.out.println("Nije dozvoljeno kopiranje fajla config.properties");
            return;
        }
        if(!destPath.startsWith(File.separator)) {
            System.out.println("Pogresno zadata putanja destinacije. Putanja mora zapoceti separatorom");
            return;
        }

        String rootPath = super.getPath();

        String pattern = Pattern.quote(System.getProperty("file.separator"));
        String[] pathElements = sourcePath.split(pattern);
        String desiredFilePath = pathElements[pathElements.length-1];

        File sourceFile = new File(rootPath.concat(System.getProperty("file.separator")).concat(sourcePath));
        File destFile = new File(rootPath.concat(destPath));
        File desiredDestFile = new File(rootPath.concat(destPath).concat(System.getProperty("file.separator")).concat(desiredFilePath));

        if(sourceFile.isFile()) {
            boolean valid = true;
            for(String name : destFile.list()) {
                if(name.equals(sourceFile.getName())) {
                    valid=false;
                    break;
                }
            }
            if(valid) {
                try {
                  fileCopy(sourceFile, desiredDestFile);
                }  catch(IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Uspesno kopiranje fajla" + sourceFile.getPath() + " na putanju " + desiredDestFile.getPath());
            } else {
                System.out.println("Fajl na putanji " + desiredDestFile + " vec postoji");
            }
        } else if(sourceFile.isDirectory()) {
            boolean valid = true;
            for(String name : destFile.list()) {
                if(name.equals(sourceFile.getName())) {
                    valid = true;
                    break;
                }
            }
            if(valid) {
                try {
                    copyDir(sourceFile, desiredDestFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Uspesno kopiranje direktorijuma " + sourceFile.getPath() + " na putanju " + desiredDestFile.getPath());
            } else {
                System.out.println("Direktorijum na putanji " + desiredDestFile + " vec postoji.");
            }
        } else {
            System.out.println("Prosledjeni fajl/direktorijum ne postoji");
        }


    }

    private void fileCopy(File src, File dest) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dest);


            byte[] buffer = new byte[1024];
            int length;

            while((length=in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } finally {
            if(in!=null) {
                in.close();
            }if(out!=null) {
                out.close();
            }
        }
    }

    public void copyDir(File src, File dest) throws IOException {
        if(src.isDirectory()) {
            if(!dest.exists()) {
                dest.mkdir();
            }
            String files[] = src.list();

            for(String fileName : files) {
                File srcFile = new File(src, fileName);
                File destFile = new File(dest, fileName);

                copyDir(srcFile, destFile);
            }
        } else {
            if(!dest.exists()) {
                fileCopy(src, dest);
            }
        }
    }

    @Override
    public void downloadFile(String sourcePath) {
        String rootPath = super.getPath();

        String pattern = Pattern.quote(System.getProperty("file.separator"));
        String[] pathElements = sourcePath.split(pattern);
        String desiredFilePath = pathElements[pathElements.length - 1];

        String DOWNLOADSPATH = System.getProperty("user.home").concat(System.getProperty("file.separator")).concat("Downloads");

        File sourceFile = new File(rootPath.concat(System.getProperty("file.separator")).concat(sourcePath));
        File newSourceFile = new File(DOWNLOADSPATH + File.separator + desiredFilePath);

        if(sourceFile.isFile()){
            try {
                fileCopy(sourceFile, newSourceFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Uspesno preuzimanje fajla sa putanje " + sourceFile.getPath());
        } else if (sourceFile.isDirectory()){
            try {
                this.copyDir(sourceFile, newSourceFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Uspesno preuzimanje direktorijuma sa putanje " + sourceFile.getPath());
        } /*else {
            System.out.println("Prosledjeni fajl/direktorijum na putanji " + sourceFile.getPath() + " ne postoji.");
        }*/

    }

    @Override
    public void deleteFile(String path) {
        if(path.equals("config.properties")) {
            System.out.println("Nije dozvoljeno brisanje fajla config.properties");
            return;
        }
        String rootPath = super.getPath();

        File file = new File(rootPath.concat(System.getProperty("file.separator")).concat(path));

        if(file.isFile()) {
            boolean success = file.delete();

            if(success) {
                System.out.println("Uspesno obrisan fajl na putanji " + file.getPath());

            } else if(file.isDirectory()) {
                try {
                    Files.walk(Path.of(file.getPath()))
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);

                } catch(IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Prosledjeni fajl/direktorijim na putanji " + file.getPath() + " ne postoji");

            }
        }

    }

    @Override
    public boolean uploadFile(String s) {
        return false;
    }

    @Override
    public List listFilesCreatedPeriod(String s, String s1) {
        return null;
    }

    @Override
    public List<File> listAll(String path) {
        List<File> finalList = new ArrayList<>();

        if(path.equals("") || path.startsWith(File.separator)) {
            String rootPath = super.getPath();
            String absolutePath;

            if (path.equals("\\")){
                 absolutePath = rootPath;
            }else {
                 absolutePath = rootPath + path;
            }

            File file = new File(absolutePath);

            if(file == null) {
                System.out.println("Nije pronadjen fajl pod zadatim imenom");
            }
            if(file.isDirectory()) {
                try {
                    addToFinal(file, finalList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                System.out.println("Data putanja ne pripada folderu");



        } else if(!path.startsWith(File.separator)) {
            System.out.println("Pogresno zadata putanja destinacije. Putanja mora poceti separatorom");
        }

        return finalList;
    }

    public static void addToFinal(File file, List<File> finalList) throws IOException {
        if(file.isDirectory()) {
            File[] files = file.listFiles();

            for(File f : files) {
                if(!f.isDirectory()) {
                    finalList.add(f);
                } else {
                    //finalList.add(f);
                    addToFinal(f, finalList);
                }
            }
        }



    }

    public boolean existInStorage(String path) {
        List<File> dirs = listAll(path);
        List<String> dirPaths = new ArrayList<>();

        for(File f : dirs) {
            dirPaths.add(f.getName());
        }
        if(dirPaths.contains(path))
            return true;
        else
            return false;
    }



    private void showDir(int indent, File file) throws IOException{
        for(int i=0; i<indent; i++) {
            if(i==indent-2) {
                if(file.isDirectory())
                    System.out.println(">");
                else if(file.isFile())
                    System.out.println("-");
            } else {
                System.out.println(" ");
            }
        }
        System.out.println(file.getName());
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            for(int i=0; i<files.length; i++) {
                showDir(i+3, files[i]);
            }
        }
    }

    @Override
    public List<File> listFiles(String path) {
        List<File> fileList = new ArrayList<>();

            if(path.equals("") || path.startsWith(File.separator)) {

                String rootPath = super.getPath();
                String absolutePath = rootPath + path;

                File dir = new File(absolutePath);
                System.out.println(dir);

                if(dir == null) {
                    System.out.println("Nije pronadjen direktorijium sa zadatim imenom");
                }
                File[] files = dir.listFiles();

                for (File f: files){
                    //if (!f.isDirectory())
                        fileList.add(f);
                }

            }
         else if(!path.startsWith(File.separator)) {
            System.out.println("Pogresno zadatak putanja destinacije. Putanja mora poceti separatorom");

        }
        return fileList;

    }


     // vrati sve fajlove u zadatom direktorijumu i svim poddirektorijumima
    @Override
    public List<File> listDirs(String path) {
        List<File> fileList = new ArrayList<>();

            if (path.equals("") || path.startsWith(File.separator)) {
                String rootPath = super.getPath();

                String absolutePath = rootPath.concat(path);
                File file = new File(absolutePath);

                File[] files = file.listFiles();

                for (File f: files){
                    if(f.isDirectory()) {
                        File[] files1 = f.listFiles();
                        for(File f1: files1){
                            fileList.add(f1);
                        }
                    }else
                        fileList.add(f);
                }

            } else if (!path.startsWith(File.separator)) {
                System.out.println("Pogresno zadata putanja destinacije. Putanja mora zapoceti separtorom");
            }



        return fileList;
    }

    @Override
    public List listByName(String path, String fileName) {
        List<File> finalList = new ArrayList<>();
        if (existInStorage(path)) {
            List<File> lista = listAll(path);
            for (File f : lista) {
                if (f.getName().equals(fileName))
                    finalList.add(f);
            }
        } else {
            System.out.println("Skladiste ne sadrzi zadatu putanju.");
        }

        return finalList;
    }

    @Override
    public void renameFile(String path, String name) {

            if (path.equals("") || path.startsWith(File.separator)) {
                String rootPath = super.getPath();

                String absolutePath = rootPath.concat(path);
                File file = new File(absolutePath);

                if (file == null) {
                    System.out.println("Ne postoji fajl sa zadatom putanjom");
                }
                if(file.renameTo(new File(rootPath + "\\" + name)))
                    System.out.println("Uspesno promenjeno ime fajlu.");
                else
                    System.out.println("Rename neuspesan.");
            }else if(!path.startsWith(File.separator)) {
                System.out.println("Pogresno zadata putanja destinacije. Putanja mora poceti separatorom");
            }
        }


    @Override
    public List listFilesWithExt(String path, String extension) {
        List<File> finalList = new ArrayList<>();

            List<File> lista = listAll(path);
            for (File f : lista) {
                if (f.getName().endsWith(extension))
                    finalList.add(f);
            }

        return finalList;
    }

     //vrati fajlove koji u svom imenu sadrže, počinju, ili se završavaju nekim
       //zadatim podstringom
    @Override
    public List listSubstringFiles(String path, String substring) {
        List<File> finalList = new ArrayList<>();

            List<File> lista = listAll(path);
            for (File f : lista) {
                if (f.getName().contains(substring))
                    finalList.add(f);
            }

        return finalList;
    }

    @Override
    public boolean containsFile(String path, List<String> fileNames) {
        boolean sadrzi = true;
        if (path.equals("") || path.startsWith(File.separator)) {
            String rootPath = super.getPath();

            String absolutePath = rootPath.concat(path);
            File file = new File(absolutePath);

            if (file == null) {
                System.out.println("Ne postoji fajl sa zadatom putanjom");
            }
            File[] files = file.listFiles();
            List<String> childrenNames = new ArrayList<>();

            for (File f: files){
                childrenNames.add(f.toString());
            }



            for (String s: fileNames){
                //System.out.println(rootPath + "\\" + s);
                if (!childrenNames.contains(rootPath + "\\" + s))
                    sadrzi = false;
            }

        }else if(!path.startsWith(File.separator)) {
            System.out.println("Pogresno zadata putanja destinacije. Putanja mora poceti separatorom");
        }
        return sadrzi;
    }

    /** vratiti u kom folderu se nalazi fajl sa određenim zadatim imenom */
    @Override
    public String returnDir(String name) {

        String rootPath = super.getPath();
        List<File> fajlovi = listAll("\\");
        Path path;
        File file = null;

        for (File f: fajlovi){
            path = Paths.get(f.toString());

            if (path.getFileName().toString().equals(name))
                file = new File(path.toString()).getParentFile();

        }


        if(file.exists())
            return file.getName();
        else
            return "error";

    }

    @Override
    public List sortByName(String source, String marker1, String order) {


        String[] markerSplit = marker1.split(" ");
        String marker = markerSplit[0];
        String substring = null;

        if(markerSplit.length > 1){
            substring = markerSplit[1];
        }
        String path;
        if (source.equals("x"))
            path = source;
        else
            path = source;

        List <File> lista = new ArrayList<>();

            switch (marker){
                case "-all":

                    if (order.equals("asc")){
                        lista.clear();
                        lista = listAll(path);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                    }else if (order.equals("desc")) {
                        lista.clear();
                        lista = listAll(path);
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
                        lista = listFiles(path);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                    }else if (order.equals("desc")) {
                        lista.clear();
                        lista = listFiles(path);
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
                        lista = listDirs(path);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                    }else if (order.equals("desc")) {
                        lista.clear();
                        lista = listDirs(path);
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
                        lista = listSubstringFiles(path,substring);
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                    }else if (order.equals("desc")) {
                        lista.clear();
                        lista = listSubstringFiles(path, substring);
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
                        lista = listAll(super.getPath());
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                    }else if (order.equals("desc")) {
                        lista.clear();
                        lista = listAll(super.getPath()); //C:\Users\matij\Documents\Storage
                        Collections.sort(lista, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o2.getName().compareTo(o1.getName());
                            }
                        });
                    }
                    break;

        }

        return lista;
    }

    public static long getFileCreationEpoch (File file) {
        try {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(),
                    BasicFileAttributes.class);
            return attr.creationTime()
                    .toInstant().toEpochMilli();
        } catch (IOException e) {
            throw new RuntimeException(file.getAbsolutePath(), e);
        }
    }
    @Override
    public List sortByDate(String source, String marker1, String order) {


        String[] markerSplit = marker1.split(" ");
        String marker = markerSplit[0];
        String substring = null;

        if (markerSplit.length > 1) {
            substring = markerSplit[1];
        }
        String path;
        if (source.equals("x"))
            path = source;
        else
            path = source;

        List<File> lista = new ArrayList<>();

        switch (marker) {
            case "-all":

                if (order.equals("asc")) {
                    lista.clear();
                    lista = listAll(path);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            long l1 = getFileCreationEpoch(o1);
                            long l2 = getFileCreationEpoch(o2);
                            return Long.valueOf(l1).compareTo(l2);

                        }
                    });
                } else if (order.equals("desc")) {
                    lista.clear();
                    lista = listAll(path);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            long l1 = getFileCreationEpoch(o1);
                            long l2 = getFileCreationEpoch(o2);
                            return Long.valueOf(l2).compareTo(l1);

                        }
                    });
                }

                break;
            case "-currdir":
                if (order.equals("asc")) {
                    lista.clear();
                    lista = listFiles(path);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            long l1 = getFileCreationEpoch(o1);
                            long l2 = getFileCreationEpoch(o2);
                            return Long.valueOf(l1).compareTo(l2);

                        }
                    });
                } else if (order.equals("desc")) {
                    lista.clear();
                    lista = listFiles(path);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            long l1 = getFileCreationEpoch(o1);
                            long l2 = getFileCreationEpoch(o2);
                            return Long.valueOf(l2).compareTo(l1);

                        }
                    });
                }
                break;
            case "-currdir+1":
                if (order.equals("asc")) {
                    lista.clear();
                    lista = listDirs(path);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            long l1 = getFileCreationEpoch(o1);
                            long l2 = getFileCreationEpoch(o2);
                            return Long.valueOf(l1).compareTo(l2);

                        }
                    });
                } else if (order.equals("desc")) {
                    lista.clear();
                    lista = listDirs(path);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            long l1 = getFileCreationEpoch(o1);
                            long l2 = getFileCreationEpoch(o2);
                            return Long.valueOf(l2).compareTo(l1);

                        }
                    });
                }
                break;
            case "-sub":
                if (order.equals("asc")) {
                    lista.clear();
                    lista = listSubstringFiles(path, substring);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            long l1 = getFileCreationEpoch(o1);
                            long l2 = getFileCreationEpoch(o2);
                            return Long.valueOf(l1).compareTo(l2);

                        }
                    });
                } else if (order.equals("desc")) {
                    lista.clear();
                    lista = listSubstringFiles(path, substring);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            long l1 = getFileCreationEpoch(o1);
                            long l2 = getFileCreationEpoch(o2);
                            return Long.valueOf(l2).compareTo(l1);

                        }
                    });
                }
                break;
            case "":
                if (order.equals("asc")) {
                    lista.clear();
                    lista = listAll(super.getPath());
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            long l1 = getFileCreationEpoch(o1);
                            long l2 = getFileCreationEpoch(o2);
                            return Long.valueOf(l1).compareTo(l2);

                        }
                    });
                } else if (order.equals("desc")) {
                    lista.clear();
                    lista = listAll(super.getPath()); //C:\Users\matij\Documents\Storage
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            long l1 = getFileCreationEpoch(o1);
                            long l2 = getFileCreationEpoch(o2);
                            return Long.valueOf(l2).compareTo(l1);

                        }
                    });
                }
                break;

        }

        return lista;
    }

    @Override
    public List sortByModification(String source, String marker1, String order) {


        String[] markerSplit = marker1.split(" ");
        String marker = markerSplit[0];
        String substring = null;

        if (markerSplit.length > 1) {
            substring = markerSplit[1];
        }
        String path;
        if (source.equals("x"))
            path = source;
        else
            path = source;

        List<File> lista = new ArrayList<>();

        switch (marker) {
            case "-all":

                if (order.equals("asc")) {
                    lista.clear();
                    lista = listAll(path);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return Long.valueOf(o1.lastModified()).compareTo(o2.lastModified());
                        }
                    });
                } else if (order.equals("desc")) {
                    lista.clear();
                    lista = listAll(path);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return Long.valueOf(o2.lastModified()).compareTo(o1.lastModified());
                        }
                    });
                }

                break;
            case "-currdir":
                if (order.equals("asc")) {
                    lista.clear();
                    lista = listFiles(path);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return Long.valueOf(o1.lastModified()).compareTo(o2.lastModified());
                        }
                    });
                } else if (order.equals("desc")) {
                    lista.clear();
                    lista = listFiles(path);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return Long.valueOf(o2.lastModified()).compareTo(o1.lastModified());
                        }
                    });
                }
                break;
            case "-currdir+1":
                if (order.equals("asc")) {
                    lista.clear();
                    lista = listDirs(path);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return Long.valueOf(o1.lastModified()).compareTo(o2.lastModified());
                        }
                    });
                } else if (order.equals("desc")) {
                    lista.clear();
                    lista = listDirs(path);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return Long.valueOf(o2.lastModified()).compareTo(o1.lastModified());
                        }
                    });
                }
                break;
            case "-sub":
                if (order.equals("asc")) {
                    lista.clear();
                    lista = listSubstringFiles(path, substring);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return Long.valueOf(o1.lastModified()).compareTo(o2.lastModified());
                        }
                    });
                } else if (order.equals("desc")) {
                    lista.clear();
                    lista = listSubstringFiles(path, substring);
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return Long.valueOf(o2.lastModified()).compareTo(o1.lastModified());
                        }
                    });
                }
                break;
            case "":
                if (order.equals("asc")) {
                    lista.clear();
                    lista = listAll(super.getPath());
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return Long.valueOf(o1.lastModified()).compareTo(o2.lastModified());
                        }
                    });
                } else if (order.equals("desc")) {
                    lista.clear();
                    lista = listAll(super.getPath()); //C:\Users\matij\Documents\Storage
                    Collections.sort(lista, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return Long.valueOf(o2.lastModified()).compareTo(o1.lastModified());
                        }
                    });
                }
                break;

        }

        return lista;
    }
}
