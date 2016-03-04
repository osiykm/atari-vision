package ale.main;

import ale.agents.HumanAgent;
import ale.agents.RLAgent;

/**
 * Created by MelRod on 3/4/16.
 */
public class Main {

    /** A simple main class for running the Human agent.
     *
     * @param args
     */
    public static void main(String[] args) {
        // Parameters; default values
        boolean useGUI = true;
        String namedPipesName = null;
        boolean exportFrames = false;

        // Parse arguments
        int argIndex = 0;

        boolean doneParsing = (args.length == 0);

        // Loop through the list of arguments
        while (!doneParsing) {
            // -nogui: do not display the Java GUI
            if (args[argIndex].equals("-nogui")) {
                useGUI = false;
                argIndex++;
            }
            // -named_pipes <basename>: use to communicate with ALE via named pipes
            //  (instead of stdin/out)
            else if (args[argIndex].equals("-named_pipes") && (argIndex + 1) < args.length) {
                namedPipesName = args[argIndex+1];

                argIndex += 2;
            }
            // -export_frames: use this to save frames as PNG images
            else if (args[argIndex].equals("-export_frames")) {
                exportFrames = true;
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

//        HumanAgent agent = new HumanAgent(useGUI, namedPipesName, exportFrames);
        RLAgent agent = new RLAgent(useGUI, namedPipesName);

        agent.run();
    }

    /** Prints out command-line usage text.
     *
     */
    public static void printUsage() {
        System.err.println ("Invalid argument.");
        System.err.println ("Usage: java HumanAgent [-nogui] [-named_pipes filename] [-export_frames]\n");
        System.err.println ("Example: java HumanAgent -named_pipes /tmp/ale_fifo_");
        System.err.println ("  Will start an agent that communicates with ALE via named pipes \n"+
                "  /tmp/ale_fifo_in and /tmp/ale_fifo_out");
    }
}
