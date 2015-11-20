package cz.mzk.k5.api.client.domain;

/**
 * Created by holmanj on 19.11.15.
 */
public class StreamInfo {

    private String label;
    private String mimeType;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String toString() {
        return "StreamInfo{" +
                "label='" + label + '\'' +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }
}
