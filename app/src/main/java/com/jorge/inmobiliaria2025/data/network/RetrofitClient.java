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
 *  - Base URL fija a la IP del backend local
 */
public class RetrofitClient {

    private static volatile Retrofit retrofit = null; // 🧱 thread-safe singleton

    // 🛠️ FIX: quitar /api/ del final para evitar el doble "api/api"
    private static final String BASE_URL = "http://192.168.1.34:5027/"; // 📡 IP fija de la PC

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

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    Log.i("Retrofit", "✅ Retrofit inicializado con URL base fija: " + BASE_URL);
                }
            }
        }
        return retrofit;
    }
}
