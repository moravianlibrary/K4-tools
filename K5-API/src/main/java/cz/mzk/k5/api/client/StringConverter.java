package cz.mzk.k5.api.client;

import retrofit.converter.*;
import retrofit.mime.*;
import sun.reflect.generics.reflectiveObjects.*;
import java.io.*;
import java.lang.reflect.*;

/**
 * Created by holmanj on 8.2.15.
 */
public class StringConverter implements Converter {
//    private static final MediaType MEDIA_TYPE = MediaType.parse("text/plain; charset=UTF-8");

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        // konverze na String
        String textResponse = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(body.in()));
            StringBuilder out = new StringBuilder();
            String newLine = System.getProperty("line.separator");
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append(newLine);
            }

            textResponse = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textResponse;
    }

    @Override
    public TypedOutput toBody(Object object) {
        throw new NotImplementedException();
    }
}
