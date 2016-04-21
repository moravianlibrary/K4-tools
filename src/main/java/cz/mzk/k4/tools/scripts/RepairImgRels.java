package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.workers.RepairImgRelsWorker;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by holmanj on 24.4.15.
 */
public class RepairImgRels implements Script {
    public static final Logger LOGGER = Logger.getLogger(RepairImgRels.class);
    AccessProvider accessProvider = AccessProvider.getInstance();
    FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);

    public RepairImgRels() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) throws FileNotFoundException {
        RepairImgRelsWorker worker = new RepairImgRelsWorker(true);
        fedoraUtils.applyToAllUuidOfModel(DigitalObjectModel.SOUND_UNIT, worker);

    }

    @Override
    public String getUsage() {
        return null;
    }
}
