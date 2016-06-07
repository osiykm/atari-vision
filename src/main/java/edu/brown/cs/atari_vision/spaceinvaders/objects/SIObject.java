package edu.brown.cs.atari_vision.spaceinvaders.objects;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.UnknownKeyException;
import edu.brown.cs.atari_vision.ale.burlap.ALEDomainConstants;

import java.util.Arrays;
import java.util.List;

/**
 * @author Melrose Roderick
 */
public abstract class SIObject implements ObjectInstance {

    int x;
    int y;

    int agentCenteredX;
    int agentCenteredY;

    private final static List<Object> keys = Arrays.<Object>asList(
            ALEDomainConstants.XATTNAME, ALEDomainConstants.YATTNAME,
            ALEDomainConstants.AGENT_CENT_XATTNAME, ALEDomainConstants.AGENT_CENT_YATTNAME);

    public SIObject(int x, int y, int agentCenteredX, int agentCenteredY) {
        this.x = x;
        this.y = y;

        this.agentCenteredX = agentCenteredX;
        this.agentCenteredY = agentCenteredY;
    }

    public SIObject(int x, int y, AgentObject agent) {
        this.x = x;
        this.y = y;

        this.agentCenteredX = this.x - agent.x;
        this.agentCenteredY = this.y - agent.y;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return (ObjectInstance)copy();
    }

    @Override
    public String name() {
        return String.format("%S:%d,%d",className(),x,y);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if (variableKey.equals(ALEDomainConstants.XATTNAME)) {
            return x;
        } else if (variableKey.equals(ALEDomainConstants.YATTNAME)) {
            return y;
        } else if (variableKey.equals(ALEDomainConstants.AGENT_CENT_XATTNAME)) {
            return agentCenteredX;
        } else if (variableKey.equals(ALEDomainConstants.AGENT_CENT_YATTNAME)) {
            return agentCenteredY;
        } else {
            throw new UnknownKeyException(variableKey);
        }
    }
}
