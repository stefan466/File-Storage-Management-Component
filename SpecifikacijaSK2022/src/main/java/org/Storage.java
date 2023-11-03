package org;

import java.util.List;

public abstract class Storage {

    public String path;

    /**
    * Initialises storage for the first time. If there is already a file with the given name on
    * the location given in the second parameter it takes that directory as a root.
    *
    * @param name the name of the root directory (usually "Storage")
    * @param path the path/id of the parent directory
     *
     * @return returns true if the creation of the root directory is successfull
     */
    public abstract boolean initStorage(String name, String path);

    /**
     * Creates a new directory.
     *
     * @param name the name of the new directory
     * @param destinationPath path/ID of the parent directory
     */
    public abstract void createNewFile(String name, String destinationPath);

    /**
     * Creates new multiple directories with names that only differ by numbers next to them.
     *
     * @param n starting number for the name
     * @param m ending number for the name
     * @param name the name, excluding the numbers
     * @param path the path/ID of the parent directory
     * @param extension at the end of the file name
     */
    public void createFromTo(int n, int m, String name, String path, String extension){
        for(int i = n ; i <= m ; i++){

            if(extension != null)
                createNewFile(name.concat(String.valueOf(i)).concat(extension), path);
            else
                createNewFile(name.concat(String.valueOf(i)), path);
        }
    }

    /**
     * Moves file from one location to another.
     * Folders cannot be copied for the Google Drive Implementation.
     *
     * @param sourcePath
     * @param destinationPath
     */
    public abstract void moveFile(String sourcePath, String destinationPath);

    public void moveFiles(List<String> paths, String destinationPath){

        for(String path : paths){

            moveFile(path, destinationPath);
        }
    }

    /**
     * Copies a file to another location.
     * Folders cannot be copied in Google Drive Implementation.
     *
     * @param sourcePath path/ID of the file to be copied
     * @param destinationPath path/ID of the location for te copy of the original file
     */
    public abstract void copyFile(String sourcePath, String destinationPath);

    /**
     * Uses the copyFile method to copy multiple files.
     * @param paths
     * @param destinationPath
     */
    public void copyFiles(List<String> paths, String destinationPath){

        for(String path : paths){

            copyFile(path, destinationPath);
        }
    }
    /**
     * Downloads a file from GoogleDrive to the local memory.
     * For local implementation downloads the file to the location outside the root directory.
     * @param sourcePath
     */
    public abstract void downloadFile(String sourcePath);
    public void downloadFiles(List<String> paths){

        for(String path : paths){

            downloadFile(path);
        }
    }
    /**
     * Deletes a file from the directory.
     * @param sourcePath path/ID of a file to be deleted
     */
    public abstract void deleteFile(String sourcePath);
    public void deleteFiles(List<String> paths){

        for(String path : paths){

            deleteFile(path);
        }
    }

    /**
     * Uploads file to the Storage.
     * @param path
     * @return returns true if the upload was successfull.
     */
    //NOVA - IMPL U TEST
    public abstract boolean uploadFile(String path);

    /**
     * Lists files that have been created in the given time period
     * and are a part of the Storage.
     *
     * @param path path/ID of the given directory that contains files to bre checked
     * @param period "dd.mm.yyyy.-dd.mm.yyyy." format of the time period
     * @return list of files that were created in the given time window
     */
    public abstract List listFilesCreatedPeriod(String path, String period);

    /**
     * Lists all files in every directory that is a child of the given directory,
     * or any of its children.
     *
     * @param path path/ID of the operation root directory
     * @return list of files that the operation root directory contains
     */
    public abstract List listAll(String path);

    /**
     * Lists all files contained only in the given directory.
     * @param path path/ID of the directory
     * @return list of files contained only in given directory
     */
    public abstract List listFiles(String path);

    /**
     * Lists all files contained in the given directory, and all the subdirectories (one level below).
     * @param path path/ID of the directory
     * @return list of files contained in the given directory, and all the subdirectory
     */
    public abstract List listDirs(String path);

    /**
     * Lists all files in the given directory that have the name given.
     * @param path path/ID of the directory
     * @param name name to search files
     * @return  list of files that are named as asked
     */
    public abstract List listByName(String path, String name);

    /**
     * Renames a file.
     * @param path path/ID of the file
     * @param newName new name for the file
     */
    public abstract void renameFile(String path, String newName);

    /**
     * Lists all the files in the given directory containing the extension given.
     * @param path path/ID of the directory
     * @param extension extension for the search
     * @return List of the files containing the extension
     */
    public abstract List listFilesWithExt(String path, String extension);//

    /**
     * Lists all the files in the given directory containing the substring given in the name.
     * @param path path/ID of the directory
     * @param substring substring for the name search
     * @return List of all the files whose names contain the given substring
     */
    public abstract List listSubstringFiles(String path, String substring);

    /**
     * Checks if the directory contains the file(s).
     * The method uses a list of Strings regardless of the number of files to be checked.
     * @param path path/ID of the directory
     * @param files List of files
     * @return true if all the files are contained, false if there is at least one file not contained
     */
    public abstract boolean containsFile(String path, List<String> files);

    /**
     * Returns the parent of the file with the given name (not path).
     * @param path name of the file
     * @return directory in which the file is contained
     */
    public abstract String returnDir(String path);

    /**
     * Lists the files sorted by name.
     * @param path path/ID of the operation root directory
     * @param marker "-all, -currdir, ..." depending on the listing method
     *              the user wishes to list the files before sorting
     * @param order "asc" or "desc" ascending or descending order
     * @return sorted list of files listed with the parameters given above
     */
    public abstract List sortByName(String path, String marker, String order);

    /**
     * Lists the files sorted by time created.
     * @param path path/ID of the operation root directory
     * @param marker "-all, -currdir, ..." depending on the listing method
     *              the user wishes to list the files before sorting
     * @param order "asc" or "desc" ascending or descending order
     * @return sorted list of files listed with the parameters given above
     */
    public abstract List sortByDate(String path, String marker, String order);

    /**
     * Lists the files sorted by last modification time.
     * @param path path/ID of the operation root directory
     * @param marker "-all, -currdir, ..." depending on the listing method
     *              the user wishes to list the files before sorting
     * @param order "asc" or "desc" ascending or descending order
     * @return sorted list of files listed with the parameters given above
     */
    public abstract List sortByModification(String path, String marker, String order);

    /**
     * Path getter.
     * @return path
     */
    public String getPath() {
        return path;
    }

    /**
     * Path setter
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }


}
