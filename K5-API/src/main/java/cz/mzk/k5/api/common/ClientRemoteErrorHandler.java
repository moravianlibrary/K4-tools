package cz.mzk.k5.api.common;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by holmanj on 16.6.15.
 */
public class ClientRemoteErrorHandler implements ErrorHandler {
    @Override
    public Throwable handleError(RetrofitError cause) {
        Response r = cause.getResponse();

//        if (r != null && r.getStatus() == 413) {
//            // vyhazuje tomcat při malém limitu na velikost souboru, tělo je html (další parsování by házelo retrofit chyby)
//            // případně to dělá proxy nebo tak něco po cestě..
//            return new EntityTooLargeException("Malý limit v tomcatu (defaultně 2 MB)");
//        }
//
//        String message = "";
//        try {
//            // dělá problémy při chybě během získávání OCR (typy QueuedImage a String)
//            QueuedImage response = (QueuedImage) cause.getBodyAs(QueuedImage.class);
//            message = response.getMessage();
//        } catch (ClassCastException e) {
//            JSONObject response2 = (JSONObject) cause.getBodyAs(JSONObject.class);
//            try {
//                message = response2.getString("message");
//            } catch (JSONException e1) {
////                e1.printStackTrace();
//            }
////            e.printStackTrace();
//        }
//        if (r != null && r.getStatus() == 404) {
//            return new ItemNotFoundException(message);
//        } else if (r != null && r.getStatus() == 409) {
//            return new ConflictException(message);
//        } else if (r != null && r.getStatus() == 400) {
//            return new BadRequestException(message);
        /** } else **/
        if (r != null && (r.getStatus() == 500 || r.getStatus() == 404)) {
            return new InternalServerErroException(r.getStatus() + " Item not found in K5.");
        }
        return cause;
    }
}
