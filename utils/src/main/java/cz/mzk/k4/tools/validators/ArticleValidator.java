package cz.mzk.k4.tools.validators;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

/**
 * Created by rumanekm on 20.1.15.
 */
public class ArticleValidator {

    private static final Logger LOGGER = Logger.getLogger(ArticleValidator.class);

    public boolean validate(String relsext) {

        String xpath = "//*[local-name()='kramerius:isOnPage']/@*[local-name()='resource']";
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            String xpathResult = xPath.evaluate(xpath, new InputSource(new StringReader(relsext)));
            return (!(xpathResult.contains("fedora/null")));
        }  catch (XPathExpressionException e) {
            LOGGER.error("XPath error " + e);

        }

        return false;


    }

}
