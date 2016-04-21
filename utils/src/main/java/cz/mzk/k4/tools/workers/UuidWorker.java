package cz.mzk.k4.tools.workers;

import java.io.FileNotFoundException;

/**
 * @author: Martin Rumanek
 * @version: 10/22/13
 */
public abstract class UuidWorker {

    // povolení k zápisu
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

    public abstract void run(String uuid) throws FileNotFoundException;
}
