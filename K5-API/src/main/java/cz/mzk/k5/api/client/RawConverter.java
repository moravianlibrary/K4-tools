package cz.mzk.k5.api.client;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * Created by holmanj on 19.11.15.
 */
public class RawConverter implements Converter {

    @Override
    public InputStream fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            return body.in();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public TypedOutput toBody(Object object) {
        return null;
    }
}
