package cz.mzk.k4.tools.providers;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by rumanekm on 11.12.13.
 */
public class ChildrenUuid implements Provider {

    private static final Logger LOGGER = LogManager.getLogger(ChildrenUuid.class);
    private final ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(1000);

    public ChildrenUuid(final String parent, final DigitalObjectModel model) throws FileNotFoundException {

        new Thread(new Runnable() {

            private FedoraUtils fedoraUtils = new FedoraUtils(new AccessProvider());

            @Override
            public void run() {
                try {
                    getChildrenUuids(parent, model);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            private void getChildrenUuids(String uuid, DigitalObjectModel model) throws IOException, InterruptedException {
                try {
                    if (model.equals(fedoraUtils.getModel(uuid))) {
                        queue.put(uuid);

                        LOGGER.debug("Adding " + uuid);
                    }
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                    return;
                }
                DigitalObjectModel parentModel = null;
                ArrayList<ArrayList<String>> children = fedoraUtils.getAllChildren(uuid);

                if (children != null) {
                    for (ArrayList<String> child : children) {
                        getChildrenUuids(child.get(0), model);
                    }
                }



            }
        }).start();


    }

    @Override
    public String take() throws InterruptedException {
        return queue.take();
    }
}
