package com.jorge.inmobiliaria2025.Retrofit;

import android.content.Context;
import android.util.Log;

import com.jorge.inmobiliaria2025.localdata.SessionManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // ✅ Debe estar arriba para que reset() la pueda limpiar
    private static volatile Retrofit retrofit = null;

    public static final String BASE_URL = "http://192.168.1.37:5027/";

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {

                    SessionManager session = SessionManager.getInstance(context);

                    // 🛰 Logs HTTP
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                        if (message.startsWith("-->") || message.startsWith("<--"))
                            Log.d("Retrofit", "🌐 " + message);
                        else if (message.contains("{") || message.contains("["))
                            Log.v("RetrofitBody", message);
                    });
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                    // 🔐 Interceptor JWT
                    Interceptor authInterceptor = chain -> {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder();

                        String token = session.obtenerToken();
                        MediaType mediaType = original.body() != null ? original.body().contentType() : null;
                        String contentType = mediaType != null ? mediaType.toString() : "";

                        // ✅ Content-Type
                        if (contentType.contains("multipart")) {
                            builder.removeHeader("Content-Type");
                        } else {
                            builder.header("Content-Type", "application/json");
                        }

                        // ✅ Solo mandar token si es válido
                        if (token != null && token.trim().length() > 10) {
                            builder.header("Authorization", "Bearer " + token);
                            Log.d("RetrofitAuth", "✅ Token enviado: " + token);
                        } else {
                            Log.w("RetrofitAuth", "⚠️ Token vacío o inválido, NO se envía");
                        }

                        Request request = builder.build();

                        try {
                            Response response = chain.proceed(request);
                            if (response.code() == 401) {
                                Log.w("RetrofitAuth", "🚫 Token inválido/expirado");
                            }
                            return response;

                        } catch (IOException e) {
                            Log.e("RetrofitAuth", "❌ Error petición: " + e.getMessage());
                            throw e;
                        }
                    };

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(authInterceptor)
                            .addInterceptor(loggingInterceptor)
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    Log.i("RetrofitInit", "✅ Retrofit inicializado con base URL: " + BASE_URL);
                }
            }
        }
        return retrofit;
    }

    // ✅ Forzar reconstrucción cuando cambia token
    public static void reset() {
        retrofit = null;
        Log.d("RetrofitClient", "🔄 Retrofit reiniciado para usar nuevo token");
    }
}
