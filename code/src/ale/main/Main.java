package ale.main;

import ale.agents.AbstractAgent;
import ale.agents.BurlapAgent;
import ale.agents.HumanAgent;
import ale.burlap.*;
import ale.burlap.alepolicies.NaiveSIPolicy;
import burlap.behavior.policy.RandomPolicy;
import burlap.oomdp.core.Domain;
import burlap.oomdp.visualizer.Visualizer;
import org.opencv.core.Core;

/**
 * Created by MelRod on 3/4/16.
 */
public class Main {

    // Load external libraries
    static {
        // load OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /** A simple main class for running the Human agent.
     *
     * @param args
     */
    public static void main(String[] args) {

        // Parameters; default values
        String agentName = "";
        int episodes = 1;
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

            // -episodes: set the episodes; default : 1
            else if (args[argIndex].equals("-episodes")) {
                episodes = Integer.valueOf(args[argIndex+1]);

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
        if (agentName.equals("human")) {
            // use the ALE human agent
            AbstractAgent agent = new HumanAgent();
            agent.run();
        } else {
            // use BURLAP agent

            BurlapAgent agent;

            if (agentName.equals("naive")) {
                SIDomainGenerator domGen = new SIDomainGenerator();
                Domain domain = domGen.generateDomain();
                ALEState initialState = new OOALEState();

                Visualizer vis = null;
                if (useGUI) {
                    vis = domGen.getVisualizer();
                }

                agent = new BurlapAgent(new NaiveSIPolicy(domain), domain, initialState, vis, useGUI);
            } else {
                if (!agentName.equals("random")) {
                    System.err.println("Unknown agent was specified. Using Random agent");
                }

                ALEDomainGenerator domGen = new ALEDomainGenerator();
                Domain domain = domGen.generateDomain();
                ALEState initialState = new BlankALEState();

                agent = new BurlapAgent(new RandomPolicy(domain), domain, initialState, null, useGUI);
            }

            agent.run(episodes);
        }
    }

//    public static GradientDescentSarsaLam createSarsaLearner(Domain domain) {
//        int nTilings = 5;
//        CMACFeatureDatabase cmac = new CMACFeatureDatabase(nTilings,
//                CMACFeatureDatabase.TilingArrangement.RANDOMJITTER);
//        double resolution = 10.;
//
//        double xWidth = SIDomainGenerator.objectWidth / resolution;
//        double yWidth = SIDomainGenerator.objectWidth / resolution;
//
//        // agent tiling
//        cmac.addSpecificationForAllTilings(ALEDomainConstants.CLASSAGENT,
//                domain.getAttribute(ALEDomainConstants.XATTNAME),
//                xWidth);
//        cmac.addSpecificationForAllTilings(ALEDomainConstants.CLASSAGENT,
//                domain.getAttribute(ALEDomainConstants.YATTNAME),
//                yWidth);
//
//        // alien tiling
//        cmac.addSpecificationForAllTilings(ALEDomainConstants.CLASSALIEN,
//                domain.getAttribute(ALEDomainConstants.XATTNAME),
//                xWidth);
//        cmac.addSpecificationForAllTilings(ALEDomainConstants.CLASSALIEN,
//                domain.getAttribute(ALEDomainConstants.YATTNAME),
//                yWidth);
//
//        // bomb tiling
//        cmac.addSpecificationForAllTilings(ALEDomainConstants.CLASSBOMB,
//                domain.getAttribute(ALEDomainConstants.XATTNAME),
//                xWidth);
//        cmac.addSpecificationForAllTilings(ALEDomainConstants.CLASSBOMB,
//                domain.getAttribute(ALEDomainConstants.YATTNAME),
//                yWidth);
//        cmac.addSpecificationForAllTilings(ALEDomainConstants.CLASSBOMB,
//                domain.getAttribute(ALEDomainConstants.VYATTNAME),
//                1);
//
//        double defaultQ = 0.5;
//        DifferentiableStateActionValue vfa = cmac.generateVFA(defaultQ/nTilings);
//
//        return new GradientDescentSarsaLam(domain, 0.99, vfa, 0.02, 0.5);
//    }

    /** Prints out command-line usage text.
     *
     */
    public static void printUsage() {
        System.err.println ("Invalid argument.");
        System.err.println ("Usage: java [-agent agentName] [-episodes episodes] [-nogui] \n");
        System.err.println ("Example: java HumanAgent -named_pipes /tmp/ale_fifo_");
        System.err.println ("  Will start an agent that communicates with ALE via named pipes \n"+
                "  /tmp/ale_fifo_in and /tmp/ale_fifo_out");
    }
}
