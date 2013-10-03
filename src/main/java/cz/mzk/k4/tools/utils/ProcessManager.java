package cz.mzk.k4.tools.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import com.google.gson.JsonSyntaxException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import cz.mzk.k4.tools.domain.KrameriusProcess;
import cz.mzk.k4.tools.domain.ProcessLog;

/**
 * 
 * @author Jan Holman
 * 
 */
public class ProcessManager {

	private BasicAuthenticationFilter credentials;
	private String host;
	private JsonParser parser;
	private Client client;

	private static org.apache.log4j.Logger LOGGER = Logger.getLogger(ProcessManager.class);

	public ProcessManager(String host, String username, String password) {
		this.credentials = new BasicAuthenticationFilter(username, password);
		this.client = Client.create();
		this.parser = new JsonParser();
		this.host = host;
	}

	/**
	 * Returns list of process descriptions
	 * 
	 * @param map of parameters
	 * @return List of Process objects
	 */
	public List<KrameriusProcess> searchByParams(Map<String, String> params)
			throws NullArgumentException {

		if (params == null) {
			throw new NullArgumentException("params");
		}

		// get JSON string
		String url = "http://" + host + "/search/api/v4.6/processes";
		url += "?";
		Iterator<String> iterator = params.keySet().iterator();
		String key = "";
		while (iterator.hasNext()) {
			key = iterator.next();
			url += key + "=" + params.get(key) + "&";
		}
		WebResource resource = client.resource(url);
		resource.addFilter(credentials);
		String strJson = resource.accept(MediaType.APPLICATION_JSON).get(
				String.class);

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
		try {
			WebResource resourse = client.resource("http://" + host
					+ "/search/api/v4.6/processes/" + uuid);
			resourse.addFilter(credentials);
			strJson = resourse.accept(MediaType.APPLICATION_JSON).get(
					String.class);
		} catch (UniformInterfaceException e) {
			int status = e.getResponse().getStatus();
			if (status == 404) {
				LOGGER.fatal("Process not found");
			}
			throw new IllegalStateException(e);
		}

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
		try {
			String url = "http://" + host + "/search/api/v4.6/processes/"
					+ uuid + "/logs";
			WebResource resource = client.resource(url);
			resource.addFilter(credentials);
			strJson = resource.accept(MediaType.APPLICATION_JSON).get(
					String.class);
		} catch (UniformInterfaceException e) {
			int status = e.getResponse().getStatus();
			if (status == 404) {
				// LOGGER.fatal("Process not found");
			}
			throw new IllegalStateException(e);
		}

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
