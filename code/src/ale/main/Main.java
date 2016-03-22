package ale.main;

import ale.agents.*;
import ale.burlap.*;
import ale.burlap.alelearners.RandomLearner;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.vfa.DifferentiableStateActionValue;
import burlap.behavior.singleagent.vfa.cmac.CMACFeatureDatabase;
import burlap.domain.singleagent.lunarlander.LLVisualizer;
import burlap.oomdp.core.Domain;
import burlap.oomdp.visualizer.Visualizer;
import org.opencv.core.Core;

import java.util.ArrayList;
import java.util.List;

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
        if (agentName.equals("human")) {
            // use the ALE human agent
            AbstractAgent agent = new HumanAgent();
            agent.run();
        } else {
            // use BURLAP agent

            LearningAgent agent;
            ALEState initialState;
            Domain domain;
            if (agentName.equals("sarsa")) {
                SIDomainGenerator domGen = new SIDomainGenerator();
                domain = domGen.generateDomain();
                initialState = new OOALEState();

                agent = createSarsaLearner(domain);
            } else {
                if (!agentName.equals("random")) {
                    System.err.println("Unknown agent was specified. Using Random agent");
                }
                ALEDomainGenerator domGen = new ALEDomainGenerator();
                domain = domGen.generateDomain();
                initialState = new BlankALEState();

                agent = new RandomLearner(domain);
            }

            ALEEnvironment env = new ALEEnvironment(domain, initialState, useGUI);
//            List<EpisodeAnalysis> episodes = new ArrayList<EpisodeAnalysis>();
            for(int i = 0; i < 5000; i++){
                EpisodeAnalysis ea = agent.runLearningEpisode(env);
//                episodes.add(ea);
                System.err.println(i + ": " + ea.maxTimeStep());
                env.resetEnvironment();
            }

//            if (initialState instanceof OOALEState) {
//                SIDomainGenerator domGen = new SIDomainGenerator();
//                Visualizer v = domGen.getVisualizer();
//                new EpisodeSequenceVisualizer(v, domain, episodes);
//            }
        }
    }

    public static GradientDescentSarsaLam createSarsaLearner(Domain domain) {
        int nTilings = 5;
        CMACFeatureDatabase cmac = new CMACFeatureDatabase(nTilings,
                CMACFeatureDatabase.TilingArrangement.RANDOMJITTER);
        double resolution = 10.;

        double xWidth = SIDomainGenerator.objectWidth / resolution;
        double yWidth = SIDomainGenerator.objectWidth / resolution;

        // agent tiling
        cmac.addSpecificationForAllTilings(ALEDomainConstants.CLASSAGENT,
                domain.getAttribute(ALEDomainConstants.XATTNAME),
                xWidth);
        cmac.addSpecificationForAllTilings(ALEDomainConstants.CLASSAGENT,
                domain.getAttribute(ALEDomainConstants.YATTNAME),
                yWidth);

        // alien tiling
        cmac.addSpecificationForAllTilings(ALEDomainConstants.CLASSALIEN,
                domain.getAttribute(ALEDomainConstants.XATTNAME),
                xWidth);
        cmac.addSpecificationForAllTilings(ALEDomainConstants.CLASSALIEN,
                domain.getAttribute(ALEDomainConstants.YATTNAME),
                yWidth);

        // bomb tiling
        cmac.addSpecificationForAllTilings(ALEDomainConstants.CLASSBOMB,
                domain.getAttribute(ALEDomainConstants.XATTNAME),
                xWidth);
        cmac.addSpecificationForAllTilings(ALEDomainConstants.CLASSBOMB,
                domain.getAttribute(ALEDomainConstants.YATTNAME),
                yWidth);
        cmac.addSpecificationForAllTilings(ALEDomainConstants.CLASSBOMB,
                domain.getAttribute(ALEDomainConstants.VYATTNAME),
                1);

        double defaultQ = 0.5;
        DifferentiableStateActionValue vfa = cmac.generateVFA(defaultQ/nTilings);

        return new GradientDescentSarsaLam(domain, 0.99, vfa, 0.02, 0.5);
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
