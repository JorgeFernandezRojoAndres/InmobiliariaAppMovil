package com.jorge.inmobiliaria2025.ui.Inmueble;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.jorge.inmobiliaria2025.localdata.InmobiliariaDatabase;
import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.model.TipoInmueble;
import com.jorge.inmobiliaria2025.ui.nav.NavViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InmuebleViewModel extends AndroidViewModel {

    private final InmuebleDao inmuebleDao;
    private final LiveData<List<Inmueble>> listaInmueblesRoom;
    private final MutableLiveData<List<Inmueble>> listaLiveData = new MutableLiveData<>();
    private final MutableLiveData<Inmueble> inmuebleSeleccionado = new MutableLiveData<>();
    private final MutableLiveData<EstadoGuardado> estadoGuardado = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final InmuebleRepository repo;
    private final MutableLiveData<List<Inmueble>> listaInmueblesRemotos = new MutableLiveData<>();
    private final MutableLiveData<Bundle> accionNavegarDetalle = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeToast = new MutableLiveData<>();


    private final MutableLiveData<List<TipoInmueble>> tiposInmueble = new MutableLiveData<>();
    private final MutableLiveData<Void> accionLimpiarCampos = new MutableLiveData<>();
    private final MutableLiveData<Void> accionNavegarAtras = new MutableLiveData<>();
    public LiveData<Void> getAccionLimpiarCampos() { return accionLimpiarCampos; }
    public LiveData<Void> getAccionNavegarAtras() { return accionNavegarAtras; }

    public InmuebleViewModel(@NonNull Application application) {
        super(application);

        // ✅ Inicializar correctamente la base local
        InmobiliariaDatabase db = InmobiliariaDatabase.getInstance(application.getApplicationContext());
        inmuebleDao = db.inmuebleDao();

        listaInmueblesRoom = inmuebleDao.obtenerTodos();
        repo = new InmuebleRepository(application.getApplicationContext());

        // ✅ Sincronización inicial
        cargarInmueblesDesdeApi();
        cargarTiposInmueble();
    }

    public void setInmuebleSeleccionado(Inmueble inmueble) {
        if (inmueble != null) {
            Log.d("InmuebleVM", "🏠 Inmueble seleccionado: " + inmueble.getDireccion());
            inmuebleSeleccionado.postValue(inmueble);
        } else {
            Log.w("InmuebleVM", "⚠️ Se intentó seleccionar un inmueble nulo");
        }
    }



    // ====================================================
// 🔹 Procesar selección de imagen desde el Fragment (sin if en el Fragment)
// ====================================================
    private final MutableLiveData<Uri> imagenUriSeleccionadaLiveData = new MutableLiveData<>();
    public LiveData<Uri> getImagenUriSeleccionada() { return imagenUriSeleccionadaLiveData; }

    public void procesarSeleccionImagen(ActivityResult result, ImageView ivPreview) {
        if (result == null) return;

        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Uri uri = result.getData().getData();
            if (uri != null) {
                ivPreview.setImageURI(uri);
                imagenUriSeleccionadaLiveData.postValue(uri); // 🔹 Guarda la URI seleccionada
                Log.d("InmuebleVM", "📸 Imagen seleccionada: " + uri);
            }
        } else {
            mensajeToast.postValue("⚠️ No se seleccionó ninguna imagen");
        }
    }

    // 🧭 ViewModel global para manejar navegación
    private NavViewModel navVM;

    public void setNavViewModel(NavViewModel navVM) {
        this.navVM = navVM;
    }

    // 🏠 Click en un inmueble desde la lista
    public void onInmuebleClick(Inmueble inmueble) {
        if (inmueble == null) return;

        // ✅ Guarda el inmueble seleccionado (mantiene tu lógica actual)
        setInmuebleSeleccionado(inmueble);

        // ✅ Prepara los datos para enviar al detalle
        Bundle bundle = new Bundle();
        bundle.putSerializable("inmueble", inmueble);

        // 🚀 Enviar evento de navegación al NavViewModel compartido
        try {
            if (navVM != null) {
                navVM.navegarADetalle(bundle);
                Log.i("InmuebleVM", "➡️ Evento enviado al NavViewModel para navegar al detalle");
            } else {
                Log.w("InmuebleVM", "⚠️ navVM no inicializado; no se puede navegar");
            }
        } catch (Exception e) {
            Log.w("InmuebleVM", "⚠️ Error al intentar usar NavViewModel: " + e.getMessage());
        }

        // 🔸 Emite igual tu LiveData local (por compatibilidad con otros observers)
        accionNavegarDetalle.postValue(bundle);

        Log.d("InmuebleVM", "➡️ Navegando al detalle: " + inmueble.getDireccion());
    }

    // 🔄 Cambiar disponibilidad (disponible / no disponible)
    public void onCambiarDisponibilidad(Inmueble inmueble) {
        if (inmueble == null) return;

        // Invoca al repo para actualizar disponibilidad
        LiveData<Boolean> resultado = repo.actualizarDisponibilidad(inmueble);

        resultado.observeForever(exito -> {
            if (Boolean.TRUE.equals(exito)) {
                mensajeToast.postValue("✅ Estado actualizado correctamente");
                cargarInmueblesDesdeApi();
            } else {
                mensajeToast.postValue("⚠️ No se pudo actualizar la disponibilidad");
            }
        });
    }

    public enum EstadoGuardado { EXITO, CAMPOS_VACIOS, PRECIO_INVALIDO }


    public LiveData<List<Inmueble>> getInmuebles() { return listaLiveData; }


    public LiveData<String> getMensajeToast() { return mensajeToast; }

    // ==========================
    // 🔹 CARGA DE TIPOS DE INMUEBLE
    // ==========================
    public void cargarTiposInmueble() {
        LiveData<List<TipoInmueble>> respuesta = repo.obtenerTiposInmueble();
        respuesta.observeForever(new Observer<List<TipoInmueble>>() {
            @Override
            public void onChanged(List<TipoInmueble> tipos) {
                respuesta.removeObserver(this);
                if (tipos != null && !tipos.isEmpty()) {
                    tiposInmueble.postValue(tipos);
                    Log.i("InmuebleVM", "✅ Tipos de inmueble cargados desde API: " + tipos.size());
                } else {
                    tiposInmueble.postValue(new ArrayList<>());
                    Log.w("InmuebleVM", "⚠️ Lista de tipos vacía o error de conexión");
                }
            }
        });
    }

    // ==========================
    // 🔹 CARGA DE INMUEBLES (API + Room fallback)
    // ==========================
    public void cargarInmueblesDesdeApi() {
        LiveData<List<Inmueble>> respuestaApi = repo.obtenerMisInmuebles();
        respuestaApi.observeForever(new Observer<List<Inmueble>>() {
            @Override
            public void onChanged(List<Inmueble> lista) {
                respuestaApi.removeObserver(this);

                if (lista != null && !lista.isEmpty()) {
                    listaInmueblesRemotos.postValue(lista);
                    listaLiveData.postValue(lista);
                    Log.i("InmuebleVM", "✅ Inmuebles cargados desde API: " + lista.size());
                } else {
                    Log.w("InmuebleVM", "⚠️ API vacía o sin respuesta, usando Room...");
                    cargarInmueblesDesdeRoom();
                }
            }
        });
    }

    private void cargarInmueblesDesdeRoom() {
        executor.execute(() -> {
            List<Inmueble> listaDB = listaInmueblesRoom.getValue();
            if (listaDB != null && !listaDB.isEmpty()) {
                listaLiveData.postValue(listaDB);
                Log.d("InmuebleVM", "💾 Cargados desde Room: " + listaDB.size());
            } else {
                listaLiveData.postValue(new ArrayList<>());
                Log.w("InmuebleVM", "⚠️ No hay inmuebles locales ni remotos.");
            }
        });
    }

    // ==========================
