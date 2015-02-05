package cz.mzk.k4.tools.fedoraApi;

import org.apache.commons.io.IOUtils;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by rumanekm on 5.2.15.
 */
public class RdfConverter implements Converter {
    @Override
    //TODO triplet.class
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        if (String.class.equals(type)) {
            try {
                return IOUtils.toString(body.in());
            } catch (IOException e) {
                throw new ConversionException(e);
            }
        }
        return null;
    }

    @Override
    public TypedOutput toBody(Object object) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
