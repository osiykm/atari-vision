# ATARI

### Dependencies

Arcade Learning Environment (ALE)
OpenCV 3 with Java support

### Installation


### ALE

To run the Java project with ALE, it needs to have access to the ale executable, the ale.cfg file, and a folder containing the Atari ROMs. To do this, symbolic link these files into the code directory.
```sh
ln -s <path-to-ale>/ale <path-to-atari-vision>/code/ale
ln -s <path-to-ale>/ale.cfg <path-to-atari-vision>/code/ale.cfg
ln -s <path-to-rom-directory> <path-to-atari-vision>/code/roms
```

### JavaCPP Caffe with cuDNN

To run our deep learning experiments, we are using the caffe library.
With the current pom.xml file, all the needed code should be downloaded run smoothly.
But, these repositories compile caffe without cuDNN, which makes the GPU training much slower.
To compile with cuDNN, first clone the JavaCPP presets repo:

```sh
git clone https://github.com/bytedeco/javacpp-presets
```

By default, this library does not compile caffe with cuDNN, so we have to change the cppbuild.sh script.
Replace the javacpp-presets/caffe/cppbuild.sh with the file provided in the instalation_files directory.
Now run these commands to compile JavaCPP caffe and install it to maven:

```sh
./cppbuild.sh install caffe
mvn install --projects caffe
```

Now this library should be linked with maven.

### Linking with an IDE

To open this project in IntelliJ or Eclipse, import it using maven.

In IntelliJ: creat a new "Project from Existing Sources," select the folder, select Maven for "Import project from external model," and then navigate through the project wizard.

### Running

The project can be run and compiled through IntelliJ or Eclipse
