package cz.mzk.k4.tools.workers;

/**
 * @author: Martin Rumanek
 * @version: 10/22/13
 */
public abstract class UuidWorker {

    // worker má právo na zápis
    private boolean writeEnabled;

    public UuidWorker(boolean writeEnabled) {
        this.writeEnabled = writeEnabled;
    }

    public boolean isWriteEnabled() {
        return writeEnabled;
    }

    public void setWriteEnabled(boolean writeEnabled) {
        this.writeEnabled = writeEnabled;
    }

    public abstract void run(String uuid);
}
