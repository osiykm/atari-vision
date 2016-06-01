package ale.io;

import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.common.NullAction;

import java.util.*;

/**
 * Created by MelRod on 5/28/16.
 */
public class ActionSet {

    private String[] actions;
    private Map<String, Integer> actionMap;
    int size;

    public ActionSet(String[] actions) {
        this.actions = actions;
        size = actions.length;

        actionMap = new HashMap<>();
        for (int i = 0; i < actions.length; i++) {
            actionMap.put(actions[i], i);
        }
    }

    public String get(int i) {
        return actions[i];
    }

    public int map(String action) {
        return actionMap.get(action);
    }

    public int size() {
        return size;
    }

    public List<Action> actionList() {
        List<Action> actionList = new ArrayList<>(actions.length);

        for (int i = 0; i < actions.length; i++) {
            Action a = new NullAction(actions[i]);
            actionList.add(a);
        }

        return actionList;
    }
}
