# ATARI

### Dependencies

Arcade Learning Environment (ALE)
OpenCV 3 with Java support

### Installation

To the Java project with ALE, it needs to have access to the ale executable, the ale.cfg file, and a folder containing the Atari ROMs. To do this, symbolic link these files into the code directory.
```sh
ln -s <path-to-ale>/ale <path-to-atari-vision>/code/ale
ln -s <path-to-ale>/ale.cfg <path-to-atari-vision>/code/ale.cfg
ln -s <path-to-rom-directory> <path-to-atari-vision>/code/roms
```

In order to access OpenCV as a maven dependency, OpenCV must be installed as a maven 3rd party Jar. To do this, run:
```sh
mvn install:install-file -Dfile=<path-to-jar> -DgroupId=org.opencv -DartifactId=opencv -Dversion=<version> -Dpackaging=jar
```


The OpenCV native library must also be added to the java.library.path:

On OSX, first copy the .so file to /Library/Java/Extensions:
```sh
cp <path-to-so-file> /Library/Java/Extensions
```
Then, symbolic link a dylib copy to the same location:
```sh
ln -s <path-to-so-file> /Library/Java/Extensions/<name-of-so-file>.dylib
```


### Compilation
To compile to project with maven, simply run:
```sh
mvn package
```

### To run
Since ale is controled with FIFO Pipes, we use a python script to run it:
```sh
python run_python [-h] [--agent AGENT] [--episodes EPISODES] [--rom ROM] [-n]
```


### Agents
Currently we have a few agents built in and are working to add more:
 - human: 60fps controled with the keyboard arrow keys and space-bar
 - random: a random action is selected uniformly at each time step
 - naive: a space-invaders agent that moves left and right only firing when there is an alien above



