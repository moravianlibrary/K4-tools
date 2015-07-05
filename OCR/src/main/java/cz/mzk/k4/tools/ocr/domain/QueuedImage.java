package cz.mzk.k4.tools.ocr.domain;

/**
 * Created by holmanj on 16.6.15.
 */
public class QueuedImage {

    // povolené stavy
    public static final String STATE_PROCESSING = "PROCESSING";
    public static final String STATE_DONE = "DONE";
    public static final String STATE_ERROR = "ERROR";
    public static final String STATE_DELETED = "DELETED";

    private String id;
    private String state;
    private String message;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
