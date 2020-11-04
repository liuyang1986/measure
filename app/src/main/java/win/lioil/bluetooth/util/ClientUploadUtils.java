package win.lioil.bluetooth.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import win.lioil.bluetooth.config.Config;

public class ClientUploadUtils {

    public static ResponseBody upload(String url, String filePath, String fileName,final Map<String,String> params) throws Exception {
        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder builder = new MultipartBody.Builder();
        if (params!=null) {
            Set<String> keys = params.keySet();
            for (String key : keys) {
                builder.addFormDataPart(key, params.get(key));
            }
        }

        RequestBody requestBody = builder
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,
                        RequestBody.create(MediaType.parse("multipart/form-data"),
                                new File(filePath)))
                .build();


        okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
        headersbuilder.add("token", Config.AUTH_TOKEN);
        Headers headers = headersbuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .headers(headers)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful())
        {
            throw new IOException("Unexpected code " + response);
        }

        return response.body();
    }
}
