package cz.mzk.k4.tools.ocr.OcrApi;

import cz.mzk.k4.tools.ocr.exceptions.BadRequestException;
import cz.mzk.k4.tools.ocr.exceptions.InternalServerErroException;
import cz.mzk.k4.tools.ocr.exceptions.ItemNotFoundException;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by holmanj on 29.6.15.
 */
public interface AbbyRestApiString {

    /**
     * získá hotové OCR
     * @param type - "alto" nebo "txt"
     * @param md5 - id procesu získané při poslání obrázku
     * @return FileInputStream s mimetype application/xml (type = alto) nebo text/plain (type = txt)
     */
    @GET("/product/{type}/{id}")
    public String getOcr(@Path("type") String type, @Path("id") String md5) throws BadRequestException, ItemNotFoundException, InternalServerErroException;

}
