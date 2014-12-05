package cz.mzk.k4.tools.utils;

import com.google.gson.JsonSyntaxException;
import cz.mzk.k4.tools.domain.KrameriusProcess;
import cz.mzk.k4.tools.domain.ProcessLog;
import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import scala.collection.mutable.HashEntry;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Jan Holman
 * 
 */
public class K4ProcessManager {

	private AccessProvider accessProvider;
    private JsonParser parser;

	private static org.apache.log4j.Logger LOGGER = Logger.getLogger(K4ProcessManager.class);

	public K4ProcessManager(AccessProvider accessProvider) {
        this.accessProvider = accessProvider;
		this.parser = new JsonParser();
	}

	/**
	 * Returns list of process descriptions
	 * 
	 * @param queryParams - map of parameters
	 * @return List of Process objects
	 */
	public List<KrameriusProcess> searchByParams(Map<String,String> queryParams)
			throws NullArgumentException {

		if (queryParams == null) {
			throw new NullArgumentException("queryParams");
		}

		// get JSON string
		String query = "/search/api/v4.6/processes";
		WebTarget resource = accessProvider.getKrameriusRESTWebResource(query);
        WebTarget webJson = resource;
        for (Map.Entry<String, String> param : queryParams.entrySet()) {
            resource.queryParam(param.getKey(), param.getValue());
        }
		String strJson = resource.request(MediaType.APPLICATION_JSON).get(String.class);

		// parse JSON string
		List<KrameriusProcess> list = null;
		try {
			list = parser.parseProcessList(strJson);
		} catch (JsonSyntaxException jse) {
			LOGGER.fatal("Incorrect Json string " + strJson);
		}
		return list;
	}

	/**
	 * Returns process description
	 * 
	 * @param uuid Process indentifier
	 * @return Process object
	 */
	public KrameriusProcess getProcessByUuid(String uuid) {
		String strJson = "";

		// get JSON string
//		try {
			WebTarget resource = accessProvider.getKrameriusRESTWebResource("/search/api/v4.6/processes/" + uuid);
			strJson = resource.request(MediaType.APPLICATION_JSON).get(String.class);
//		} catch (UniformInterfaceException e) {
//			int status = e.getResponse().getStatus();
//			if (status == 404) {
//				LOGGER.fatal("Process not found");
//			}
//			throw new IllegalStateException(e);
//		}

		// parse JSON string
		KrameriusProcess process = null;
		try {
			process = parser.parseProcess(strJson);
		} catch (JsonSyntaxException jse) {
			LOGGER.fatal("Incorrect Json string " + strJson);
		}
		return process;
	}

	/**
	 * Returns process log
	 * 
	 * @param uuid Process indentifier
	 * @return ProcessLog object (sout, serr)
	 */
	public ProcessLog getLog(String uuid) throws IllegalStateException {
		String strJson = "";

		// get JSON string
//		try {
			String query = "/search/api/v4.6/processes/" + uuid + "/logs";
            WebTarget resource = accessProvider.getKrameriusRESTWebResource(query);
			strJson = resource.request(MediaType.APPLICATION_JSON).get(String.class);
//		} catch (UniformInterfaceException e) {
//			int status = e.getResponse().getStatus();
//			if (status == 404) {
//				// LOGGER.fatal("Process not found");
//			}
//			throw new IllegalStateException(e);
//		}

		// parse JSON string
		ProcessLog log = null;
		try {
			log = parser.parseLog(strJson);
		} catch (JsonSyntaxException jse) {
			LOGGER.fatal("Incorrect Json string " + strJson);
		}

		return log;
	}
}
