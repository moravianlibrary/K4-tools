package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.client.domain.Item;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by holmanj on 21.12.15.
 */
public class SwitchImages implements Script {
    public static final Logger LOGGER = LogManager.getLogger(SwitchImages.class);
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    ProcessRemoteApi k5RemoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);

    public SwitchImages() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {

        // nový ročník 1945:
        String novy = "uuid:2f3eafe4-cc29-490d-9d54-e12223a27cd2";
        // starý ročník 1945:
        String stary = "uuid:26d908df-c651-42db-8449-2cd580af665e";
        try {
            List<Item> novaCisla = k5Api.getChildren(novy);
            System.out.println(novaCisla.size() + " nových čísel");
            List<Item> staraCisla = k5Api.getChildren(stary);
            System.out.println(staraCisla.size() + " starých čísel");
            for (int i = 0; i < novaCisla.size(); i++) {
//                int novychStran = k5Api.getChildren(novaCisla.get(i).getPid()).size();
//                int starychchStran = k5Api.getChildren(staraCisla.get(i).getPid()).size();
//                if (novychStran != starychchStran) {
//                    System.out.println("počty stran se liší v čísle " + novaCisla.get(i).getPid() + " " + staraCisla.get(i).getPid());
//                    System.out.println("nových stran: " + novychStran + ", starých stran: " + starychchStran);
//                }

//                System.out.println(i);
//                List<Item> stareStrany = k5Api.getChildren(staraCisla.get(i).getPid());
//                for (Item strana : stareStrany) {
//                    System.out.println(strana.getPid());
//                }
                k5RemoteApi.deleteObject(staraCisla.get(i).getPid());
            }
        } catch (K5ApiException e) {
            e.printStackTrace();
        }

        // compare child count
        // check child order
        // switch links? nechat nové a smazat staré?

    }

    @Override
    public String getUsage() {
        return null;
    }
}
