package cz.mzk.k4.tools.utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: Martin Rumanek
 * @version: 11/8/13
 */
public class ScriptRunner {
    private static Map<String, Script> scripts = new HashMap<String, Script>();

    public void register(@NotNull String name, @NotNull Script script) {
        if (scripts.containsKey(name)) {
            throw new IllegalArgumentException("Script with this name is already registered.");
        };

        scripts.put(name, script);
    }

    public void run(@NotNull String name, List<String> args) {
        if (!scripts.containsKey(name)) {
            throw new IllegalArgumentException("Script with this name does not exist.");
        };

        scripts.get(name).run(args);
    }

    public Set<String> getAllScriptsName() {
        return scripts.keySet();
    }


}
