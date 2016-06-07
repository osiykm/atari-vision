package edu.brown.cs.atari_vision.ale.burlap.action;

import burlap.mdp.core.Action;
import burlap.mdp.core.SimpleAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionType;

import java.util.List;

/**
 * @author Melrose Roderick
 */
public class SimpleActionType implements ActionType {

    String name;
    ActionSet actionSet;

    public SimpleActionType(String name, ActionSet actionSet) {
        this.name = name;
        this.actionSet = actionSet;
    }

    @Override
    public String typeName() {
        return name;
    }

    @Override
    public Action associatedAction(String strRep) {
        return new SimpleAction(name);
    }

    @Override
    public List<Action> allApplicableActions(State s) {
        return actionSet.actionList();
    }
}
