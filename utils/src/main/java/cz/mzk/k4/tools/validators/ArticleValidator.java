package cz.mzk.k4.tools.validators;

import cz.mzk.k4.client.api.KrameriusClientApi;
import cz.mzk.k4.tools.utils.AccessProvider;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileInputStream;
import java.io.StringReader;

/**
 * Created by rumanekm on 20.1.15.
 */
public class ArticleValidator {
    public boolean validate(String foxml) {

        // TODO https://github.com/moravianlibrary/K4-tools/issues/1

        return true;
    }

}
