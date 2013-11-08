package cz.mzk.k4.tools.utils;

/**
 * @author: Martin Rumanek
 * @version: 11/8/13
 */
public interface Script {
    public void run(String args[]);

    public String getUsage();
}
