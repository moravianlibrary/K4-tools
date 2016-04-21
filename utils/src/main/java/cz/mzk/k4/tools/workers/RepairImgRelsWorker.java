package cz.mzk.k4.tools.workers;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.log4j.Logger;

import javax.xml.transform.TransformerException;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by holmanj on 30.4.15.
 */
public class RepairImgRelsWorker extends UuidWorker  {

    private static final Logger LOGGER = Logger.getLogger(RepairImgRelsWorker.class);
    private FedoraUtils fedoraUtils;
    int counter;

    public RepairImgRelsWorker(boolean writeEnabled) throws FileNotFoundException {
        super(writeEnabled);
        fedoraUtils = new FedoraUtils(new AccessProvider());
    }

    @Override
    public void run(String uuid) {
        try {
            fedoraUtils.repairImageserverTilesRelation(uuid);
        } catch (CreateObjectException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        counter++;
        if ((counter % 100) == 0 && counter > 1) {
            LOGGER.debug("Opraveno " + counter + " objektů");
        }
    }
}
