package cz.mzk.k5.api.client;

import cz.mzk.k5.api.client.domain.Item;
import cz.mzk.k5.api.client.domain.Streams;
import cz.mzk.k5.api.common.K5ApiException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import retrofit.RetrofitError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by holmanj on 8.2.15.
 * Posílá dotaz dál podle očekávaného typu dat (různá deserializase jsonu, stringu a xml)
 */
public class ClientRemoteApi {
    private final String DC = "DC";
    private final String BIBLIO_MODS = "BIBLIO_MODS";
    private final String TEI_P5 = "TEI_P5";
    private final String TEXT_OCR = "TEXT_OCR";
    private final String TEXT_OCR_AMD = "TEXT_OCR_AMD";
    private final String ALTO = "ALTO";
    private final String IMG_FULL = "IMG_FULL";
    private final String IMG_FULL_AMD = "IMG_FULL_AMD";
    private final String IMG_PREVIEW = "IMG_PREVIEW";
    private final String IMG_THUMB = "IMG_THUMB";
    private final String WAV = "WAV";
    private final String OGG = "OGG";
    private final String MP3 = "MP3";

    private ClientRemoteApiJSON apiJSON;
    private ClientRemoteApiString apiString;
    private ClientRemoteApiXML apiXML;
    private ClientRemoteApiRaw apiRaw;

    // získávat API přes factory:
    // ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    public ClientRemoteApi(ClientRemoteApiJSON apiJSON, ClientRemoteApiString apiString, ClientRemoteApiXML apiXML, ClientRemoteApiRaw apiRaw) {
        this.apiJSON = apiJSON;
        this.apiString = apiString;
        this.apiXML = apiXML;
        this.apiRaw = apiRaw;
    }

    public Item getItem(String pid) throws K5ApiException {
        return apiJSON.getItem(pid);
    }

    public List<Item> getChildren(String pid) throws K5ApiException {
        try {
            return apiJSON.getChildren(pid);
        } catch (RetrofitError error) {
            System.out.println(error.getUrl());
            throw error;
        }
    }

    public Document getFoxml(String pid) throws K5ApiException {
        return apiXML.getFoxml(pid);
    }

    public Document solrSearch(Map<String, String> queryMap) throws K5ApiException {
        // solr chce query parametry ve tvaru key:value místo standardního key=value
        String query = "?q=";
        for(Map.Entry<String, String> entry : queryMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            query += key + ":" + value;
        }
        return apiXML.solrSearch(query);
    }

    // TODO: solr simple search (vrátí seznam uuid)
    public List<String> solrSimpleSearch(Map<String, String> queryMap) throws K5ApiException {
        // solr chce query parametry ve tvaru key:value místo standardního key=value
        String query = "?q=";
        for(Map.Entry<String, String> entry : queryMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            query += key + ":" + value;
        }
        query += "&fl=PID";
        try {
            return parseUuids(apiXML.solrSearch(query));
        // TODO: log
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<String> parseUuids(Document result) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
//        final String pXML = "<root><x>1</x><x>2</x><x>3</x><x>4</x></root>";
//        final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(pXML.getBytes()));
        // //str[@name='PID']/text()
        final XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile("//str[@name='PID']/text()");
        final NodeList nodeList = (NodeList) xPathExpression.evaluate(result, XPathConstants.NODESET);
        final List<String> values = new LinkedList<>();
        for (int i = 0; i < nodeList.getLength(); ++i) {
            values.add(nodeList.item(i).getNodeValue());
        }
        return values;
    }

    public Streams listStreams(String pid) throws K5ApiException {
        return apiJSON.listStreams(pid);
    }

    public String getOCR(String pid) throws K5ApiException {
        return apiString.getStream(pid, TEXT_OCR);
    }

    public Document getAlto(String pid) throws K5ApiException {
        return apiXML.getStream(pid, ALTO);
    }

    public Document getDublinCore(String pid) throws K5ApiException {
        return apiXML.getStream(pid, DC);
    }

    public Document getMods(String pid) throws K5ApiException {
        return apiXML.getStream(pid, BIBLIO_MODS);
    }

    public Document getTei(String pid) throws K5ApiException {
        return apiXML.getStream(pid, TEI_P5);
    }

    public Document getImgFullAmd(String pid) throws K5ApiException {
        return apiXML.getStream(pid, IMG_FULL_AMD);
    }

    public Document getOcrAmd(String pid) throws K5ApiException {
        return apiXML.getStream(pid, TEXT_OCR_AMD);
    }

    public InputStream getImgFull(String pid) throws K5ApiException {
        return apiRaw.getStream(pid, IMG_FULL);
    }

    public InputStream getImgPreview(String pid) throws K5ApiException {
        return apiRaw.getStream(pid, IMG_PREVIEW);
    }

    public InputStream getImgThumb(String pid) throws K5ApiException {
        return apiRaw.getStream(pid, IMG_THUMB);
    }

    public InputStream getRecordingWav(String pid) throws K5ApiException {
        return apiRaw.getStream(pid, WAV);
    }

    public InputStream getRecordingOgg(String pid) throws K5ApiException {
        return apiRaw.getStream(pid, OGG);
    }

    public InputStream getRecordingMp3(String pid) throws K5ApiException {
        return apiRaw.getStream(pid, MP3);
    }
}
