package cz.mzk.k4.tools.fedoraApi;

import org.junit.Test;

/**
 * Created by rumanekm on 5.2.15.
 */
public class RisearchTest {

    @Test
    public void query() {
        RisearchService service = FedoraFactoryService.getService("http://krameriusdemo.mzk.cz/fedora/");
        service.query("* * *");
        assert(true);
    }

}
