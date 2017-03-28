package cz.mzk.k4.tools.utils.util;

import cz.mzk.k4.tools.utils.domain.DCConent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Stack;

/**
 * DC content utils
 * @author pavels
 */
public class DCContentUtils {

    public static final String DC_NAMESPACE_URI = "http://purl.org/dc/elements/1.1/";
    private static final Logger LOGGER = LogManager.getLogger(DCContentUtils.class);

    /**
     * Returns dc content from given document
     * @param DCStream parsed DC stream
     * @return
     */
    public static DCConent parseDCStream(Document DCStream) {
        DCConent content = new DCConent();
        String title = titleFromDC(DCStream);
        if (title != null) content.setTitle(title);

        String model = modelFromDC(DCStream);
        if (model != null) content.setType(model);

        String date = dateFromDC(DCStream);
        if (date != null) content.setDate(date);

        String policy = dateFromDC(DCStream);
        if (policy != null) content.setPolicy(policy);

        String[] languagesFromDC = languageFromDC(DCStream);
        if (languagesFromDC != null) content.setPublishers(languagesFromDC);

        String[] publishersFromDC = publishersFromDC(DCStream);
        if (publishersFromDC != null) content.setPublishers(publishersFromDC);

        String[] creatorsFromDC = creatorsFromDC(DCStream);
        if (creatorsFromDC != null) content.setCreators(creatorsFromDC);

        String[] identsFromDC = identifierlsFromDC(DCStream);
        if (identsFromDC != null) content.setIdentifiers(identsFromDC);

        return content;

    }

    public static String rightsFromDC(org.w3c.dom.Document doc) {
        Element elm = findElement(doc.getDocumentElement(), "rights", DC_NAMESPACE_URI);
        if (elm != null) {
            String policy = elm.getTextContent().trim();
            return policy;
        } else return null;
    }

    /**
     * Returns title from dc stream
     * @param doc parsed DC stream
     * @return
     */
    public static String titleFromDC(org.w3c.dom.Document doc) {
        Element elm = findElement(doc.getDocumentElement(), "title", DC_NAMESPACE_URI);
        if (elm == null) elm = findElement(doc.getDocumentElement(), "identifier", DC_NAMESPACE_URI);
        String title = elm.getTextContent();
        return title;
    }

    /**
     * Returns model from dc stream
     * @param doc parsed DC stream
     * @return
     */
    public static String modelFromDC(org.w3c.dom.Document doc) {
        NodeList nodeList = doc.getElementsByTagNameNS(DC_NAMESPACE_URI, "type");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            String type = item.getTextContent();
            if (type.contains("model:")) {
                return type.split(":")[1];
            }
        }
        return null;
    }

    /**
     * Returns publishers from DC stream
     * @param doc parsed DC stream
     * @return
     */
    public static String[] publishersFromDC(org.w3c.dom.Document doc) {
        ArrayList<String> texts = findElmTexts(doc, "publisher");
        return (String[]) texts.toArray(new String[texts.size()]);
    }

    public static String[] creatorsFromDC(org.w3c.dom.Document dc) {
        ArrayList<String> texts = findElmTexts(dc, "creator");
        return (String[]) texts.toArray(new String[texts.size()]);
    }

    public static String dateFromDC(org.w3c.dom.Document dc) {
        // kontrolovat data ->  1857], [1857], 1981-1984
        ArrayList<String> dates = findElmTexts(dc, "date");
        if (!dates.isEmpty()){
            String date = dates.get(0);
            if (date.matches("\\d\\d\\d\\d")) {
                return date;
            } else if (date.matches(".\\d\\d\\d\\d.")) {
                return date.substring(1,5);
            } else if (date.matches("\\d\\d\\d\\d.")) {
                return date.substring(0,4);
            } else if (date.matches(".\\d\\d\\d\\d")) {
                return date.substring(1,5);
            } else if (date.matches("\\d\\d\\d\\d.\\d\\d\\d\\d")) {
                return date.substring(5,9);
            } else return date;
        } else return null;
    }

    public static String[] languageFromDC(org.w3c.dom.Document dc) {
        ArrayList<String> dates = findElmTexts(dc, "language");
        if (!dates.isEmpty()) return dates.toArray(new String[dates.size()]);
        else return null;
    }


    public static String[] identifierlsFromDC(org.w3c.dom.Document dc) {
        ArrayList<String> identifiers = findElmTexts(dc, "identifier");
        return (String[]) identifiers.toArray(new String[identifiers.size()]);
    }

    public static ArrayList<String> findElmTexts(org.w3c.dom.Document dc, String elmName) {
        ArrayList<String> texts  = new ArrayList<String>();
        Element documentElement = dc.getDocumentElement();
        NodeList childNodes = documentElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                if (item.getLocalName().equals(elmName)) {
                    texts.add(item.getTextContent().trim());
                }
            }
        }
        return texts;
    }

    /**
     * Finds element in DOM tree
     * @param topElm Root node
     * @param localName Local element name
     * @param namespace Element namespace
     * @return found element
     */
    public static Element findElement(Element topElm, String localName, String namespace) {
        Stack<Element> stack = new Stack<Element>();
        stack.push(topElm);
        while (!stack.isEmpty()) {
            Element curElm = stack.pop();
            if ((curElm.getLocalName().equals(localName)) && (namespacesAreSame(curElm.getNamespaceURI(), namespace))) {
                return curElm;
            }
            NodeList childNodes = curElm.getChildNodes();
            for (int i = 0, ll = childNodes.getLength(); i < ll; i++) {
                Node item = childNodes.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    stack.push((Element) item);
                }
            }
        }
        return null;
    }

    private static boolean namespacesAreSame(String fNamespace, String sNamespace) {
        if ((fNamespace == null) && (sNamespace == null)) {
            return true;
        } else if (fNamespace != null) {
            return fNamespace.equals(sNamespace);
        } else
            return false;
    }


}
