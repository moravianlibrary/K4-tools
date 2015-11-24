package cz.mzk.k5.api.client;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

//            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
//            byte[] buffer = new byte[1024];
//            int len;
//            while ((len = body.in().read(buffer)) > -1 ) {
//                byteOutput.write(buffer, 0, len);
//            }
//            byteOutput.flush();
//
//            return new ByteArrayInputStream(byteOutput.toByteArray());
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
