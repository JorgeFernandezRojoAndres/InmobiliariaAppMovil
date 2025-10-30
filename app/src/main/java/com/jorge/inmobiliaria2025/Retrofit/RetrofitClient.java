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

/**
 * ✅ RetrofitClient (versión mejorada)
 * Totalmente compatible con tu API .NET + JWT + Android real device
 */
public class RetrofitClient {

    private static volatile Retrofit retrofit = null;

    // ⚠️ Verifica que esta IP sea la de tu PC (ipconfig en consola)
    //    No uses localhost ni 10.0.2.2 en teléfono real
    public static final String BASE_URL = "http://192.168.1.37:5027/";

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {

                    SessionManager session = SessionManager.getInstance(context);


                    // 🛰️ Interceptor de logs HTTP (solo desarrollo)
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                        if (message.startsWith("-->") || message.startsWith("<--"))
                            Log.d("Retrofit", "🌐 " + message);
                        else if (message.contains("{") || message.contains("["))
                            Log.v("RetrofitBody", message);
                    });
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                    // 🔐 Interceptor de autenticación JWT
                    Interceptor authInterceptor = chain -> {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder();

                        String token = session.obtenerToken();
                        MediaType mediaType = original.body() != null ? original.body().contentType() : null;
                        String contentType = mediaType != null ? mediaType.toString() : "";

                        if (contentType.contains("multipart")) {
                            builder.removeHeader("Content-Type");
                        } else if (!contentType.contains("json")) {
                            builder.header("Content-Type", "application/json");
                        }

                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                            Log.d("RetrofitAuth", "🪶 Token enviado en header");
                        } else {
                            Log.w("RetrofitAuth", "⚠️ No hay token almacenado");
                        }

                        Request request = builder.build();

                        try {
                            Response response = chain.proceed(request);
                            if (response.code() == 401)
                                Log.w("RetrofitAuth", "🚫 Token inválido o expirado (401)");
                            else if (response.code() >= 400)
                                Log.e("RetrofitAuth", "⚠️ Error HTTP " + response.code());
                            return response;
                        } catch (IOException e) {
                            Log.e("Retrofit", "❌ Error al procesar la petición: " + e.getMessage());
                            throw e;
                        }
                    };

                    // ⚙️ Cliente HTTP con tiempos aumentados y retry
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(authInterceptor)
                            .addInterceptor(loggingInterceptor)
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();

                    // 🚀 Construcción de Retrofit
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
}
