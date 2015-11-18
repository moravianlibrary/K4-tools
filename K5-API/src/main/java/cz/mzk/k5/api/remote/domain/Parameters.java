package cz.mzk.k5.api.remote.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by holmanj on 16.11.15.
 */
public class Parameters {
    // {"parameters":["first","second","third"]}
    // "{"parameters":[" + pid_path + "," + pid_path + "]}";
    final List<String> parameters;

    public Parameters(String... parameters) {
        this.parameters = new ArrayList<String>();
        for (String parameter : parameters) {
            this.parameters.add(parameter);
        }
    }
}

