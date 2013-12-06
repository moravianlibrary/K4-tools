/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.mzk.k4.tools.scripts;

import com.google.gwt.user.server.Base64Utils;
import cz.mzk.k4.tools.utils.AccessProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Filip Volner
 */
public class ConvertDjvuToJP2 {

    private static final String FEDORA_URL = "http://krameriustest.mzk.cz/fedora/get/";//"http://fedora.mzk.cz/fedora/get/";
    private static final String RELS_EXT = "/RELS-EXT/";
    private static final String IMG_FULL = "/IMG_FULL/";    
    private static final String DJVU_TO_TIFF = "ddjvu -format=tiff ";
    private static final String DECOMPRESS_TIFF = "convert ";
    private static final String DECOMPRESS_TIFF_PARAM = " +compress ";
    private static final String TIFF_TO_JP2 = "src/main/resources/djatoka/bin/compress.sh djatoka ";

    private static String USER = "";//fedora user
    private static String PASS = "";//fedora password

    private AccessProvider accessProvider;
    
    public void run(String[] args) {
        String uuid = args[0];//"uuid:bdc405b0-e5f9-11dc-bfb2-000d606f5dc6";

        this.accessProvider = new AccessProvider();

        USER = accessProvider.getFedoraUser();
        PASS = accessProvider.getFedoraPassword();
//        FEDORA_URL = accessProvider.getFedoraHost() + "/get/";

        //download and convert djvu images of given periodical uuid
        try {
            downloadImagesOfPeriodical(uuid);
        } catch (IOException ex) {
            Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
        }

        convertDjvuToTiff();

        convertTiffToDecompressedTiff();

        convertDecompressedTiffToJp2();



        //SO FAR AN UNSUCCESFULL ATTEMPT TO UPLOAD CONVERTED IMAGES TO FILESERVER
        //AND CHANGING URL LOCATION IN RELS-EXT

 //       try {
//            FedoraClient fc = new FedoraClient(FEDORA_URL, USER, PASS);
//            FedoraAPIA apia = fc.getFedoraAPIA(); //not really needed
//            FedoraAPIM apim = fc.getAPIM();                       
//           
//            /*******FIRST APPROACH*******/
//        
//            //USE APIM TO DOWNLOAD OBJECT INFO, MODIFY OBJECTS XML REPRESENTATION TO CHANGE THE IMAGE URL
//            
//            byte[] b = apim.export(uuid, "info:fedora/fedora-system:FOXML-1.1" , "public"); //apim.getObjectXML(uuid);
//
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            factory.setNamespaceAware(true);
//            Document doc;
//            DocumentBuilder builder;
//            try {
//                builder = factory.newDocumentBuilder();
//                InputStream is = new ByteArrayInputStream(b);
//                
//                doc = builder.parse(is);                                
//                                
//                NodeList nl = doc.getElementsByTagName("foxml:contentLocation");
//                for (int i=0; i<nl.getLength(); i++)
//                {
//                    if(nl.item(i).getAttributes().getNamedItem("REF").getNodeValue().contains("IMG_"))
//                    {
//                        nl.item(i).getAttributes().getNamedItem("REF").setNodeValue(/*LINK TO IMAGE ON FILESERVER*/"");
//                    }
//                }
//                
//                TransformerFactory tf = TransformerFactory.newInstance();
//                Transformer t = tf.newTransformer();
//                DOMSource source = new DOMSource(doc);
//                ByteArrayOutputStream bos=new ByteArrayOutputStream();
//                StreamResult result=new StreamResult(bos);
//                t.transform(source, result);
//                
//                PrintWriter pw = new PrintWriter(new File("out.xml"),"utf-8");
//                pw.print(new String(bos.toByteArray()));
//                pw.close();
//                
//                //UPLOADING THE OBJECT INFO BACK IS UNSUCCESFULL
//                //apim.purgeObject(uuid, "", false);  //EXPERIMENT BY FIRST DESTROYING THE OBJECT INFO
//                apim.ingest(bos.toByteArray(), "info:fedora/fedora-system:FOXML-1.1", ""); //INGEST CREATES NEW OBJECT, THEREFORE IT HAD TO BE DESTROYED ABOVE
//                
//            } catch (ParserConfigurationException ex) {
//                Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (SAXException ex) {
//                Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (TransformerConfigurationException ex) {
//                Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (TransformerException ex) {
//                Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//            /*******SECOND APPROACH*******/
//        
//            //GET AND MODIFY DATASTREAM, KEEPING ALL ITS ORIGINAL VALUES EXCEPT THE IMAGE URL - UNSUCCESFULL
//            //THE XML REPRESENTATION DOES NOT CHANGE THE URL ELEMENT, HOWEVER THE IMAGE IS REPLACED IN FEDORA ITSELF - NOT WHAT WE WANT
//            Datastream ds = apim.getDatastream(uuid, "IMG_FULL", null);
//            apim.modifyDatastreamByReference(uuid, ds.getID(), ds.getAltIDs(), ds.getLabel(), ds.getMIMEType(), ds.getFormatURI(), "http://krameriustest.mzk.cz/fedora/objects/uuid:2f9ab1f7-ed09-11df-9847-0050569e4b0b/datastreams/IMG_FULL/content", ds.getChecksumType(), ds.getChecksum(), "Modify datastream location.", true);
//
//
//
//
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ServiceException ex) {
//            Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
//        }



    }