// 🆕 SUBIR IMAGEN INDIVIDUAL (corregido para ejecutar en hilo principal)
// ==========================
    public void subirImagenInmueble(int idInmueble, Uri imagenUri) {
        if (imagenUri == null) {
            mensajeToast.postValue("⚠️ Seleccione una imagen antes de guardar");
            return;
        }

        // 👇 Asegura que observeForever se ejecute en el hilo principal
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            LiveData<Boolean> resultado = repo.subirImagenInmueble(idInmueble, imagenUri);
            resultado.observeForever(new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean exito) {
                    resultado.removeObserver(this);
                    if (Boolean.TRUE.equals(exito)) {
                        mensajeToast.postValue("✅ Imagen subida correctamente");
                        cargarInmueblesDesdeApi();
                    } else {
                        mensajeToast.postValue("⚠️ Error al subir la imagen del inmueble");
                    }
                }
            });
        });
    }

    // ✅ Versión extendida con validación de Metros²
    public void guardarInmueble(String direccion, String precioTexto, String metrosTexto,
                                boolean disponible, Uri imagenUri) {

        // 🔹 Validaciones mínimas (controladas desde el ViewModel)
        if (direccion == null || direccion.trim().isEmpty()) {
            mensajeToast.postValue("⚠️ La dirección es obligatoria");
            return;
        }
        if (precioTexto == null || precioTexto.trim().isEmpty()) {
            mensajeToast.postValue("⚠️ El precio es obligatorio");
            return;
        }
        if (metrosTexto == null || metrosTexto.trim().isEmpty()) {
            mensajeToast.postValue("⚠️ Los metros cuadrados son obligatorios");
            return;
        }

        double precio;
        int metros;

        try {
            precio = Double.parseDouble(precioTexto);
        } catch (NumberFormatException e) {
            mensajeToast.postValue("❌ Precio inválido");
            return;
        }

        try {
            metros = Integer.parseInt(metrosTexto);
        } catch (NumberFormatException e) {
            mensajeToast.postValue("❌ Metros inválidos");
            return;
        }

        // 🔹 Crear el objeto con todos los datos
        Inmueble nuevo = new Inmueble(direccion.trim(), precio, disponible);
        nuevo.setMetrosCuadrados(metros); // 🆕 agregar metros al modelo
        nuevo.setTipoId(1);

        LiveData<Inmueble> creado = repo.crearInmueble(nuevo);

        Observer<Inmueble> observer = new Observer<Inmueble>() {
            @Override
            public void onChanged(Inmueble inmuebleCreado) {
                creado.removeObserver(this);

                if (inmuebleCreado == null) {
                    mensajeToast.postValue("⚠️ Error al crear el inmueble en el servidor");
                    return;
                }

                mensajeToast.postValue("✅ Inmueble creado correctamente");

                // 📤 Subir imagen si corresponde
                if (imagenUri != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                            subirImagenInmueble(inmuebleCreado.getId(), imagenUri)
                    );
                }

                // 🔄 Refrescar lista y emitir eventos
                cargarInmueblesDesdeApi();
                estadoGuardado.postValue(EstadoGuardado.EXITO);
                accionLimpiarCampos.postValue(null);
                accionNavegarAtras.postValue(null);
            }
        };

        creado.observeForever(observer);
    }

    // ==========================
    // 🔹 UTILIDADES VISUALES
    // ==========================
    public void mostrarToast(Context context, String mensaje) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
    }

    public LiveData<List<Inmueble>> getListaFiltrada() {
        MutableLiveData<List<Inmueble>> filtrada = new MutableLiveData<>();
        getListaLiveData().observeForever(lista -> {
            if (lista == null || lista.isEmpty()) {
                filtrada.postValue(new ArrayList<>());
            } else {
                filtrada.postValue(lista);
            }
        });
        return filtrada;
    }

    public LiveData<List<Inmueble>> getListaLiveData() {
        return getInmuebles();
    }
    public void cargarInmuebles() {
        cargarInmueblesDesdeApi();
    }
}