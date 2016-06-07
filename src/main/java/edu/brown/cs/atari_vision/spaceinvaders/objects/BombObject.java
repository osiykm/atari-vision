package edu.brown.cs.atari_vision.spaceinvaders.objects;

import burlap.mdp.core.state.State;

/**
 * @author Melrose Roderick
 */
public class BombObject extends SIObject {

    String className;

    public BombObject(int x, int y, AgentObject agent, String className) {
        super(x, y, agent);

        this.className = className;
    }

    @Override
    public String className() {
        return className;
    }

    @Override
    public State copy() {
        return new AgentObject(this.x, this.y);
    }

}
