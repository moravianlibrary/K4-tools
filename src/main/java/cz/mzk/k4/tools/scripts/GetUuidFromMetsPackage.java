package cz.mzk.k4.tools.scripts;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import cz.mzk.k4.tools.utils.Script;

public class GetUuidFromMetsPackage implements Script {

	
	@Override
	public void run(List<String> args) {
		String path = args.get(0);
        //Iterator<File> iterator =  FileUtils.iterateFiles(new File(path), new SuffixFileFilter(".xml"), TrueFileFilter.INSTANCE);
		Iterator<File> iterator =  FileUtils.iterateFiles(new File(path), new RegexFileFilter("^METS\\S*.xml$"), TrueFileFilter.INSTANCE);
        
        while(iterator.hasNext()) {
            File file = iterator.next();
            getUuidFromXml(file);
        }
		
	}
	
	protected String getUuidFromXml(File file){
		SAXReader reader = new SAXReader();
		try{
			Document document = reader.read(file);
			
			List listNodes = document.selectNodes( "//*[local-name()='identifier' and namespace-uri()='http://www.loc.gov/mods/v3'][@type='uuid']" );
			System.out.println(listNodes.get(0));
			
		}catch(DocumentException e){
			e.printStackTrace();
		}
		return "t";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return null;
	}

}
