package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: holmanj
 * Date: 12/5/13
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestScript implements Script {

    private static FedoraUtils fedoraUtils = new FedoraUtils(AccessProvider.getInstance());
    private static KrameriusUtils krameriusUtils = new KrameriusUtils(AccessProvider.getInstance());

    @Override
    public void run(List<String> args) {

//        String uuid = args.get(0);
        String filePath = args.get(0);
        List<String> uuidList = GeneralUtils.loadUuidsFromFile(filePath);
        for (String uuid : uuidList) {
            krameriusUtils.export(uuid);
        }

        //fedoraUtils.applyToAllUuid(new ChildrenUuid(uuid, DigitalObjectModel.PAGE));

        //krameriusUtils.setPublic(uuid);
    }

    @Override
    public String getUsage() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
