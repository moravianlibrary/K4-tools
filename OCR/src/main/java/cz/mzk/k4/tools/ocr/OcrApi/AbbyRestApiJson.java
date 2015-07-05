package cz.mzk.k4.tools.ocr.OcrApi;

import cz.mzk.k4.tools.ocr.domain.QueuedImage;
import cz.mzk.k4.tools.ocr.exceptions.BadRequestException;
import cz.mzk.k4.tools.ocr.exceptions.ConflictException;
import cz.mzk.k4.tools.ocr.exceptions.InternalServerErroException;
import cz.mzk.k4.tools.ocr.exceptions.ItemNotFoundException;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.mime.TypedFile;

/**
 * Created by holmanj on 16.6.15.
 */
public interface AbbyRestApiJson {

    /**
     * pošle obrázek OCR serveru, zpátky dostane identifikátor (md5)
     * @param image (mimetype image/jpeg)
     * @return JSON: id = md5, state = processing/done/error/deleted, message
     */
    @Headers("Content-Type: image/jpeg")
    @POST("/in")
    public QueuedImage sendImageJpeg(@Body TypedFile image) throws InternalServerErroException, ConflictException;

    /**
     * pošle obrázek OCR serveru, zpátky dostane identifikátor (md5)
     * @param image (mimetype image/jp2)
     * @return JSON: id = md5, state = processing/done/error/deleted, message
     */
    @Headers("Content-Type: image/jp2")
    @POST("/in")
    public QueuedImage sendImageJp2(@Body TypedFile image) throws InternalServerErroException, ConflictException;

    /**
     * ptá se na stav procesu
     * @param md5 - id procesu získané při poslání obrázku
     * @return JSON: id = stejné jako v parametru, state = processing/done/error/deleted, message
     */
    @GET("/state/{id}")
    public QueuedImage pollOcrState(@Path("id") String md5) throws BadRequestException, ItemNotFoundException, InternalServerErroException;

    /**
     * smaže proces nezávisle na stavu (maže ze vstupní, výstupní, i error složky)
     * @param md5 - id procesu získané při poslání obrázku
     * @return JSON: id = stejné jako v parametru, state = processing/done/error/deleted, message
     */
    @DELETE("/delete/{id}")
    public QueuedImage deleteItem(@Path("id") String md5) throws InternalServerErroException, ItemNotFoundException;

}
