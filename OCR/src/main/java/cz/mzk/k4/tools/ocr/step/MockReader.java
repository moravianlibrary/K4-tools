package cz.mzk.k4.tools.ocr.step;

import cz.mzk.k4.tools.ocr.domain.Img;
import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by holmanj on 15.6.15.
 */

@Scope("Step")
public class MockReader implements ItemReader<Img> {

    private static final Logger LOGGER = Logger.getLogger(MockReader.class);
    private List<String> uuids;

    public MockReader(String rootPid) {
        uuids = new ArrayList<String>();
        uuids.add(rootPid);
    }

    @Override
    public Img read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException  {
        if (!uuids.isEmpty()) {
            String uuid = uuids.get(0);
            uuids.remove(uuid);
            LOGGER.info("Reading document " + uuid);
            return new Img(uuid, null);
        } else {
            return null;
        }
    }
}
