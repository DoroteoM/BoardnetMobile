package doroteo.boardnetmobile;

import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.*;

public class HttpUtils {
    //private static final String BASE_URL = "http://api.twitt0.er.com/1/";
    private static final String BASE_URL = "http://10.0.2.2:8000/api/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler)
    {
        client.get(getAbsoluteUrl(url), params, responseHandler);
        String test = "test";
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler)
    {
        client.post(getAbsoluteUrl(url), params, responseHandler);
        Log.d("asd", "---------------- this is response : " + client.post(getAbsoluteUrl(url), params, responseHandler));
    }

    public static void getByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler)
    {
        client.get(url, params, responseHandler);
    }

    public static void postByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler)
    {
        client.post(url, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl)
    {
        return BASE_URL + relativeUrl;
    }
}