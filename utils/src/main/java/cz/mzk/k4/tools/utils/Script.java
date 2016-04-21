package cz.mzk.k4.tools.utils;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 11/8/13
 */
public interface Script {

    public void run(List<String> args) throws FileNotFoundException;

    public String getUsage();

}
