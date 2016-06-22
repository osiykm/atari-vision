package edu.brown.cs.atari_vision.ale.burlap;

import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import org.bytedeco.javacpp.opencv_core;

/**
 * @author Melrose Roderick
 */
public interface ALEState extends State {

    ALEState updateStateWithScreen(Domain domain, opencv_core.Mat newScreen);

    ALEState reset();
}