    private static URLConnection openConnection(String urlStr, String user, String pass) {
        URLConnection uc = null;
        try {
            URL url = new URL(urlStr);

            uc = url.openConnection();

            String userPassword = user + ":" + pass;
            String encoded = Base64Utils.toBase64(userPassword.getBytes());
            uc.setRequestProperty("Authorization", "Basic " + encoded);

        } catch (MalformedURLException ex) {
            Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
        }
        return uc;
    }

    private static Document getDocument(String uuid) {
        Document parseDocument = null;

        HttpURLConnection connection = null;
        InputStream stream = null;
        try {
            connection = (HttpURLConnection) openConnection(FEDORA_URL + uuid + RELS_EXT, USER, PASS);

            stream = connection.getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            parseDocument = builder.parse(stream);

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            if (connection != null) {
                connection.disconnect();
            }

            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return parseDocument;
    }

    private static Set<String> getVolumesUuidList(Document doc) {
        Set<String> uuidList = new HashSet<String>();
        NodeList nl = doc.getElementsByTagName("hasVolume");

        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            uuidList.add((String) n.getAttributes().getNamedItem("rdf:resource").getNodeValue().substring(12));
        }

        return uuidList;
    }

    private static Set<String> getItemsUuidList(Document doc) {
        Set<String> uuidList = new HashSet<String>();
        NodeList nl = doc.getElementsByTagName("kramerius:hasItem");

        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            uuidList.add((String) n.getAttributes().getNamedItem("rdf:resource").getNodeValue().substring(12));
        }

        return uuidList;
    }

    private static Set<String> getPagesUuidList(Document doc) {
        Set<String> uuidList = new HashSet<String>();
        NodeList nl = doc.getElementsByTagName("kramerius:hasPage");

        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            uuidList.add((String) n.getAttributes().getNamedItem("rdf:resource").getNodeValue().substring(12));
        }

        return uuidList;
    }

    private static void getImage(String uuid) {
        HttpURLConnection connection = null;
        connection = (HttpURLConnection) openConnection(FEDORA_URL + uuid + IMG_FULL, USER, PASS);

        byte[] buffer = new byte[8 * 1024];

        InputStream input = null;

        try {
            input = connection.getInputStream();
            OutputStream output = new FileOutputStream(new File("images/" + uuid + ".djvu"));

            try {
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            } catch (IOException ex) {
                Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                output.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private static void convertDecompressedTiffToJp2() {
        for (File image : new File("images").listFiles()) {
            if (image.getAbsolutePath().endsWith("decompressed.tiff")) {
                //System.out.println("converting to jp2 - " + image.getName());
                try {
                    synchronized (ConvertDjvuToJP2.class) {
                        Runtime.getRuntime().exec(TIFF_TO_JP2 + image.getAbsolutePath() + " " + image.getAbsolutePath().replaceAll(".tiff", ".jp2")).waitFor();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private static void convertTiffToDecompressedTiff() {
        for (File image : new File("images").listFiles()) {
            if (image.getAbsolutePath().endsWith(".tiff") && !image.getAbsolutePath().endsWith("decompressed.tiff")) {
                //System.out.println("decompressing " + image.getName());
                String out = image.getAbsolutePath().split("/")[image.getAbsolutePath().split("/").length - 1].replaceAll(".tiff", "");
                try {
                    synchronized (ConvertDjvuToJP2.class) {
                        Runtime.getRuntime().exec(DECOMPRESS_TIFF + image.getAbsolutePath() + DECOMPRESS_TIFF_PARAM + image.getAbsolutePath().replaceAll(out, out + "_decompressed")).waitFor();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private static void convertDjvuToTiff() {
        for (File image : new File("images").listFiles()) {
            if (image.getAbsolutePath().endsWith(".djvu")) {
                //System.out.println("Converting to tiff - " + image.getName());
                try {
                    synchronized (ConvertDjvuToJP2.class) {
                        Runtime.getRuntime().exec(DJVU_TO_TIFF + image.getAbsolutePath() + " " + image.getAbsolutePath().replaceAll(".djvu", ".tiff")).waitFor();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private static void downloadImagesOfPeriodical(String uuid) throws IOException {
        File dir = new File("images");
        if (!dir.exists() || !dir.isDirectory()) {
            if (!(new File("images").mkdir())) {
                throw new IOException("Could not make directory to store images.");
            }
        }

        for (String volumeUuid : getVolumesUuidList(getDocument(uuid))) {
            //BREAK FOR TESTING PURPOSES
            for (String itemUuid : getItemsUuidList(getDocument(volumeUuid))) {
                for (String pageUuid : getPagesUuidList(getDocument(itemUuid))) {
                    getImage(pageUuid);
                    //break;
                }
                //break;
            }
            //break;
        }
    }
    
}
