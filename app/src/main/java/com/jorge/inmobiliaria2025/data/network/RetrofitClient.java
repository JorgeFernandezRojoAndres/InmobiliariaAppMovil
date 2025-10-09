package com.jorge.inmobiliaria2025.data.network;

import android.content.Context;
import android.util.Log;

import com.jorge.inmobiliaria2025.data.SessionManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
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
 *  - Compatibilidad con multipart (subida de imágenes)
 *  - Base URL adaptable (emulador / dispositivo físico)
 *  - Endpoint ajustado a /api/PropietariosApi/*
 */
public class RetrofitClient {

    private static volatile Retrofit retrofit = null; // 🧱 thread-safe singleton

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) { // 🔒 evita doble inicialización
                if (retrofit == null) {
                    SessionManager session = new SessionManager(context);

                    // 🛰️ Interceptor de logging
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                            message -> Log.d("Retrofit", "🛰️ " + message)
                    );
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                    // 🔐 Interceptor de autenticación JWT
                    Interceptor authInterceptor = chain -> {
                        Request original = chain.request();
                        String token = session.obtenerToken();
                        Request.Builder builder = original.newBuilder();

                        // ⚙️ Evitar sobrescribir content-type cuando es multipart
                        if (original.header("Content-Type") == null) {
                            builder.header("Content-Type", "application/json");
                        }

                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                            Log.d("Retrofit", "🔑 Token agregado a la petición");
                        } else {
                            Log.w("Retrofit", "⚠️ No hay token guardado en SessionManager");
                        }

                        try {
                            Response response = chain.proceed(builder.build());
                            if (response.code() == 401) {
                                Log.w("Retrofit", "🚫 Token inválido o expirado (401 Unauthorized)");
                            }
                            return response;
                        } catch (IOException e) {
                            Log.e("Retrofit", "❌ Error al procesar la petición: " + e.getMessage());
                            throw e;
                        }
                    };

                    // ⚙️ Configuración del cliente HTTP
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(authInterceptor)
                            .addInterceptor(loggingInterceptor)
                            .connectTimeout(40, TimeUnit.SECONDS)
                            .readTimeout(40, TimeUnit.SECONDS)
                            .writeTimeout(40, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();

                    // 🌐 Base URL dinámica
                    String BASE_URL;
                    if (isRunningOnEmulator()) {
                        BASE_URL = "http://10.0.2.2:5027/api/"; // 💻 Emulador
                        Log.i("Retrofit", "🧩 Emulador → usando 10.0.2.2");
                    } else {
                        BASE_URL = "http://192.168.1.33:5027/api/"; // 📱 Dispositivo físico
                        Log.i("Retrofit", "📶 Dispositivo → usando IP local");
                    }

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    Log.i("Retrofit", "✅ Retrofit inicializado con URL base: " + BASE_URL);
                }
            }
        }
        return retrofit;
    }

    // 🔎 Detecta si se ejecuta en un emulador Android
    private static boolean isRunningOnEmulator() {
        return android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86");
    }
}
