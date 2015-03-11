package cz.mzk.k4.tools.validators;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by rumanekm on 22.1.15.
 */

public class ArticleValidatorTest {

    @Test
    public void invalidArticle() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream invalidArticle = classLoader.getResourceAsStream("invalidArticle.xml");
        String invalidFoxml = IOUtils.toString(invalidArticle);

        InputStream validArticle = classLoader.getResourceAsStream("validArticle.xml");
        String validFoxml = IOUtils.toString(validArticle);

        assertTrue(invalidFoxml.contains("fedora/null"));
        assertFalse(validFoxml.contains("fedora/null"));

    }
}
