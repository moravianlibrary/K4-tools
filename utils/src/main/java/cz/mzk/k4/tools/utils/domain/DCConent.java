package cz.mzk.k4.tools.utils.domain;

import java.util.Arrays;

/**
 * Stores infromation from DC stream
 * @author pavels
 */
public class DCConent {
    private String title;
    private String type;
    private String date;
    private String policy;
    private String[] languages;
    private String[] identifiers;
    private String[] publishers;
    private String[] creators;

    /**
     * Returns title from dc
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns type
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }


    /**
     * Returns date
     * @return
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets date
     * @param date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * REturns identifiers
     * @return
     */
    public String[] getIdentifiers() {
        return identifiers;
    }

    /**
     * Sets identifiers
     * @param identifiers
     */
    public void setIdentifiers(String[] identifiers) {
        this.identifiers = identifiers;
    }

    /**
     * Returns publishers
     * @return
     */
    public String[] getPublishers() {
        return publishers;
    }

    /**
     * Sets publishers
     * @param publishers
     */
    public void setPublishers(String[] publishers) {
        this.publishers = publishers;
    }

    /**
     * Returns creators
     * @return
     */
    public String[] getCreators() {
        return creators;
    }

    /**
     * Sets creators
     * @param creators
     */
    public void setCreators(String[] creators) {
        this.creators = creators;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(creators);
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + Arrays.hashCode(identifiers);
        result = prime * result + Arrays.hashCode(publishers);
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DCConent other = (DCConent) obj;
        if (!Arrays.equals(creators, other.creators))
            return false;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (!Arrays.equals(identifiers, other.identifiers))
            return false;
        if (!Arrays.equals(publishers, other.publishers))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DCConent [title=" + title + ", type=" + type + ", date=" + date + ", identifiers=" + Arrays.toString(identifiers) + ", publishers=" + Arrays.toString(publishers) + ", creators=" + Arrays.toString(creators) + "]";
    }

}
