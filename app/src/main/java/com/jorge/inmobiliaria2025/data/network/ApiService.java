package com.jorge.inmobiliaria2025.data.network;

import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.model.Inquilino;
import com.jorge.inmobiliaria2025.model.Contrato;
import com.jorge.inmobiliaria2025.model.Pago;
import com.jorge.inmobiliaria2025.model.LoginRequest;
import com.jorge.inmobiliaria2025.model.TokenResponse;
import com.jorge.inmobiliaria2025.model.Propietario;
import com.jorge.inmobiliaria2025.model.CambioClaveDto;

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

    @Multipart
    @POST("api/Propietarios/subirAvatar")
    Call<ResponseBody> subirAvatar(
            @Header("Authorization") String token,
            @Part MultipartBody.Part archivo
    );

    @GET("api/Propietarios/perfil")
    Call<Propietario> obtenerPerfil(@Header("Authorization") String token);

    @PUT("api/Propietarios/perfil")
    Call<ResponseBody> actualizarPerfil(
            @Header("Authorization") String token,
            @Body Propietario propietario
    );

    @PUT("api/Propietarios/cambiar-clave")
    Call<ResponseBody> cambiarClave(
            @Header("Authorization") String token,
            @Body CambioClaveDto dto
    );


    // -------------------- INMUEBLES --------------------
    // 🔹 Nuevo: obtiene los inmuebles del propietario autenticado (token)
    @GET("api/Inmuebles/misInmuebles")
    Call<List<Inmueble>> getMisInmuebles(@Header("Authorization") String token);

    // 🔹 Viejo: aún disponible si lo usás en otra parte (alquilados)
    @GET("api/Inmuebles/alquilados")
    Call<List<Inmueble>> getInmueblesAlquilados();

    // 🔹 Nuevo: cambia disponibilidad (Activo) del inmueble
    @PUT("api/Inmuebles/{id}")
    Call<ResponseBody> actualizarDisponibilidad(
            @Header("Authorization") String token,
            @Path("id") int idInmueble,
            @Body Inmueble inmueble
    );


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
