package cz.mzk.k4.tools.ocr.OcrApi;

import cz.mzk.k4.tools.ocr.domain.QueuedImage;
import cz.mzk.k4.tools.ocr.exceptions.BadRequestException;
import cz.mzk.k4.tools.ocr.exceptions.ConflictException;
import cz.mzk.k4.tools.ocr.exceptions.InternalServerErroException;
import cz.mzk.k4.tools.ocr.exceptions.ItemNotFoundException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by holmanj on 16.6.15.
 */
public class AbbyRestErrorHandler implements ErrorHandler {
    @Override
    public Throwable handleError(RetrofitError cause) {
        Response r = cause.getResponse();
        String message = "";
        try {
            // dělá problémy při chybě během získávání OCR (typy QueuedImage a String)
            QueuedImage response = (QueuedImage) cause.getBodyAs(QueuedImage.class);
            message = response.getMessage();
        } catch (ClassCastException e) {
            JSONObject response2 = (JSONObject) cause.getBodyAs(JSONObject.class);
            try {
                message = response2.getString("message");
            } catch (JSONException e1) {
//                e1.printStackTrace();
            }
//            e.printStackTrace();
        }
        if (r != null && r.getStatus() == 404) {
            return new ItemNotFoundException(message);
        } else if (r != null && r.getStatus() == 409) {
            return new ConflictException(message);
        } else if (r != null && r.getStatus() == 400) {
            return new BadRequestException(message);
        } else if (r != null && r.getStatus() == 500) {
            return new InternalServerErroException(message);
        }
        return cause;
    }
}
