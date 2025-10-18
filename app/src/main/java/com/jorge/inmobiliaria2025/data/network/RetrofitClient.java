package com.jorge.inmobiliaria2025.data.network;

import android.content.Context;
import android.util.Log;

import com.jorge.inmobiliaria2025.data.SessionManager;

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
 * ✅ RetrofitClient
 * Configura la comunicación entre la app Android y el backend .NET 8/9.
 * Incluye:
 *  - Autenticación JWT (header Authorization)
 *  - Logging detallado en Logcat
 *  - Soporte multipart (subida y actualización de imágenes)
 *  - Base URL sin /api al final
 */
public class RetrofitClient {

    private static volatile Retrofit retrofit = null; // 🧱 Singleton seguro
    public static final String BASE_URL = "http://192.168.1.34:5027/"; // 📡 Cambiar según tu red local

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {

                    SessionManager session = new SessionManager(context);

                    // 🛰️ Interceptor de logging (detallado solo para JSON)
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                        if (message.startsWith("-->") || message.startsWith("<--")) {
                            Log.d("Retrofit", "🌐 " + message);
                        } else if (message.contains("{") || message.contains("[")) {
                            Log.v("RetrofitBody", message);
                        }
                    });
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                    // 🔐 Interceptor de autenticación y control de Content-Type
                    Interceptor authInterceptor = chain -> {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder();

                        String token = session.obtenerToken();
                        MediaType mediaType = original.body() != null ? original.body().contentType() : null;
                        String contentType = mediaType != null ? mediaType.toString() : "";

                        // ✅ Evitar interferir en multipart
                        if (contentType.contains("multipart")) {
                            builder.removeHeader("Content-Type");
                        } else if (!contentType.contains("json")) {
                            builder.header("Content-Type", "application/json");
                        }

                        // ✅ Agregar token si existe
                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                        }

                        Request request = builder.build();

                        try {
                            Response response = chain.proceed(request);
                            int code = response.code();

                            if (code == 401) {
                                Log.w("RetrofitAuth", "🚫 Token inválido o expirado (401 Unauthorized)");
                            } else if (code >= 400) {
                                Log.e("RetrofitAuth", "⚠️ Error HTTP " + code + ": " + response.message());
                            }
                            return response;

                        } catch (IOException e) {
                            Log.e("Retrofit", "❌ Error al procesar la petición: " + e.getMessage());
                            throw e;
                        }
                    };

                    // ⚙️ Cliente HTTP configurado
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(authInterceptor)
                            .addInterceptor(loggingInterceptor)
                            .connectTimeout(45, TimeUnit.SECONDS)
                            .readTimeout(45, TimeUnit.SECONDS)
                            .writeTimeout(45, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();

                    // 🧩 Retrofit con soporte JSON y multipart
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    Log.i("RetrofitInit", "✅ Retrofit inicializado correctamente con base URL: " + BASE_URL);
                }
            }
        }
        return retrofit;
    }
}
