package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;
import cz.mzk.k4.tools.workers.UuidWorker;

import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 11/11/13
 */
public class DeletedDocuments implements Script {

    private static FedoraUtils fu = new FedoraUtils();

    @Override
    public void run(List<String> args) {
        fu.applyToAllUuidOfStateDeleted(new UuidWorker() {
            @Override
            public void run(String uuid) {
                System.out.println(uuid);
            }
        });
    }

    @Override
    public String getUsage() {
        return "Vypisuje objekty ve Fedore oznacene jako deleted";
    }
}
