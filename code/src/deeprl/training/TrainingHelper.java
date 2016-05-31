package deeprl.training;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import deeprl.learners.DeepQLearner;
import deeprl.vfa.NNVFA;

/**
 * Created by MelRod on 5/28/16.
 */
public class TrainingHelper {

    DeepQLearner learner;
    NNVFA vfa;
    Policy testPolicy;
    Environment env;

    int maxEpisodeFrames = 10000;
    int totalTrainingFrames = 10000000;

    int testInterval = 100000;
    int numTestEpisodes = 10;

    String snapshotFileName;
    int snapshotInterval = -1;

    public TrainingHelper(DeepQLearner learner, NNVFA vfa, Policy testPolicy, Environment env) {
        this.learner = learner;
        this.vfa = vfa;
        this.testPolicy = testPolicy;
        this.env = env;
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

            if (testCountDown <= 0) {
                runTestSet();
                testCountDown += testInterval;
            } else {
                testCountDown -= ea.numTimeSteps();
            }

            if (snapshotInterval > 0) {
                if (snapshotCountDown == 0) {
                    vfa.saveWeightsTo(snapshotFileName);
                    testCountDown += snapshotInterval;
                } else {
                    snapshotCountDown -= ea.numTimeSteps();
                }
            }

            frameCounter += ea.numTimeSteps();
            episode++;
        }

        System.out.println("Done Training!");
    }

    private void runTestSet() {
        System.out.println("Running Test Set...");
        double totalTestReward = 0;
        for (int e = 1; e <= numTestEpisodes; e++) {
            EpisodeAnalysis ea = runTestEpisode();

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

    private EpisodeAnalysis runTestEpisode() {
        env.resetEnvironment();
        EpisodeAnalysis ea = new EpisodeAnalysis();

        int eFrameCounter = 0;
        while(!env.isInTerminalState() && (eFrameCounter < maxEpisodeFrames || maxEpisodeFrames == -1)){
            State curState = env.getCurrentObservation();
            GroundedAction action = (GroundedAction)testPolicy.getAction(curState);

            EnvironmentOutcome eo = action.executeIn(env);
            ea.recordTransitionTo(eo.a, eo.op, eo.r);

            eFrameCounter++;
        }

        return ea;
    }
}
