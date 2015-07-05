package cz.mzk.k4.tools.ocr.OcrApi;

import cz.mzk.k4.tools.ocr.domain.QueuedImage;
import cz.mzk.k4.tools.ocr.exceptions.BadRequestException;
import cz.mzk.k4.tools.ocr.exceptions.ConflictException;
import cz.mzk.k4.tools.ocr.exceptions.InternalServerErroException;
import cz.mzk.k4.tools.ocr.exceptions.ItemNotFoundException;
import retrofit.mime.TypedFile;

/**
 * Created by holmanj on 8.2.15.
 * Posílá dotaz dál podle očekávaného typu dat (různá deserializase jsonu, stringu a xml)
 */
public class AbbyRestApi {

    private AbbyRestApiJson apiJSON;
    private AbbyRestApiString apiString;

    // získávat API přes factory:
    // ClientRemoteApi k5Api = AbbyRestApiFactory.getAbbyRestApi();
    public AbbyRestApi(AbbyRestApiJson apiJSON, AbbyRestApiString apiString) {
        this.apiJSON = apiJSON;
        this.apiString = apiString;
    }

    public String getOCR(String type, String md5) throws BadRequestException, InternalServerErroException, ItemNotFoundException {
        return apiString.getOcr(type, md5);
    }

    public QueuedImage sendImageJpeg(TypedFile image) throws InternalServerErroException, ConflictException {
        return apiJSON.sendImageJpeg(image);
    }

    public QueuedImage sendImageJp2(TypedFile image) throws InternalServerErroException, ConflictException {
        return apiJSON.sendImageJp2(image);
    }

    public QueuedImage pollOcrState(String md5) throws BadRequestException, ItemNotFoundException, InternalServerErroException {
        return apiJSON.pollOcrState(md5);
    }

    public QueuedImage deleteItem(String md5) throws InternalServerErroException, ItemNotFoundException {
        return apiJSON.deleteItem(md5);
    }
}
