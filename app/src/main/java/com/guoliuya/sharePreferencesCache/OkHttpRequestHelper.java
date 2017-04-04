package com.guoliuya.sharePreferencesCache;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpRequestHelper{
    private static final int MY_SOCKET_TIMEOUT_MS = 4000;

    private static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");
    private static final String CACHE_FILE = FileUtil.CACHE_DOWNLOAD_DIR + File.separator + "cache.tmp";

    private Handler mHandler;
    private OkHttpClient mClient;
    private SharedPreferences mPref;
    private HttpCallBack mCallBack;
    private String info = "";

    public OkHttpRequestHelper(Context context,HttpCallBack callBack) {
        mPref = context.getApplicationContext().getSharedPreferences("http_cache",Context.MODE_PRIVATE);
        mHandler = new Handler(context.getMainLooper());
        mCallBack = callBack;

        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(new File(CACHE_FILE), cacheSize);
        Interceptor mNetworkInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request requestOrigin = chain.request();
                Headers headersOrigin = requestOrigin.headers();
                Headers headers = headersOrigin.newBuilder()
                        .set("Charset", "utf-8").build();
                Request request = requestOrigin.newBuilder().headers(headers).build();
                return chain.proceed(request);
            }
        };
        mClient = new OkHttpClient().newBuilder()
                .connectTimeout(MY_SOCKET_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(MY_SOCKET_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .writeTimeout(MY_SOCKET_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .addNetworkInterceptor(mNetworkInterceptor)
                .cache(cache)
                .build();
    }


    public void getAsString(String url) {

        Request.Builder requestBuilder = new Request.Builder().url(url);
        requestBuilder.get();

        mClient.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCallBack.onFailure(mPref.getString("main_page_cache",""));

                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) {
                try {
                    info = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (response.isSuccessful()) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //使用SharedPreferences保存数据
                            SharedPreferences.Editor edit = mPref.edit();
                            edit.putString("main_page_cache",info);
                            edit.apply();
                            //缓存已经做好，接下来在无网的时候使用gson和picasso来展示出来
                            mCallBack.onSuccess(info);

                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //onError
                        }
                    });
                }

            }
        });
    }

    public interface HttpCallBack{
        void onSuccess(String info);
        void onFailure(String info);
    }
}
