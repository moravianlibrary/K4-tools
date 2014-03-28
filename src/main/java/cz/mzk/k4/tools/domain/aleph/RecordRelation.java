/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.mzk.k4.tools.domain.aleph;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hanis
 */
public class RecordRelation {

    public static final int SUPPLEMENT_NUMBER_CONFLICT = 0;
    public static final int SUPPLEMENT_NAME_CONFLICT = 1;
    public static final int UNKNOWN_CONFLICT = 2;
    private List<String> tifNames;
    private List<RecordRelation> supplements;
    private String sysno;
    private String barcode;
    private String supplementName;
    private int supplementNumber;
    private boolean ok = false;
    private int conflictType;

    public RecordRelation(String sysno, String barcode, List<String> tifNames) {
        this.supplements = new ArrayList<RecordRelation>();
        this.sysno = sysno;
        this.barcode = barcode;
        this.tifNames = tifNames;
    }

    public RecordRelation(String sysno, String supplementName) {
        this.supplements = new ArrayList<RecordRelation>();
        this.sysno = sysno;
        this.supplementName = supplementName;
        this.tifNames = new ArrayList<String>();
    }

    /**
     * @return the tifName
     */
    public List<String> getTifNames() {
        return tifNames;
    }

    /**
     * @param tifName the tifName to set
     */
    public void setTifNames(List<String> tifNames) {
        this.tifNames = tifNames;
    }

    public void addTifName(String tifName) {
        this.tifNames.add(tifName);
    }

    /**
     * @return the sysno
     */
    public String getSysno() {
        return sysno;
    }

    public String getCuttedSysno() {
        return sysno.substring(0, 3) + "/" + sysno.substring(3, 6) + "/" + sysno.substring(6, 9) + "/";
    }

    /**
     * @param sysno the sysno to set
     */
    public void setSysno(String sysno) {
        this.sysno = sysno;
    }

    /**
     * @return the barcode
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * @param barcode the barcode to set
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    /**
     * @return the supplements
     */
    public List<RecordRelation> getSupplements() {
        return supplements;
    }

    /**
     * @param supplements the supplements to set
     */
    public void setSupplements(List<RecordRelation> supplements) {
        this.supplements = supplements;
    }

    public void addSupplement(RecordRelation supplement) {
        this.supplements.add(supplement);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sysno).append(", ").append(barcode);
        for (String string : tifNames) {
            sb.append("\n   obrazek: ").append(string);
        }
        for (RecordRelation rr : getSupplements()) {
            sb.append("\n   privazek:").append(rr.getSysno()).append(", ").append(rr.getSupplementName());
        }
        return sb.toString();
    }

    public String toHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append(sysno).append(", ").append(barcode).append("<br>");
        for (String string : tifNames) {
            sb.append("\n   obrazek: ").append(string).append("<br>");
        }
        for (RecordRelation rr : getSupplements()) {
            sb.append("\n   privazek: ").append(rr.getSysno()).append(", ").append(rr.getSupplementName()).append("<br>");
        }
        return sb.toString();
    }

    /**
     * @return the supplementName
     */
    public String getSupplementName() {
        return supplementName;
    }

    /**
     * @param supplementName the supplementName to set
     */
    public void setSupplementName(String supplementName) {
        this.supplementName = supplementName;
    }

    public boolean isMissingSupplement() {
        return getNumberOfExcpectedSupplements() > getSupplements().size();
    }
    
    public boolean isToManySupplements() {
        return getNumberOfExcpectedSupplements() < getSupplements().size();
    }    
    
    

    public int getNumberOfExcpectedSupplements() {
        int tifSup = 0;
        boolean found = true;
        while (found) {
            found = false;
            for (String string : tifNames) {
                if (getTypeFromTifName(string).contains(String.valueOf(tifSup + 1)) && (getTypeFromTifName(string).startsWith("titul")
                      || getTypeFromTifName(string).startsWith("patitul") || getTypeFromTifName(string).startsWith("priv"))) {
                    tifSup++;
                    found = true;
                    break;
                }
            }
        }
        return tifSup;
    }

    private String getTypeFromTifName(String tifName) {
        try {
            return tifName.substring(tifName.indexOf(".") + 1, tifName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException ex) {
             return "";
        }
    }

    public RecordRelation findSupplement(int number) {
        for (RecordRelation supplement : getSupplements()) {
            if (supplement.getSupplementNumber() == number) {
                return supplement;
            }
        }
        return null;
    }

    public void divide() {
        if (isMissingSupplement()) {
            this.ok = false;
            setConflictType(SUPPLEMENT_NUMBER_CONFLICT);
            return;
        }

        for (RecordRelation supplement : getSupplements()) {
            String name = supplement.getSupplementName();
            if (!name.contains("přívazek")) {
                this.ok = false;
                setConflictType(SUPPLEMENT_NAME_CONFLICT);
                return;
            }
            String strNumber = name.substring(name.indexOf("přívazek") + 8).trim();
            if (strNumber.isEmpty()) {
                supplement.setSupplementNumber(1);
            } else {
                try {
                    supplement.setSupplementNumber(Integer.valueOf(strNumber));
                } catch (NumberFormatException ex) {
                    this.ok = false;
                    setConflictType(SUPPLEMENT_NAME_CONFLICT);
                    return;
                }
            }
        }
        List<String> tifNamesToRemova = new ArrayList<String>();
        for (String string : tifNames) {
            
            if(getTypeFromTifName(string).startsWith("titul") 
                    || getTypeFromTifName(string).startsWith("patitul")
                    || getTypeFromTifName(string).startsWith("priv")) {
                int num = getNumberFromTifName(string, "titul");
                if(num == 0) {
                    num = getNumberFromTifName(string, "priv");
                }
                if (num > 0) {
                    RecordRelation supplement = findSupplement(num);
                    if (supplement != null) {
                        supplement.addTifName(string); 
                        tifNamesToRemova.add(string); 
                    } else {
                        this.ok = false;
                        setConflictType(UNKNOWN_CONFLICT);
                        return;
                    }
                }
            }
        }
        tifNames.removeAll(tifNamesToRemova);
        this.ok = true;
    }

    private int getNumberFromTifName(String tifName, String type) {
        String srtNumber = tifName.substring(tifName.indexOf(type) + type.length(), tifName.lastIndexOf("."));
        /*     System.out.println("tifName:" + tifName);
         System.out.println("type:" + type);
         System.out.println("tifName.indexOf(type):" + tifName.indexOf(type));
         System.out.println("type.length():" + type.length());
         System.out.println("tifName.lastIndexOf(\".\"):" + tifName.lastIndexOf("."));
         System.out.println("srtNumber:" + srtNumber);
         */
        if (!srtNumber.isEmpty()) {
            try {
               // System.out.println(srtNumber);
                return Integer.valueOf(srtNumber);
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }

    public boolean isOk() {
        return this.ok;
    }

    /**
     * @return the supplementNumber
     */
    public int getSupplementNumber() {
        return supplementNumber;
    }

    /**
     * @param supplementNumber the supplementNumber to set
     */
    public void setSupplementNumber(int supplementNumber) {
        this.supplementNumber = supplementNumber;
    }

    /**
     * @return the conflictType
     */
    public int getConflictType() {
        return conflictType;
    }

    /**
     * @param conflictType the conflictType to set
     */
    public void setConflictType(int conflictType) {
        this.conflictType = conflictType;
    }
        
}
