/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.mzk.k4.tools.domain.aleph;



import cz.mzk.k4.tools.utils.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hanis
 */
public class RecordHolder {

    private List<String> tifNames;
    private List<RecordRelation> recordRelations;
    private List<String> barcodes;

    private Configuration configuration = new Configuration();

    public static final String IMAGESERVER_DATA_PREFIX = "/data/georef/mzk03/";
    public static final String IMAGESERVER_PREFIX = "http://imageserver.mzk.cz/mzk03/";
    public final String JP2_PREFIX = configuration.getJP2Prefix();


    public RecordHolder(List<String> tifNames) {
        this.recordRelations = new ArrayList<RecordRelation>();
        this.barcodes = new ArrayList<String>();
        this.tifNames = tifNames;
        createBarcodesList();
    }

    public void handleRecord(String sysno, String barcode) {
        if (getBarcodes().contains(barcode)) {
            RecordRelation rr = new RecordRelation(sysno, barcode, getTifNamesWithPrefix(barcode));
            recordRelations.add(rr);
        }

    }

    public void handleSupplement(String supplementSysno, String mainRecordSysno, String supplementName) {
        if (mainRecordSysno.length() < 9) {
            mainRecordSysno = "00" + mainRecordSysno;
        }
        RecordRelation mainRecord = getRecordRelation(mainRecordSysno);
        if (mainRecord != null) {
            // System.out.println(supplementSysno + "," + mainRecordSysno + ", " + supplementName);
            mainRecord.addSupplement(new RecordRelation(supplementSysno, supplementName));
        }

    }

    private String getBarcodeFromTifName(String tifName) {
        if (tifName.contains(".")) {
            return tifName.substring(0, tifName.indexOf("."));
        } else {
            return tifName;
        }
    }

    private String getExtensionlessTifName(String tifName) {
        if (tifName.contains(".")) {
        return tifName.substring(0, tifName.lastIndexOf("."));
        } else {
            return tifName;
        }
    }


    private List<String> getTifNamesWithPrefix(String barcode) {
        List<String> list = new ArrayList<String>();
        for (String string : tifNames) {
            if (getBarcodeFromTifName(string).equals(barcode)) {
                list.add(string);
            }
        }
        return list;
    }

    /**
     * @return the tifNames
     */
    public List<String> getTifNames() {
        return tifNames;
    }

    /**
     * @param tifNames the tifNames to set
     */
    public void setTifNames(List<String> tifNames) {
        this.tifNames = tifNames;
    }

    /**
     * @return the recordRelations
     */
    public List<RecordRelation> getRecordRelations() {
        return recordRelations;
    }

    /**
     * @param recordRelations the recordRelations to set
     */
    public void setRecordRelations(List<RecordRelation> recordRelations) {
        this.recordRelations = recordRelations;
    }

    public void addRecordRelation(RecordRelation recordRelation) {
        this.recordRelations.add(recordRelation);
    }


    public RecordRelation getRecordRelation(String sysno) {
        for (RecordRelation rr : recordRelations) {
            if (rr.getSysno().equals(sysno)) {
                return rr;
            }
        }
        return null;
    }

    private void createBarcodesList() {
        for (String string : tifNames) {
            getBarcodes().add(getBarcodeFromTifName(string));
        }
    }

    /**
     * @return the barcodes
     */
    public List<String> getBarcodes() {
        return barcodes;
    }

    /**
     * @param barcodes the barcodes to set
     */
    public void setBarcodes(List<String> barcodes) {
        this.barcodes = barcodes;
    }


    public void writeSupplements() {
        for (RecordRelation rr : getRecordRelations()) {
            for (RecordRelation sup : rr.getSupplements()) {
                System.out.println(sup.getSysno());
            }
        }
    }


    public void divideAll() {
        for (RecordRelation rr : recordRelations) {
            rr.divide();
        }
    }

    private String getCuttedSysno(String sysno) {
        return sysno.substring(0, 3) + "/" + sysno.substring(3, 6) + "/" + sysno.substring(6, 9) + "/";
    }



    private String writeSingleImageserverScript(RecordRelation rr) {
        StringBuilder sb = new StringBuilder();
        for (String tifName : rr.getTifNames()) {
            sb.append("mkdir -p ").append(IMAGESERVER_DATA_PREFIX).append(getCuttedSysno(rr.getSysno())).append("\n");
            sb.append("ln -sf ")
                    .append(JP2_PREFIX)
                    .append(getJp2FileName(tifName))
                    .append(" ")
                            //sb.append("rm ")
                    .append(getImageserverDataPath(rr.getSysno(), tifName))
                    .append("\n");
        }
        return sb.toString();
    }


    private String getImageserverDataPath(String sysno, String tifName) {
        return IMAGESERVER_DATA_PREFIX + getCuttedSysno(sysno) + getJp2FileName(tifName);
    }

    private String getImageserverLink(String sysno, String tifName) {
        return IMAGESERVER_PREFIX + getCuttedSysno(sysno) + getExtensionlessTifName(tifName);
    }

    private String writeSingleAlephScript(RecordRelation rr) {
        //SYSNO 85641 L $$LINK$$Digitalizovaný dokument (klikněte pro zobrazení)
        StringBuilder sb = new StringBuilder();
        for (String tifName : rr.getTifNames()) {
            sb.append(rr.getSysno())
                    .append(" 85641 L $$u")
                    .append(getImageserverLink(rr.getSysno(), tifName))
                    .append("$$yDigitalizovaný dokument (klikněte pro zobrazení)")
                    .append("\n");
        }
        return sb.toString();
    }


    public void writeImageserverScript() {
        for (RecordRelation rr : getRecordRelations()) {
            if (rr.isOk()) {
                System.out.print(writeSingleImageserverScript(rr));
                for (RecordRelation sup : rr.getSupplements()) {
                    System.out.print(writeSingleImageserverScript(sup));
                }
            }
        }
    }

    public List<String> getImageserverLinkList() {
        List<String> list = new ArrayList<>();
        for (RecordRelation rr : getRecordRelations()) {
            if (rr.isOk()) {
                list.addAll(getImageserverLinkSingleList(rr));
                //writeSingleImageserverScript(rr);
                for (RecordRelation sup : rr.getSupplements()) {
                    list.addAll(getImageserverLinkSingleList(sup));
                }
            }
        }

        return list;
    }

    private List<String> getImageserverLinkSingleList(RecordRelation rr) {
        List<String> list = new ArrayList<>();
        for (String tifName : rr.getTifNames()) {
            list.add(getImageserverLink(rr.getSysno(), tifName));
        }

        return list;
    }




    public void writeAlephScript() {
        for (RecordRelation rr : getRecordRelations()) {
            if (rr.isOk()) {
                System.out.print(writeSingleAlephScript(rr));
                for (RecordRelation sup : rr.getSupplements()) {
                    System.out.print(writeSingleAlephScript(sup));
                }
            }
        }
    }


    private String getJp2FileName(String tifName) {
        if (tifName.contains(".")) {
            return tifName.substring(0, tifName.lastIndexOf(".")) + ".jp2";
        } else {
            return tifName + ".jp2";
        }
    }


    public int getCorrectCount() {
        int count = 0;
        for (RecordRelation rr : recordRelations) {
            if (rr.isOk()) {
                count += 1 + rr.getSupplements().size();
            }
        }
        return count;
    }

    public int getIncorrectCount() {
        int count = 0;
        for (RecordRelation rr : recordRelations) {
            if (!rr.isOk()) {
                count += 1 + rr.getSupplements().size();
            }
        }
        return count;
    }


}


