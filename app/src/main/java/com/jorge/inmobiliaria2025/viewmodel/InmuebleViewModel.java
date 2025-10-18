package com.jorge.inmobiliaria2025.viewmodel;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.jorge.inmobiliaria2025.data.InmobiliariaDatabase;
import com.jorge.inmobiliaria2025.data.InmuebleDao;
import com.jorge.inmobiliaria2025.data.InmuebleRepository;
import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.model.TipoInmueble;

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

    private final MutableLiveData<String> mensajeToast = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navegarAtras = new MutableLiveData<>();

    private final MutableLiveData<List<TipoInmueble>> tiposInmueble = new MutableLiveData<>();

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

    public void procesarGuardado(String direccion, String precioTexto, boolean disponible) {
        if (direccion == null || direccion.isEmpty() || precioTexto == null || precioTexto.isEmpty()) {
            mensajeToast.postValue("⚠️ Complete todos los campos");
            return;
        }

        try {
            double precio = Double.parseDouble(precioTexto);
            Inmueble nuevo = new Inmueble(direccion.trim(), precio, disponible);

            executor.execute(() -> {
                inmuebleDao.insertar(nuevo);
                List<Inmueble> actual = listaLiveData.getValue();
                if (actual == null) actual = new ArrayList<>();
                actual.add(nuevo);
                listaLiveData.postValue(actual);
            });

            mensajeToast.postValue("✅ Inmueble guardado correctamente");
            navegarAtras.postValue(true);
        } catch (NumberFormatException e) {
            mensajeToast.postValue("❌ Precio inválido");
        }
    }

    public enum EstadoGuardado { EXITO, CAMPOS_VACIOS, PRECIO_INVALIDO }

    public LiveData<EstadoGuardado> getEstadoGuardado() { return estadoGuardado; }
    public LiveData<List<Inmueble>> getInmuebles() { return listaLiveData; }
    public LiveData<List<Inmueble>> getInmueblesRemotos() { return listaInmueblesRemotos; }
    public LiveData<Inmueble> getInmuebleSeleccionado() { return inmuebleSeleccionado; }
    public LiveData<String> getMensajeToast() { return mensajeToast; }
    public LiveData<Boolean> getNavegarAtras() { return navegarAtras; }
    public LiveData<List<TipoInmueble>> getTiposInmueble() { return tiposInmueble; }

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
    // 🔹 DETALLE / EDICIÓN
    // ==========================
    public void cargarDesdeBundle(Bundle args) {
        if (args == null) return;
        Inmueble inmueble = (Inmueble) args.getSerializable("inmueble");
        if (inmueble != null) {
            inmuebleSeleccionado.postValue(inmueble);
            Log.d("InmuebleVM", "📦 Inmueble cargado desde Bundle: " + inmueble.getDireccion());
        } else {
            Log.w("InmuebleVM", "⚠️ Bundle sin inmueble válido");
        }
    }

    // ==========================
    // 🔹 DISPONIBILIDAD
    // ==========================
    public void actualizarDisponibilidad(Inmueble inmueble) {
        if (inmueble == null) return;

        LiveData<Boolean> resultado = repo.actualizarDisponibilidad(inmueble);
        resultado.observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean exito) {
                resultado.removeObserver(this);
                if (Boolean.TRUE.equals(exito)) {
                    mensajeToast.postValue("✅ Estado actualizado correctamente");
                    List<Inmueble> actual = listaLiveData.getValue();
                    if (actual != null) {
                        for (Inmueble i : actual) {
                            if (i.getId() == inmueble.getId()) {
                                i.setDisponible(inmueble.isDisponible());
                                break;
                            }
                        }
                        listaLiveData.postValue(actual);
                    }
                } else {
                    mensajeToast.postValue("⚠️ No se pudo actualizar disponibilidad");
                }
            }
        });
    }

    // ==========================
    // 🔹 ACTUALIZACIÓN COMPLETA (form-data)
    // ==========================
    public void actualizarInmueble(Inmueble inmueble, Uri imagenUri) {
        if (inmueble == null) {
            mensajeToast.postValue("⚠️ Inmueble nulo, no se puede actualizar");
            return;
        }

        LiveData<Boolean> resultado = repo.actualizarInmuebleConImagenForm(inmueble, imagenUri);
        resultado.observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean exito) {
                resultado.removeObserver(this);
                if (Boolean.TRUE.equals(exito)) {
                    mensajeToast.postValue("✅ Inmueble actualizado correctamente");
                    cargarInmueblesDesdeApi();
                } else {
                    mensajeToast.postValue("⚠️ Error al actualizar el inmueble");
                }
            }
        });
    }

    // ==========================
    // 🆕 SUBIR IMAGEN INDIVIDUAL
    // ==========================
    public void subirImagenInmueble(int idInmueble, Uri imagenUri) {
        if (imagenUri == null) {
            mensajeToast.postValue("⚠️ Seleccione una imagen antes de guardar");
            return;
        }

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
    }

    // ==========================
    // 🔹 UTILIDADES VISUALES
    // ==========================
    public void mostrarToast(Context context, String mensaje) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
    }

    public String getTextoDisponibilidad(Inmueble inmueble) {
        return inmueble.isDisponible() ? "Disponible" : "No disponible";
    }

    public int getColorDisponibilidad(Context context, Inmueble inmueble) {
        int color = inmueble.isDisponible()
                ? android.R.color.holo_green_dark
                : android.R.color.holo_red_dark;
        return ContextCompat.getColor(context, color);
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
