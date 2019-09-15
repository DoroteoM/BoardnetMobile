package doroteo.boardnetmobile;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.VolleyError;

public class ErrorResponse {
    public static void errorResponse(VolleyError e, Context context) {
        if (e.networkResponse.statusCode == 401) {
            Toast.makeText(context, "Error 401: The request has not been applied because it lacks valid authentication credentials for the target resource.", Toast.LENGTH_LONG).show();
        } else if (e.networkResponse.statusCode == 403) {
            Toast.makeText(context, "Error 403: The server understood the request but refuses to authorize it.", Toast.LENGTH_LONG).show();
        } else if (e.networkResponse.statusCode == 404) {
            Toast.makeText(context, "Error 404: Requested resource not found.", Toast.LENGTH_LONG).show();
        } else if (e.networkResponse.statusCode == 500) {
            Toast.makeText(context, "Error 500: Something went wrong on our servers.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Error" + e.networkResponse.statusCode + ": " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
