package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;
import cz.mzk.k4.tools.workers.UuidWorker;
import cz.mzk.k4.tools.workers.readOnly.HasPagesWorker;

/**
 * Created with IntelliJ IDEA.
 * User: Jan Holman
 * Date: 11/8/13
 * Time: 10:08 AM
 */
public class FindPagelessMonographs {

    private static FedoraUtils fu = new FedoraUtils();
    private static UuidWorker worker = new HasPagesWorker();
    private static String model = "monograph";

    public static void run() {
        fu.applyToAllUuidOfModel(model, worker);
    }
}
