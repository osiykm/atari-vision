package ale.main;

import ale.agents.*;
import org.opencv.core.Core;

/**
 * Created by MelRod on 3/4/16.
 */
public class Main {

    /** A simple main class for running the Human agent.
     *
     * @param args
     */
    public static void main(String[] args) {
        // Initialize any needed resources
        init();

        // Parameters; default values
        String agentName = "";
        boolean useGUI = true;

        // Parse arguments
        int argIndex = 0;

        boolean doneParsing = (args.length == 0);

        // Loop through the list of arguments
        while (!doneParsing) {
            // -agent: set the agent; default : human
            if (args[argIndex].equals("-agent")) {
                agentName = args[argIndex+1];

                argIndex += 2;
            }

            // -nogui: do not display the Java GUI
            else if (args[argIndex].equals("-nogui")) {
                useGUI = false;
                argIndex++;
            }

            // If the argument is unrecognized, exit
            else {
                printUsage();
                System.exit(-1);
            }

            // Once we have parsed all arguments, stop
            if (argIndex >= args.length)
                doneParsing = true;
        }

        // select agent
        AbstractAgent agent;
        if (agentName.equals("null")) {
            agent = new NullAgent(useGUI);
        } else if (agentName.equals("random")) {
            agent = new RandomAgent(useGUI);
        } else if (agentName.equals("sarsa")) {
            agent = new SarsaAgent(useGUI);
        } else {
            agent = new HumanAgent(useGUI);
        }

        // run agent
        agent.run();
    }

    /** Prints out command-line usage text.
     *
     */
    public static void printUsage() {
        System.err.println ("Invalid argument.");
        System.err.println ("Usage: java [-agent agentName] [-nogui]\n");
        System.err.println ("Example: java HumanAgent -named_pipes /tmp/ale_fifo_");
        System.err.println ("  Will start an agent that communicates with ALE via named pipes \n"+
                "  /tmp/ale_fifo_in and /tmp/ale_fifo_out");
    }

    /**
     * Initializes needed resources.
     */
    public static void init() {
        System.load(System.getProperty("user.dir") + "/dist/lib" + Core.NATIVE_LIBRARY_NAME + ".so");
        //System.load("/usr/local/Cellar/opencv3/3.1.0_1/share/OpenCV/java/libopencv_java310.so");
    }
}
