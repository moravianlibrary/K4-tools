package cz.mzk.k4.tools.scripts.rajhradValidate;

import cz.mzk.k4.tools.Configuration;
import cz.mzk.k4.tools.domain.aleph.RecordHolder;
import cz.mzk.k4.tools.domain.aleph.RecordRelation;
import cz.mzk.k4.tools.scripts.RajhradValidate;
import org.marc4j.MarcException;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author: Martin Rumanek
 * @version: 11/13/13
 */
public class Validation {

    Configuration configuration = new Configuration();


    public void validate(RecordHolder holder, String path) {
        InputStream in = null;
        int exc = 0;
        int i = 0;
        int j = 0;
        int k = 0;
        try {
            in = new FileInputStream(path);
            MarcReader reader = new MarcPermissiveStreamReader(in, true, true, "UTF-8");
            while (reader.hasNext()) {
                try {
                    Record record = reader.next();

                    String sysno = getSingleSubfield(record, "990", "a");
                    //System.out.println(sysno);
                    String barcode = getSingleSubfield(record, "980", "1");
                    holder.handleRecord(sysno, barcode);
                } catch (MarcException e) {
                    j++;
                } catch (ArrayIndexOutOfBoundsException e) {
                    // System.out.println("EXCEPTION: " + ++exc);

                }
            }
            in.close();
            in = new FileInputStream(path);
            reader = new MarcPermissiveStreamReader(in, true, true, "UTF-8");
            while (reader.hasNext()) {
                try {
                    Record record = reader.next();
                    String lkr = getSingleSubfield(record, "LKR", "a");
                    if ("UP".equals(lkr)) {

                        String sysno = getSingleSubfield(record, "990", "a");
                        String mainSysno = getSingleSubfield(record, "LKR", "b");
                        String supplementName = getSingleSubfield(record, "LKR", "m");
                        //System.out.println(sysno + ", " + mainSysno + ", " + supplementName);
                        holder.handleSupplement(sysno, mainSysno, supplementName);
                    }
                } catch (MarcException e) {
                    j++;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("X");
                }
            }
            in.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(RajhradValidate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RajhradValidate.class.getName()).log(Level.SEVERE, null, ex);

        }
//        System.out.println(i + ", " + j + ", " + k);
//
//        for (RecordRelation rr : holder.getRecordRelations()) {
//            System.out.println(rr);
//        }
//        for (String b : holder.getBarcodes()) {
//            System.out.println(b);
//        }
//        System.out.println("-----------------------------");
//
//

        holder.divideAll();
        int c = 0;


        System.out.println("<h2>Varovani: Prebyvaji privazky</h2>");
        for (RecordRelation rr : holder.getRecordRelations()) {
            if (!rr.isOk() && rr.getConflictType() == RecordRelation.SUPPLEMENT_NUMBER_CONFLICT &&
                    rr.getNumberOfExcpectedSupplements() < rr.getSupplements().size()) {
                c++;
                System.out.println("<h3>Varovani: ocekavany pocet privazku: <b>" + rr.getNumberOfExcpectedSupplements()
                        + "</b> skutecny pocet privazku: <b>" + rr.getSupplements().size() + "</b></h3><p>" + rr.toHtml() + "</p>");
            }
        }

        System.out.println("Celkem: " + c + "<br/>");
        c = 0;
        System.out.println("<h2>Chyby: Chybi privazky</h2>");
        for (RecordRelation rr : holder.getRecordRelations()) {
            if (!rr.isOk() && rr.getConflictType() == RecordRelation.SUPPLEMENT_NUMBER_CONFLICT &&
                    rr.getNumberOfExcpectedSupplements() > rr.getSupplements().size()) {
                c++;
                System.out.println("<h3>Chyba: ocekavany pocet privazku: <b>" + rr.getNumberOfExcpectedSupplements()
                        + "</b> skutecny pocet privazku: <b>" + rr.getSupplements().size() + "</b></h3><p>" + rr.toHtml() + "</p>");
            }
        }
        System.out.println("Celkem: " + c + "<br/>");


        System.out.println("<h2>Chyby v nazvech privazku</h2>");
        c = 0;
        for (RecordRelation rr : holder.getRecordRelations()) {
            if (!rr.isOk() && rr.getConflictType() == RecordRelation.SUPPLEMENT_NAME_CONFLICT) {
                c++;
                System.out.println(rr.getSysno() + "<br/>");
            }
        }
        System.out.println("Celkem: " + c + "<br/>");
        c = 0;
        System.out.println("<h2>Nezname chyby</h2>");
        for (RecordRelation rr : holder.getRecordRelations()) {
            if (!rr.isOk() && rr.getConflictType() == RecordRelation.UNKNOWN_CONFLICT) {
                c++;
                System.out.println(rr.getSysno() + "<br/>");
            }
        }
        System.out.println("Celkem: " + c + "<br/>");


//        System.out.println("-----------------------------------------------");
//        c = 0;
//        for (RecordRelation rr : holder.getRecordRelations()) {
//            if (rr.isOk()) {
//                c++;
//              //  System.out.println(rr.getSysno());
//            }
//        }
//        System.out.println("dobre celkem: " + c);
//
//
//
//        System.out.println("correct count: " + holder.getCorrectCount());
//        System.out.println("incorrect count: " + holder.getIncorrectCount());
    }

    private String getSingleSubfield(Record record, String field, String subfield) {
        DataField dataField = (DataField) record.getVariableField(field);
        if (dataField == null) {
            return "";
        }
        Subfield dataSubfield = dataField.getSubfield(subfield.charAt(0));
        if (dataSubfield == null) {
            return "";
        }
        return dataSubfield.getData();
    }

}
