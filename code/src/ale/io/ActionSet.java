package ale.io;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
}
