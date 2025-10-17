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

    // 🧱 LiveData para tipos de inmueble
    private final MutableLiveData<List<TipoInmueble>> tiposInmueble = new MutableLiveData<>();

    public InmuebleViewModel(@NonNull Application application) {
        super(application);
        InmobiliariaDatabase db = InmobiliariaDatabase.getDatabase(application);
        inmuebleDao = db.inmuebleDao();
        listaInmueblesRoom = inmuebleDao.obtenerTodos();
        repo = new InmuebleRepository(application.getApplicationContext());
        cargarInmueblesDesdeApi();
        cargarTiposInmueble(); // 🔹 carga automática al iniciar
    }

    // ✅ Método agregado correctamente
    public void setInmuebleSeleccionado(Inmueble inmueble) {
        if (inmueble != null) {
            Log.d("InmuebleVM", "🏠 Inmueble seleccionado: " + inmueble.getDireccion());
            inmuebleSeleccionado.postValue(inmueble);
        } else {
            Log.w("InmuebleVM", "⚠️ Se intentó seleccionar un inmueble nulo");
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
    // 🔹 CARGA DE INMUEBLES
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
    // 🔹 CRUD LOCAL
    // ==========================
    public void insertar(Inmueble inmueble) {
        executor.execute(() -> {
            inmuebleDao.insertar(inmueble);
            actualizarListaLocal(inmueble);
        });
    }

    public void actualizar(Inmueble inmueble) {
        executor.execute(() -> {
            inmuebleDao.actualizar(inmueble);
            cargarInmueblesDesdeRoom();
        });
    }

    public void eliminarInmueble(Inmueble inmueble) {
        executor.execute(() -> {
            inmuebleDao.eliminar(inmueble);
            cargarInmueblesDesdeRoom();
        });
    }

    // ==========================
    // 🔹 DETALLE / EDICIÓN
    // ==========================
    public void cargarDesdeBundle(Bundle args) {
        if (args == null) return;
        Inmueble inmueble = (Inmueble) args.getSerializable("inmueble");
        mostrarInmuebleSiExiste(inmueble);
    }

    public void mostrarInmuebleSiExiste(Inmueble inmueble) {
        if (inmueble != null) {
            inmuebleSeleccionado.postValue(inmueble);
        }
    }

    // ==========================
    // 🔹 GUARDADO SIMPLE
    // ==========================
    public void procesarGuardado(String direccion, String precioStr, boolean disponible) {
        if (direccion.isEmpty() || precioStr.isEmpty()) {
            mensajeToast.postValue("⚠️ Complete todos los campos");
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr);
            Inmueble nuevo = new Inmueble(direccion.trim(), precio, disponible);
            insertar(nuevo);
            mensajeToast.postValue("✅ Inmueble guardado correctamente");
            navegarAtras.postValue(true);
        } catch (NumberFormatException e) {
            mensajeToast.postValue("❌ Precio inválido");
        }
    }

    private void actualizarListaLocal(Inmueble nuevo) {
        List<Inmueble> actual = listaLiveData.getValue();
        if (actual == null) actual = new ArrayList<>();
        actual.add(nuevo);
        listaLiveData.postValue(actual);
    }

    // ==========================
    // 🔹 DISPONIBILIDAD
    // ==========================
    public void actualizarDisponibilidad(Inmueble inmueble) {
        if (inmueble == null) return;

        Log.d("InmuebleVM", "🔄 Actualizando disponibilidad ID=" + inmueble.getId()
                + " estado=" + inmueble.isDisponible());

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
    // 🔹 ACTUALIZACIÓN COMPLETA
    // ==========================
    public void actualizarInmueble(Inmueble inmueble, Uri imagenUri) {
        LiveData<Boolean> resultado = repo.actualizarInmueble(inmueble, imagenUri);
        resultado.observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean exito) {
                resultado.removeObserver(this);
                if (Boolean.TRUE.equals(exito)) {
                    mensajeToast.postValue("✅ Inmueble actualizado en el servidor");
                    cargarInmueblesDesdeApi();
                } else {
                    mensajeToast.postValue("⚠️ Error al actualizar inmueble");
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

    // ==========================
    // 🔹 FILTRO / RECARGA
    // ==========================
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
