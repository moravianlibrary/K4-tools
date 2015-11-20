package cz.mzk.k5.api.client;

import cz.mzk.k5.api.client.domain.Item;
import cz.mzk.k5.api.client.domain.Streams;
import cz.mzk.k5.api.common.K5ApiException;
import org.w3c.dom.Document;
import retrofit.RetrofitError;

import java.io.InputStream;
import java.util.List;

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
