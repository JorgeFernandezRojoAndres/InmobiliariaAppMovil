package com.jorge.inmobiliaria2025.data.network;

import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.model.Inquilino;
import com.jorge.inmobiliaria2025.model.Contrato;
import com.jorge.inmobiliaria2025.model.Pago;
import com.jorge.inmobiliaria2025.model.LoginRequest;
import com.jorge.inmobiliaria2025.model.TokenResponse;
import com.jorge.inmobiliaria2025.model.Propietario;
import com.jorge.inmobiliaria2025.model.CambioClaveDto;
import com.jorge.inmobiliaria2025.model.TipoInmueble; // ✅ nuevo modelo

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

/**
 * ✅ ApiService
 * Define todos los endpoints REST utilizados por la app móvil.
 * Incluye: autenticación, perfil, inmuebles, tipos, contratos, inquilinos y pagos.
 */
public interface ApiService {

    // -------------------- 🔐 AUTENTICACIÓN --------------------
    @POST("api/Propietarios/login")
    Call<TokenResponse> login(@Body LoginRequest request);

    // -------------------- 👤 PERFIL / AVATAR --------------------
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

    // -------------------- 🏠 INMUEBLES --------------------
    @GET("api/Inmuebles/misInmuebles")
    Call<List<Inmueble>> getMisInmuebles(@Header("Authorization") String token);

    @GET("api/Inmuebles/alquilados")
    Call<List<Inmueble>> getInmueblesAlquilados(@Header("Authorization") String token);

    @PUT("api/Inmuebles/{id}")
    Call<ResponseBody> actualizarDisponibilidad(
            @Header("Authorization") String token,
            @Path("id") int idInmueble,
            @Body Inmueble inmueble
    );

    @Multipart
    @PUT("api/Inmuebles/{id}")
    Call<ResponseBody> actualizarInmueble(
            @Header("Authorization") String token,
            @Path("id") int idInmueble,
            @Part("Direccion") RequestBody direccion,
            @Part("TipoId") RequestBody tipoId,
            @Part("MetrosCuadrados") RequestBody metrosCuadrados,
            @Part("Precio") RequestBody precio,
            @Part("Activo") RequestBody activo,
            @Part MultipartBody.Part imagenFile
    );

    // -------------------- 🏗️ TIPOS DE INMUEBLE --------------------
    @GET("api/TiposInmuebleApi") // ✅ ruta real del controlador backend
    Call<List<TipoInmueble>> getTiposInmueble(@Header("Authorization") String token);


    // -------------------- 👥 INQUILINOS --------------------
    @GET("api/Inquilinos/{idInmueble}")
    Call<Inquilino> getInquilinoPorInmueble(
            @Header("Authorization") String token,
            @Path("idInmueble") int idInmueble
    );

    // -------------------- 📄 CONTRATOS --------------------
    @GET("api/Contratos/vigentes")
    Call<List<Contrato>> getContratosVigentes(@Header("Authorization") String token);

    // -------------------- 💰 PAGOS --------------------
    @GET("api/Pagos/{idContrato}")
    Call<List<Pago>> getPagosPorContrato(
            @Header("Authorization") String token,
            @Path("idContrato") int idContrato
    );
}
