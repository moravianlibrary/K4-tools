package cz.mzk.k4.tools.validators;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
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
        String validFoxml = IOUtils.toString(invalidArticle);

        ArticleValidator articleValidator = new ArticleValidator();
        assertFalse(articleValidator.validate(invalidFoxml));
        assertTrue(articleValidator.validate(validFoxml));

    }
}
