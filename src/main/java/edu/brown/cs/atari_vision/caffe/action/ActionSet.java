package edu.brown.cs.atari_vision.caffe.action;


import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MelRod on 5/28/16.
 */
public class ActionSet {

    private Action[] actions;
    private Map<Action, Integer> actionMap;
    int size;

    public ActionSet(String[] actionNames) {
        size = actionNames.length;
        Action[] actions = new Action[size];
        for (int i = 0; i < size; i++) {
            actions[i] = new SimpleAction(actionNames[i]);
        }

        initActionMap();
    }

    public ActionSet(Action[] actions) {
        this.actions = actions;
        size = actions.length;

        initActionMap();
    }

    protected void initActionMap() {
        actionMap = new HashMap<>();
        for (int i = 0; i < actions.length; i++) {
            actionMap.put(actions[i], i);
        }
    }

    public Action get(int i) {
        return actions[i];
    }

    public int map(String action) {
        return actionMap.get(action);
    }

    public int size() {
        return size;
    }

    // TODO: remove
//    public List<Action> actionList() {
//        List<Action> actionList = new ArrayList<>(actions.length);
//
//        for (int i = 0; i < actions.length; i++) {
//            Action a = new SimpleAction(actions[i]);
//            actionList.add(a);
//        }
//
//        return actionList;
//    }
//
//    public List<ActionType> actionTypeList() {
//        List<ActionType> actionList = new ArrayList<>(actions.length);
//
//        for (int i = 0; i < actions.length; i++) {
//            ActionType at = new SimpleActionType(actions[i], this);
//            actionList.add(at);
//        }
//
//        return actionList;
//    }
}