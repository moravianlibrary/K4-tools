package cz.mzk.k5.api.client.domain;

/**
 * Created by holmanj on 5.2.15.
 */
public class Details {

    // page
//    private String pagenumber; // TODO zatím dělá problémy: https://github.com/ceskaexpedice/kramerius/issues/211
    private String type;

    // periodical item:
    private String issueNumber; // používá se?
//    private String date;  // TODO zatím dělá problémy: https://github.com/ceskaexpedice/kramerius/issues/211
    private String partNumber; // aspoň u Lidovek je číslo tady
    private String year;
//    private String volumeNumber;  // TODO zatím dělá problémy: https://github.com/ceskaexpedice/kramerius/issues/211

//    public String getPagenumber() {
//        return pagenumber;
//    }
//
//    public void setPagenumber(String pagenumber) {
//        this.pagenumber = pagenumber;
//    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(String issueNumber) {
        this.issueNumber = issueNumber;
    }

//    public String getDate() {
//        return date;
//    }
//
//    public void setDate(String date) {
//        this.date = date;
//    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

//    public String getVolumeNumber() {
//        return volumeNumber;
//    }
//
//    public void setVolumeNumber(String volumeNumber) {
//        this.volumeNumber = volumeNumber;
//    }
}
