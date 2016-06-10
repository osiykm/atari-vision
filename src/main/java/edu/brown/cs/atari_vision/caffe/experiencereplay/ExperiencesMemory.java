package edu.brown.cs.atari_vision.caffe.experiencereplay;

import burlap.mdp.singleagent.environment.EnvironmentOutcome;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public interface ExperiencesMemory {
	void addExperience(EnvironmentOutcome eo);
	List<EnvironmentOutcome> sampleExperiences(int n);
	void resetMemory();
}
