package cz.mzk.k4.tools.validators;

import org.xml.sax.InputSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

/**
 * Created by rumanekm on 20.1.15.
 */
public class ArticleValidator {
    public boolean validate(String foxml) throws XPathExpressionException {

        String xpath = "//*[local-name()='isOnPage']/@*[local-name()='resource']";
        XPath xPath = XPathFactory.newInstance().newXPath();
        String xpathResult = xPath.evaluate(xpath, new InputSource(new StringReader(foxml)));

        return (!(xpathResult.contains("fedora/null")));
    }

}
