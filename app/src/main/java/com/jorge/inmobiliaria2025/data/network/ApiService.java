package com.jorge.inmobiliaria2025.data.network;

import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.model.Inquilino;
import com.jorge.inmobiliaria2025.model.Contrato;
import com.jorge.inmobiliaria2025.model.Pago;
import com.jorge.inmobiliaria2025.model.LoginRequest;
import com.jorge.inmobiliaria2025.model.TokenResponse;
import com.jorge.inmobiliaria2025.model.Propietario;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    // -------------------- AUTENTICACIÓN --------------------
    @POST("api/Propietarios/login")
    Call<TokenResponse> login(@Body LoginRequest request);


    // -------------------- PERFIL / AVATAR --------------------

    // ✅ Sube avatar con header Authorization
    @Multipart
    @POST("api/Propietarios/subirAvatar")
    Call<ResponseBody> subirAvatar(
            @Header("Authorization") String token,
            @Part MultipartBody.Part archivo
    );

    // ✅ Obtiene perfil del propietario autenticado
    @GET("api/Propietarios/perfil")
    Call<Propietario> obtenerPerfil(@Header("Authorization") String token);

    // ✅ Actualiza datos del propietario (nombre, teléfono, etc.)
    @PUT("api/Propietarios/perfil")
    Call<Propietario> actualizarPerfil(
            @Header("Authorization") String token,
            @Body Propietario propietario
    );


    // -------------------- INMUEBLES --------------------
    @GET("api/Inmuebles/alquilados")
    Call<List<Inmueble>> getInmueblesAlquilados();


    // -------------------- INQUILINOS --------------------
    @GET("api/Inquilinos/{idInmueble}")
    Call<Inquilino> getInquilinoPorInmueble(@Path("idInmueble") int idInmueble);


    // -------------------- CONTRATOS --------------------
    @GET("api/Contratos/vigentes")
    Call<List<Contrato>> getContratosVigentes();


    // -------------------- PAGOS --------------------
    @GET("api/Pagos/{idContrato}")
    Call<List<Pago>> getPagosPorContrato(@Path("idContrato") int idContrato);
}
