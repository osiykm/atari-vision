# ATARI

### Dependencies

Arcade Learning Environment (ALE)
OpenCV 3 with Java support

### Installation

To run the Java project with ALE, it needs to have access to the ale executable, the ale.cfg file, and a folder containing the Atari ROMs. To do this, symbolic link these files into the code directory.
```sh
ln -s <path-to-ale>/ale <path-to-atari-vision>/code/ale
ln -s <path-to-ale>/ale.cfg <path-to-atari-vision>/code/ale.cfg
ln -s <path-to-rom-directory> <path-to-atari-vision>/code/roms
```

### Linking with an IDE

To open this project in IntelliJ or Eclipse, import it using maven.

In IntelliJ: creat a new "Project from Existing Sources," select the folder, select Maven for "Import project from external model," and then navigate through the project wizard.

### Running

The project can be run and compiled through IntelliJ or Eclipse


### OpenCV

Some parts of the code require OpenCV. To run this code, the OpenCV native library must also be added to the java.library.path:



