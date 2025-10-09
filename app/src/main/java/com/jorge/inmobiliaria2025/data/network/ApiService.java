package com.jorge.inmobiliaria2025.data.network;

import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.model.Inquilino;
import com.jorge.inmobiliaria2025.model.Contrato;
import com.jorge.inmobiliaria2025.model.Pago;
import com.jorge.inmobiliaria2025.model.LoginRequest;
import com.jorge.inmobiliaria2025.model.TokenResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * ✅ ApiService
 * Interfaz central para consumir el backend .NET 8/9 de Inmobiliaria_2025.
 *
 * Contiene endpoints para:
 * 🔐 Autenticación JWT
 * 🏠 Inmuebles
 * 👤 Inquilinos
 * 🧾 Contratos
 * 💰 Pagos
 * 🖼️ Subida de avatar del propietario
 */
public interface ApiService {

    // ==========================================================
    // 🔐 ---- AUTENTICACIÓN ----
    // ==========================================================
    // Login del propietario (ahora mapea al controlador PropietariosApiController)
    @POST("PropietariosApi/login")
    Call<TokenResponse> login(@Body LoginRequest request);


    // ==========================================================
    // 🖼️ ---- PERFIL / AVATAR ----
    // ==========================================================
    // Sube una imagen de perfil (avatar) del propietario autenticado (JWT requerido)
    @Multipart
    @POST("PropietariosApi/subirAvatar")
    Call<ResponseBody> subirAvatar(@Part MultipartBody.Part archivo);


    // ==========================================================
    // 🏠 ---- INMUEBLES ----
    // ==========================================================
    // Devuelve la lista de inmuebles alquilados del propietario autenticado
    @GET("Inmuebles/alquilados")
    Call<List<Inmueble>> getInmueblesAlquilados();


    // ==========================================================
    // 👤 ---- INQUILINOS ----
    // ==========================================================
    // Obtiene el inquilino asociado a un inmueble específico
    @GET("Inquilinos/{idInmueble}")
    Call<Inquilino> getInquilinoPorInmueble(@Path("idInmueble") int idInmueble);


    // ==========================================================
    // 🧾 ---- CONTRATOS ----
    // ==========================================================
    // Devuelve la lista de contratos vigentes del propietario autenticado
    @GET("Contratos/vigentes")
    Call<List<Contrato>> getContratosVigentes();


    // ==========================================================
    // 💰 ---- PAGOS ----
    // ==========================================================
    // Devuelve todos los pagos registrados para un contrato específico
    @GET("Pagos/{idContrato}")
    Call<List<Pago>> getPagosPorContrato(@Path("idContrato") int idContrato);
}
