package spaceinvaders;

import ale.agents.AbstractAgent;
import spaceinvaders.BurlapAgent;
import spaceinvaders.BurlapLearningAgent;
import ale.agents.HumanAgent;
import ale.burlap.*;
import ale.burlap.alepolicies.NaiveSIPolicy;
import spaceinvaders.SIALEState;
import spaceinvaders.SIDomainGenerator;
import spaceinvaders.sarsa.AnnealedEpsilonGreedy;
import spaceinvaders.sarsa.MultiObjectTiling;
import spaceinvaders.sarsa.ObjectTiling;
import burlap.behavior.policy.RandomPolicy;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.oomdp.core.Domain;
import burlap.oomdp.visualizer.Visualizer;
import org.opencv.core.Core;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by MelRod on 3/4/16.
 */
public class RunAgents {

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

        String rom = "space_invaders.bin";

        // select agent
        if (agentName.equals("human")) {
            // use the ALE human agent
            AbstractAgent agent = new HumanAgent(rom);
            agent.run();
        } else {
            // use BURLAP agent

            BurlapAgent agent;

            if (agentName.equals("naive")) {
                SIDomainGenerator domGen = new SIDomainGenerator();
                Domain domain = domGen.generateDomain();
                ALEState initialState = new SIALEState();

                Visualizer vis = null;
                if (useGUI) {
                    vis = domGen.getVisualizer();
                }

                agent = new BurlapAgent(new NaiveSIPolicy(domain), domain, initialState, vis, rom, useGUI);
                agent.run(episodes);
            } else if (agentName.equals("sarsa")) {
                SIDomainGenerator domGen = new SIDomainGenerator();
                Domain domain = domGen.generateDomain();
                ALEState initialState = new SIALEState();

                MultiObjectTiling tiling = createMultiObjectTiling();

                GradientDescentSarsaLam learner = new GradientDescentSarsaLam(domain, 0.99, tiling, 0.002, 0.99);
                AnnealedEpsilonGreedy policy = new AnnealedEpsilonGreedy(learner, 1.0D, 0.1D, 500000);
                learner.setLearningPolicy(policy);

                Visualizer vis = null;

                agent = new BurlapLearningAgent(learner, domain, initialState, vis, rom, useGUI);
                agent.run(episodes);

                // save VFA parameters
                try {
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("objectTilings.obj"));
                    objectOutputStream.writeObject(tiling);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (agentName.equals("sarsa_test")) {
                SIDomainGenerator domGen = new SIDomainGenerator();
                Domain domain = domGen.generateDomain();
                ALEState initialState = new SIALEState();


                // load VFA parameters
                MultiObjectTiling tiling;
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("objectTilings.obj"));
                    tiling = (MultiObjectTiling) objectInputStream.readObject();

                    // Dummy learner
                    GradientDescentSarsaLam learner = new GradientDescentSarsaLam(domain, 0.99, tiling, 0.002, 0.6);

                    Visualizer vis = null;
//                    if (useGUI) {
//                        vis = domGen.getParamVisualizer(tiling.objectTilings.get(2), Actions.map("player_a_rightfire"));
//                    }

//                    agent = new BurlapAgent(new EpsilonGreedy(learner, 0.1D), domain, initialState, vis, useGUI);
                    agent = new BurlapLearningAgent(learner, domain, initialState, vis, rom, useGUI);
                    agent.run(episodes);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (!agentName.equals("random")) {
                    System.err.println("Unknown agent was specified. Using Random agent");
                }

                ALEDomainGenerator domGen = new ALEDomainGenerator();
                Domain domain = domGen.generateDomain();
                ALEState initialState = new BlankALEState();

                agent = new BurlapAgent(new RandomPolicy(domain), domain, initialState, null, rom, useGUI);
                agent.run(episodes);
            }
        }
    }

    public static MultiObjectTiling createMultiObjectTiling() {

        int numTilesX = 14;
        int numTilesY = 20;

        ArrayList<ObjectTiling> tilings = new ArrayList<>();

        // Add Agent tiling
        tilings.add(new ObjectTiling(
                ALEDomainConstants.CLASSAGENT,
                ALEDomainConstants.XATTNAME,
                ALEDomainConstants.YATTNAME,
                0, 0, ALEDomainConstants.ALEScreenWidth, ALEDomainConstants.ALEScreenHeight,
                numTilesX, 1
        ));

        // Add Alien tiling
        tilings.add(new ObjectTiling(
                ALEDomainConstants.CLASSALIEN,
                ALEDomainConstants.AGENT_CENT_XATTNAME,
                ALEDomainConstants.AGENT_CENT_YATTNAME,
                -ALEDomainConstants.ALEScreenWidth, -ALEDomainConstants.ALEScreenHeight,
                ALEDomainConstants.ALEScreenWidth, ALEDomainConstants.ALEScreenHeight,
                2*numTilesX, 2*numTilesY
        ));

        // Add Bomb tiling
        tilings.add(new ObjectTiling(
                ALEDomainConstants.CLASS_BOMB_ALIEN,
                ALEDomainConstants.AGENT_CENT_XATTNAME,
                ALEDomainConstants.AGENT_CENT_YATTNAME,
                -ALEDomainConstants.ALEScreenWidth,  -ALEDomainConstants.ALEScreenHeight,
                ALEDomainConstants.ALEScreenWidth, ALEDomainConstants.ALEScreenHeight,
                2*numTilesX, 2*numTilesY
        ));

        return new MultiObjectTiling(tilings);
    }

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
