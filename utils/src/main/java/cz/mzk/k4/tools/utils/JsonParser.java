//package cz.mzk.k4.tools.utils;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonSyntaxException;
//import com.google.gson.reflect.TypeToken;
//import cz.mzk.k4.tools.utils.domain.KrameriusProcess;
//import cz.mzk.k4.tools.utils.domain.ProcessLog;
//import org.apache.logging.log4j.Logger;
//
//import javax.xml.bind.DatatypeConverter;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// *
// * @author Jan Holman
// *
// */
//public class JsonParser {
//
//	Gson gson;
//	private static org.apache.log4j.Logger LOGGER = Logger
//			.getLogger(JsonParser.class);
//
//	public JsonParser() {
//		gson = new Gson();
//	}
//
//	public KrameriusProcess parseProcess(String strJson)
//			throws JsonSyntaxException {
//		return gson.fromJson(strJson, KrameriusProcess.class);
//	}
//
//	public List<KrameriusProcess> parseProcessList(String strJson)
//			throws JsonSyntaxException {
//
//		List<KrameriusProcess> processes = gson.fromJson(strJson,
//				new TypeToken<ArrayList<KrameriusProcess>>() {
//				}.getType());
//
//		return processes;
//	}
//
//	public ProcessLog parseLog(String strLog) throws JsonSyntaxException {
//
//		ProcessLog log = gson.fromJson(strLog, ProcessLog.class);
//
//		// sout and serr in Json are Base64 encoded
//		byte[] decodedSout = DatatypeConverter.parseBase64Binary(log.getSout());
//		byte[] decodedSerr = DatatypeConverter.parseBase64Binary(log.getSerr());
//
//		log.setSout(new String(decodedSout));
//		log.setSerr(new String(decodedSerr));
//
//		return log;
//	}
//
//}