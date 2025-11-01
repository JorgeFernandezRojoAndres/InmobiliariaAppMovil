package com.jorge.inmobiliaria2025.Retrofit;

import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.model.Contrato;
import com.jorge.inmobiliaria2025.model.Pago;
import com.jorge.inmobiliaria2025.model.LoginRequest;
import com.jorge.inmobiliaria2025.model.TokenResponse;
import com.jorge.inmobiliaria2025.model.Propietario;
import com.jorge.inmobiliaria2025.model.CambioClaveDto;
import com.jorge.inmobiliaria2025.model.TipoInmueble;
import com.jorge.inmobiliaria2025.model.InquilinoConInmueble;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * ✅ ApiService (versión simplificada)
 * Usa el interceptor de RetrofitClient para agregar el token JWT automáticamente.
 * Define todos los endpoints REST utilizados por la app móvil.
 */
public interface ApiService {

    // -------------------- 🔐 AUTENTICACIÓN --------------------
    @POST("api/Propietarios/login")
    Call<TokenResponse> login(@Body LoginRequest request);

    // -------------------- 👤 PERFIL / AVATAR --------------------
    @Multipart
    @POST("api/Propietarios/subirAvatar")
    Call<ResponseBody> subirAvatar(
            @Part MultipartBody.Part archivo
    );

    @GET("api/Propietarios/perfil")
    Call<Propietario> obtenerPerfil();

    @PUT("api/Propietarios/perfil")
    Call<ResponseBody> actualizarPerfil(@Body Propietario propietario);

    @PUT("api/Propietarios/cambiar-clave")
    Call<ResponseBody> cambiarClave(@Body CambioClaveDto dto);

    // -------------------- 🏠 INMUEBLES --------------------
    @GET("api/Inmuebles/misInmuebles")
    Call<List<Inmueble>> getMisInmuebles();

    @GET("api/Inmuebles/alquilados")
    Call<List<Inmueble>> getInmueblesAlquilados();

    @PUT("api/Inmuebles/{id}/disponibilidad")
    Call<ResponseBody> actualizarDisponibilidad(
            @Path("id") int idInmueble,
            @Body Inmueble inmueble
    );

    @Multipart
    @PUT("api/Inmuebles/{id}/form")
    Call<ResponseBody> actualizarInmuebleConImagen(
            @Path("id") int idInmueble,
            @Part("Id") RequestBody id,
            @Part("Direccion") RequestBody direccion,
            @Part("TipoId") RequestBody tipoId,
            @Part("MetrosCuadrados") RequestBody metros,
            @Part("Precio") RequestBody precio,
            @Part("Activo") RequestBody activo,
            @Part MultipartBody.Part imagen
    );

    @Multipart
    @POST("api/Inmuebles/upload")
    Call<ResponseBody> subirImagenInmueble(
            @Part("idInmueble") RequestBody idInmueble,
            @Part MultipartBody.Part imagen
    );

    // -------------------- 🏗️ TIPOS DE INMUEBLE --------------------
    @GET("api/TiposInmuebleApi")
    Call<List<TipoInmueble>> getTiposInmueble();

    // -------------------- 📄 CONTRATOS --------------------
    @GET("api/ContratosApi/vigentes")
    Call<List<Contrato>> getContratosVigentes();

    // -------------------- 💰 PAGOS --------------------
    @GET("api/ContratosApi/{id}/pagos")
    Call<List<Pago>> getPagosPorContrato(@Path("id") int idContrato);

    @GET("api/Pagos/propietarioActual")
    Call<List<Pago>> getPagosGlobales();

    // -------------------- ✏️ ACTUALIZAR / CREAR INMUEBLE --------------------
    @PUT("api/Inmuebles/{id}")
    Call<ResponseBody> actualizarInmueble(
            @Path("id") int idInmueble,
            @Body Inmueble inmueble
    );

    @POST("api/Inmuebles")
    Call<Inmueble> crearInmueble(@Body Inmueble nuevo);

    // -------------------- 👥 INQUILINOS --------------------
    @GET("api/InquilinosApi/con-inmueble")
    Call<List<InquilinoConInmueble>> getInquilinosConInmueble();

    @GET("api/InquilinosApi/{idInquilino}")
    Call<InquilinoConInmueble> getInquilinoById(@Path("idInquilino") int idInquilino);

    // -------------------- ⚖️ RESCISIÓN DE CONTRATO --------------------
    @POST("api/ContratosApi/rescindir/{id}")
    Call<ResponseBody> rescindirContrato(@Path("id") int idContrato);
}
