package cz.mzk.k4.tools.scripts.rajhradValidate;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hradskam on 24.8.14.
 */
public class StopProcesses implements Script {
    @Override
    public void run(List<String> args) {

        if(args.size() !=1 ) {
            System.out.println("spatne");
            return;
        }

        AccessProvider accessProvider = AccessProvider.getInstance();
        KrameriusUtils krameriusUtils = new KrameriusUtils(accessProvider);
            String pid = args.get(0);
            krameriusUtils.stopProcessess(pid);



    }

    @Override
    public String getUsage() {
        return null;
    }
}
