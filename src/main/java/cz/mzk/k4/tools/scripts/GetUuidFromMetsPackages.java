package cz.mzk.k4.tools.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;

public class GetUuidFromMetsPackages implements Script {
    private static final Logger LOGGER = LogManager.getLogger(GetUuidFromMetsPackages.class);
    private ArrayList<String> uuidList = new ArrayList<String>();
    private AccessProvider accessProvider = AccessProvider.getInstance();
//    private KrameriusUtils krameriusUtils = new KrameriusUtils(accessProvider);
    private ProcessRemoteApi krameriusApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(
                                                accessProvider.getKrameriusHost(),
                                                accessProvider.getKrameriusUser(),
                                                accessProvider.getKrameriusPassword());

    public GetUuidFromMetsPackages() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {
        String path = args.get(0);
        Iterator<File> iterator =  FileUtils.iterateFiles(new File(path), new RegexFileFilter("^METS\\S*.xml$"), TrueFileFilter.INSTANCE);

        while(iterator.hasNext()) {
            File file = iterator.next();
            uuidList.add(getUuidFromXml(file));
            String uuid = getUuidFromXml(file);
            try {
                krameriusApi.deleteObject(uuid);
            } catch (K5ApiException e) {
                LOGGER.error(e.getMessage());
                LOGGER.error("Selhalo plánování procesu delete u objektu " + uuid);
            }
        }

        System.out.println(uuidList.toString());
        System.out.println(uuidList.size());
    }

    protected String getUuidFromXml(File file){
        SAXReader reader = new SAXReader();
        String uuid = null;
        String type = "";
        try{
            Document document = reader.read(file);
            type = document.selectSingleNode( "//*[local-name()='issuance' and namespace-uri()='http://www.loc.gov/mods/v3']" ).getText();

            if(type.equals("continuing")){
                uuid = document.selectSingleNode( "//*[local-name()='mods' and namespace-uri()='http://www.loc.gov/mods/v3'][@ID='MODS_ISSUE_0001']/*[local-name()='identifier' and namespace-uri()='http://www.loc.gov/mods/v3'][@type='uuid']" ).getText();
            }else if(type.equals("monographic")){
                uuid = document.selectSingleNode( "//*[local-name()='mods' and namespace-uri()='http://www.loc.gov/mods/v3'][@ID='MODS_VOLUME_0001']/*[local-name()='identifier' and namespace-uri()='http://www.loc.gov/mods/v3'][@type='uuid']" ).getText();
            }
        }catch(DocumentException e){
            e.printStackTrace();
        }
        return uuid;
    }

    @Override
    public String getUsage() {
        // TODO Auto-generated method stub
        return null;
    }

}
