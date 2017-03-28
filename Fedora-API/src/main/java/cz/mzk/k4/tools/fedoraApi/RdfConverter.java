package cz.mzk.k4.tools.fedoraApi;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rumanekm on 5.2.15.
 */
public class RdfConverter implements Converter {

    private static final Logger LOGGER = LogManager.getLogger(RdfConverter.class);

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            List<RelationshipTuple> tripletList = new ArrayList<>();
            String[] lines = IOUtils.toString(body.in()).split("\n");
            for (String line : lines) {
//            String[] tokens = line.split(" "); // platná hodnota je i " ", ale to by se rozdělilo na 2x ", nebyl by to triplet
//            String[] tokens = line.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)"); //split on the space only if that comma has zero, or an even number of quotes ahead of it. - nefunguje, když je např. v title lichý počet "
                String[] tokens = line.split(" (?=([<*>]))"); // rozdělit kolem mezer, za kterýma následuje text ve tvaru <..>
                if (tokens.length == 2) {
                    String[] rest;
                    if (tokens[1].contains("<http://www.w3.org/2001/XMLSchema#dateTime>")) {
                        // 2. token je ve tvaru <info:fedora/fedora-system:def/view#lastModifiedDate> "2011-08-10T09:19:13.641Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
                        tokens[1] = tokens[1].replace("^^<http://www.w3.org/2001/XMLSchema#dateTime>", "");
                    }
                    // 2. token by měl být ve tvaru: <http://purl.org/dc/elements/1.1/language> "cze" .
                    rest = new String[2];
                    rest[0] = tokens[1].substring(0, tokens[1].indexOf(" ")); // všechno po první mezeru (bez ní)
                    rest[1] = tokens[1].substring(tokens[1].indexOf(" ") + 1); // všechno od první mezery (bez ní)
                    String[] tokensCorrected = new String[3];
                    tokensCorrected[0] = tokens[0]; // objekt: <...>
                    tokensCorrected[1] = rest[0];   // predikát <...>
                    tokensCorrected[2] = rest[1];   // subjekt "..."
                    tokens = tokensCorrected;
                }
                if (tokens.length >= 3) {
                    tokens[2] = tokens[2].substring(0, tokens[2].length() - 2); // odstranění " ." z konce
                }

                if (tokens.length < 3) continue;
                try {
                    RelationshipTuple tuple = new RelationshipTuple();
                    tuple.setSubject(tokens[0].substring(1, tokens[0].length() - 1));
                    tuple.setPredicate(tokens[1].substring(1, tokens[1].length() - 1));
                    tuple.setObject(tokens[2].substring(1, tokens[2].length() - 1));
                    tuple.setIsLiteral(false);
                    tripletList.add(tuple);
                } catch (Exception ex) {
                    LOGGER.warn("Problem parsing RDF, skipping line:" + Arrays.toString(tokens) + " : " + ex);
                }
            }
            return tripletList;
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

    @Override
    public TypedOutput toBody(Object object) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
