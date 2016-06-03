package deeprl.training;

import ale.io.ActionSet;
import ale.io.Actions;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.RandomPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import deeprl.learners.DeepQLearner;
import deeprl.vfa.NNVFA;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by MelRod on 5/28/16.
 */
public class TrainingHelper {

    DeepQLearner learner;
    NNVFA vfa;
    Policy testPolicy;
    Environment env;
    ActionSet actionSet;

    int maxEpisodeFrames = 10000;
    int totalTrainingFrames = 10000000;

    int testInterval = 100000;
    int numTestEpisodes = 10;


    List<State> sampleStates;
    int numSampleStates = -1;

    String snapshotFileName;
    int snapshotInterval = -1;

    public TrainingHelper(DeepQLearner learner, NNVFA vfa, Policy testPolicy, ActionSet actionSet, Environment env) {
        this.learner = learner;
        this.vfa = vfa;
        this.testPolicy = testPolicy;
        this.env = env;
        this.actionSet = actionSet;
    }

    public void setNumSampleStates(int n) {
        numSampleStates = n;
    }

    public void setTotalTrainingFrames(int n) {
        totalTrainingFrames = n;
    }

    public void setNumTestEpisodes(int n) {
        numTestEpisodes = n;
    }

    public void setTestInterval(int i) {
        testInterval = i;
    }

    public void setSnapshots(String snapshotFileName, int snapshotInterval) {
        this.snapshotFileName = snapshotFileName;
        this.snapshotInterval = snapshotInterval;
    }

    public void run() {
        if (numSampleStates > 0) {
            System.out.println("Sampling random states");

            // Take a random sample of states
            Policy randomPolicy = new RandomPolicy(actionSet.actionList());
            sampleStates = new ArrayList<>(numSampleStates);

            while (sampleStates.size() < numSampleStates) {
                // Run a random episode
                EpisodeAnalysis ea = runEpisode(randomPolicy);
                int episodeSize = ea.numTimeSteps();

                // Random sample of unique states from the episode
                Random rng = new Random();
                int numStatesToAdd = Math.min(episodeSize, numSampleStates - sampleStates.size());
                for (int i = 0; i < numStatesToAdd; i++) {
                    sampleStates.add(ea.getState(rng.nextInt(episodeSize)));
                }
            }
        }

        int frameCounter = 0;
        int episode = 0;

        int testCountDown = testInterval;
        int snapshotCountDown = snapshotInterval;

        while (frameCounter < totalTrainingFrames) {
            System.out.println(String.format("Training Episode %d at frame %d", episode, frameCounter));

            env.resetEnvironment();
            EpisodeAnalysis ea = learner.runLearningEpisode(env, Math.min(totalTrainingFrames - frameCounter, maxEpisodeFrames));
            double totalReward = 0;
            for (double r : ea.rewardSequence) {
                totalReward += r;
            }
            System.out.println(String.format("Episode reward: %.2f", totalReward));
            System.out.println();

            testCountDown -= ea.numTimeSteps();
            if (testCountDown <= 0) {
                runTestSet();
                testCountDown += testInterval;
            }

            if (snapshotInterval > 0) {
                snapshotCountDown -= ea.numTimeSteps();
                if (snapshotCountDown <= 0) {
                    vfa.saveWeightsTo(snapshotFileName);
                    testCountDown += snapshotInterval;
                }
            }

            frameCounter += ea.numTimeSteps();
            episode++;
        }

        System.out.println("Done Training!");
    }

    public void runTestSet() {

        // Test the MaxQValues of the sample states
        if (sampleStates != null) {
            double totalMaxQ = 0;
            for (State state : sampleStates) {
                totalMaxQ += vfa.qValuesForState(state).maxNumber().doubleValue();
            }

            double averageMaxQ = totalMaxQ/numSampleStates;
            System.out.println(String.format("Average Max Q-Value for sample states: %.3f", averageMaxQ));
        }


        // Run the test policy on test episodes
        System.out.println("Running Test Set...");
        double totalTestReward = 0;
        for (int e = 1; e <= numTestEpisodes; e++) {
            EpisodeAnalysis ea = runEpisode(testPolicy);

            double totalReward = 0;
            for (double reward : ea.rewardSequence) {
                totalReward += reward;
            }

            System.out.println(String.format("%d: Reward = %.2f", e, totalReward));
            totalTestReward += totalReward;
        }

        System.out.println(String.format("Average Test Reward: %.2f", totalTestReward/numTestEpisodes));
        System.out.println();
    }

    public EpisodeAnalysis runEpisode(Policy policy) {
        env.resetEnvironment();
        EpisodeAnalysis ea = new EpisodeAnalysis();

        int eFrameCounter = 0;
        while(!env.isInTerminalState() && (eFrameCounter < maxEpisodeFrames || maxEpisodeFrames == -1)){
            State curState = env.getCurrentObservation();
            GroundedAction action = (GroundedAction)policy.getAction(curState);

            EnvironmentOutcome eo = action.executeIn(env);
            ea.recordTransitionTo(eo.a, eo.op, eo.r);

            eFrameCounter++;
        }

        return ea;
    }
}
