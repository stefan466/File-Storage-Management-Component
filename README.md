# File Storage Management Library

This project is a file storage management library (component) that provides a set of operations for working with file storage and allows for storage configuration. The library is designed to have a separate specification (API). Additionally, two distinct implementations of this specification have been developed as separate libraries:

1. **Remote Storage on Google Drive**: This implementation stores files in a remote Google Drive and authenticates using a Gmail account.

2. **Local File System Storage**: The second implementation stores files in a local file system.

## Project Overview

The goal of this project is to create a versatile file storage management solution. The library allows you to perform various operations on the storage and supports features like:

- Creating and configuring storage, including specifying storage sizes, disallowing specific file extensions, and limiting the number of files in a directory.
- Basic storage operations such as creating directories, storing files, deleting files and directories, moving files, and renaming files and folders.
- Efficient searching capabilities within the storage, including retrieving files based on various criteria.

## Getting Started

1. Clone the repository or add the library as a dependency in your Java project.
2. Choose the appropriate implementation (remote or local) and follow the documentation to set up the library.
3. Integrate the library into your application to perform file storage and management operations.

