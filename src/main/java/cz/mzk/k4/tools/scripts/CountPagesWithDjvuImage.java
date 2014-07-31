package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedora.Constants;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.workers.RelationshipCounterWorker;
import cz.mzk.k4.tools.workers.UuidWorker;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Created by rumanekm on 30.7.14.
 */
public class CountPagesWithDjvuImage implements Script {

    private AccessProvider accessProvider;
    private KrameriusUtils krameriusUtils;
    private FedoraUtils fedoraUtils = new FedoraUtils(new AccessProvider());
    private static UuidWorker worker = new RelationshipCounterWorker(false);
    private static final Logger LOGGER = Logger.getLogger(FindLonelyMonographs.class);

    @Override
    public void run(List<String> args) {
        accessProvider = new AccessProvider();
        krameriusUtils = new KrameriusUtils(accessProvider);

        String model = args.get(0);
        List<String> uuidList = krameriusUtils.getUuidsByModelSolr(model); // argument

        LOGGER.info("Running " + this.getClass() + " on " + accessProvider.getLibraryPrefix());
        for (String uuid : uuidList) {
            Integer count = null;
            try {
                count = getCountDjvuPages(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(uuid + ":" + count);
        }
    }

    private Integer getCountDjvuPages(String uuid) throws IOException {
        Integer sum = 0;
        List<String> children = fedoraUtils.getChildrenUuids(uuid, DigitalObjectModel.PAGE);
        for (String child : children) {
            String mimetype = fedoraUtils.getMimeTypeForStream(child, Constants.DATASTREAM_ID.IMG_FULL.getValue());
            if ("image/vnd.djvu".equals(mimetype)) {
                return sum++;
            } else {
                break;
            }
        }

        return sum;
    }

    @Override
    public String getUsage() {
        return null;
    }
}
