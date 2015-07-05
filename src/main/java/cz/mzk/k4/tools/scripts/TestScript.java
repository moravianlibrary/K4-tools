package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.api.ClientRemoteApi;
import cz.mzk.k4.tools.api.KrameriusClientRemoteApiFactory;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import domain.Item;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    AccessProvider accessProvider = new AccessProvider();
    ClientRemoteApi krameriusApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());

    @Override
    public void run(List<String> args) {

//        String uuid = args.get(0);
//        String filePath = args.get(0);
//        List<String> uuidList = GeneralUtils.loadUuidsFromFile(filePath);
//        for (String uuid : uuidList) {
//            System.out.println(uuid);
//        }

        //fedoraUtils.applyToAllUuid(new ChildrenUuid(uuid, DigitalObjectModel.PAGE));

        //krameriusUtils.setPublic(uuid);

//        File jpgFile = new File("konverze-test/2106500001.jpg");
//        File jp2File = new File("konverze-test/testJp2Output.jp2");
//
//        InputStream convertedStream = null;
//        try {
//            convertedStream = FormatConvertor.convertJpgToJp2(jpgFile);
//            FileUtils.copyInputStreamToFile(convertedStream, jp2File);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                convertedStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

//        krameriusUtils.export("uuid:01adabfe-aed2-4ca9-b856-d2bc47109309");

//        List<String> uuidList = fedoraUtils.getChildrenUuids("uuid:bf62f530-170f-11e5-ac88-005056827e51", DigitalObjectModel.PAGE);
        List<Item> uuidList = krameriusApi.getChildren("uuid:bf62f530-170f-11e5-ac88-005056827e51");
//        uuidList.add("uuid:af666007-51f5-49c3-931a-e8bb61338614");
//        uuidList.add("uuid:38db2dca-738d-448a-9921-9f02ffbc9067");
//        uuidList.add("uuid:87a664a7-25bc-4f4a-ad4f-51bef05a0703");
//        uuidList.add("uuid:ea65c355-6558-48ed-945c-a43949c3305f");
//        uuidList.add("uuid:2ba3016d-ff81-4c1b-9842-7d1e80fe9676");
//        uuidList.add("uuid:6797e7df-68ce-407b-8084-f42085fe1fd1");
//        uuidList.add("uuid:d7e028f9-2f1e-4894-bb2f-5b492c1e0bd5");
//        uuidList.add("uuid:f34a12b2-621b-4136-870c-c4ca2d5b3eac");
//        uuidList.add("uuid:56775c82-435f-11dd-b505-00145e5790ea");
//        uuidList.add("uuid:56775c85-435f-11dd-b505-00145e5790ea");
//        uuidList.add("uuid:56778398-435f-11dd-b505-00145e5790ea");
//        uuidList.add("uuid:56778399-435f-11dd-b505-00145e5790ea");


        System.out.println("Found " + uuidList.size() + " pages");

        for (int i = 0; i < uuidList.size(); i++) {
            String uuid = uuidList.get(i).getPid();
            try {
                InputStream rawInputStream = fedoraUtils.getImgFull(uuid, "image/jp2");
                FileUtils.copyInputStreamToFile(rawInputStream, new File("bf62f530-170f-11e5-ac88-005056827e51/" + String.format("%03d", i+1) + ".jp2"));
//                System.out.println(fedoraUtils.getImgLocation(uuid));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public String getUsage() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
