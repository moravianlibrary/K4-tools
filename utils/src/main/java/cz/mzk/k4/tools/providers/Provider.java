package cz.mzk.k4.tools.providers;

/**
 * Created by rumanekm on 11.12.13.
 */
public interface Provider {
    String take() throws InterruptedException;
}
