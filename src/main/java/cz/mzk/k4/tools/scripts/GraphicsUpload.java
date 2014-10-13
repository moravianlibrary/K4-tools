package cz.mzk.k4.tools.scripts;



import cz.mzk.k4.tools.utils.Script;


import java.util.List;

/**
 * Created by rumanekm on 9.6.14.
 */
public class GraphicsUpload implements Script {


    @Override
    public void run(List<String> args) {
        String sysno = args.get(0);
        String metadataPrefix = "http://oai.mzk.cz/provider?verb=GetRecord&identifier=oai:aleph.mzk.cz:MZK03-"+ sysno +"&metadataPrefix=";

        //OAIPMHClient client = new OAIPMHClientImpl(getConfigarion());
        //List<MetadataBundle> metadataBundles = client.search(metadataPrefix, "MZK03");

        //System.out.println(metadataBundles);
    }

//    public EditorConfiguration getConfigarion() {
//        Map<String, Object> parameters = new HashMap<String, Object>();
//        parameters.put("editor.home", System.getProperty("user.home") + File.separatorChar + "k4_tools");
//        Configuration configuration = new MapConfiguration(parameters);
//
//        EditorConfigurationImpl editorConfiguration = new EditorConfigurationImpl();
//        editorConfiguration.setConfiguration(configuration);
//
//        return editorConfiguration;
//    }



    @Override
    public String getUsage() {
        return null;
    }
}
