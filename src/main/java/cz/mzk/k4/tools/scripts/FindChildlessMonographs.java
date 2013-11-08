package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;
import cz.mzk.k4.tools.utils.fedoraUtils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.workers.UuidWorker;
import cz.mzk.k4.tools.workers.readOnly.ChildCounterWorker;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Jan Holman
 * Date: 11/8/13
 * Time: 10:08 AM
 */
public class FindChildlessMonographs implements Script {

    private static FedoraUtils fu = new FedoraUtils();
    private static UuidWorker worker = new ChildCounterWorker();

    @Override
    public void run(List<String> args) {
        fu.applyToAllUuidOfModel(DigitalObjectModel.MONOGRAPH, worker);
    }

    @Override
    public String getUsage() {
        return "vražda smrt zabití";
    }
}
