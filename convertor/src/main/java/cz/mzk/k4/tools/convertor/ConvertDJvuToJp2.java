//package cz.mzk.k4.tools.convertor;
//
//import cz.mzk.k4.tools.utils.AccessProvider;
//import cz.mzk.k4.tools.utils.Script;
//import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
//import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
//import cz.mzk.k4.tools.workers.ConvertDjvuWorker;
//import cz.mzk.k4.tools.workers.UuidWorker;
//
//import java.util.List;
//
///**
// * Created by hradskam on 26.3.14.
// */
//public class ConvertDJvuToJp2 implements Script {
//    private static FedoraUtils fedoraUtils = new FedoraUtils(new AccessProvider());
//
//    /**
//     * Base method for converting files from djvu to jpeg 2000 format
//     *
//     * @param args There should be one argument, uuid of page/volume.. that needs to be converted
//     */
//    @Override
//    public void run(List<String> args) {
//        UuidWorker convertDjvuToJp2 = new ConvertDjvuWorker(true);
//        if (args.isEmpty()) {
//            System.out.println("Chybí zadat uuid dokumentu ke zpracování jako argument příkazu.");
//            return;
//        }
//        String uuid = args.get(0);
//
//        //Check if the given uuid belongs to volume and if it does, get all its pages and run conversion for them
//        List<String> fedoraList = fedoraUtils.getChildrenUuids(uuid, DigitalObjectModel.PAGE);
//        for (String id : fedoraList) {
//            convertDjvuToJp2.run(id);
//        }
//        System.out.println("KONEC");
//    }
//
//    @Override
//    public String getUsage() {
//        return "Konverze zadaného uuid z formátu djvu do jp2";
//    }
//}
