package com.jorge.inmobiliaria2025.viewmodel;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.data.InmobiliariaDatabase;
import com.jorge.inmobiliaria2025.data.InmuebleDao;
import com.jorge.inmobiliaria2025.data.InmuebleRepository;
import com.jorge.inmobiliaria2025.model.Inmueble;

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

    // 🔹 Repositorio remoto
    private final InmuebleRepository repo;
    private final MutableLiveData<List<Inmueble>> listaInmueblesRemotos = new MutableLiveData<>();

    // 🔹 LiveData para mensajes y navegación
    private final MutableLiveData<String> mensajeToast = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navegarAtras = new MutableLiveData<>();

    public InmuebleViewModel(@NonNull Application application) {
        super(application);
        InmobiliariaDatabase db = InmobiliariaDatabase.getDatabase(application);
        inmuebleDao = db.inmuebleDao();
        listaInmueblesRoom = inmuebleDao.obtenerTodos();
        repo = new InmuebleRepository(application.getApplicationContext());

        // 🔹 Carga inicial: intenta traer desde API, si falla usa Room
        cargarInmueblesDesdeApi();
    }

    public enum EstadoGuardado { EXITO, CAMPOS_VACIOS, PRECIO_INVALIDO }

    public LiveData<EstadoGuardado> getEstadoGuardado() {
        return estadoGuardado;
    }

    public LiveData<List<Inmueble>> getInmuebles() {
        return listaLiveData;
    }

    public LiveData<List<Inmueble>> getInmueblesRemotos() {
        return listaInmueblesRemotos;
    }

    public LiveData<Inmueble> getInmuebleSeleccionado() {
        return inmuebleSeleccionado;
    }

    public void setInmuebleSeleccionado(Inmueble inmueble) {
        inmuebleSeleccionado.setValue(inmueble);
    }

    // 🔹 Carga preferente desde la API
    public void cargarInmueblesDesdeApi() {
        repo.obtenerInmueblesAlquilados().observeForever(lista -> {
            if (lista != null && !lista.isEmpty()) {
                listaInmueblesRemotos.postValue(lista);
                listaLiveData.postValue(lista);
                Log.i("InmuebleVM", "✅ Inmuebles cargados desde API: " + lista.size());
            } else {
                Log.w("InmuebleVM", "⚠️ No hay inmuebles del propietario en API, usando Room...");
                cargarInmueblesDesdeRoom();
            }
        });
    }

    // 🔹 Fallback si no hay conexión o API vacía
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

    // 🔹 Inserta un inmueble nuevo y sincroniza la lista local
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

    // 🔹 Carga sin lógica en Fragment
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

    // 🔹 Nuevo método centralizado (reemplaza el if/switch del Fragment)
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

    // 🔹 Actualiza lista local tras inserción
    private void actualizarListaLocal(Inmueble nuevo) {
        List<Inmueble> actual = listaLiveData.getValue();
        if (actual == null) actual = new ArrayList<>();
        actual.add(nuevo);
        listaLiveData.postValue(actual);
    }

    // 🔹 Utilidad para mostrar toasts desde el Fragment sin lógica
    public void mostrarToast(Context context, String mensaje) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
    }

    public LiveData<String> getMensajeToast() {
        return mensajeToast;
    }

    public LiveData<Boolean> getNavegarAtras() {
        return navegarAtras;
    }

    // 🔹 Métodos declarativos
    public String getTextoDisponibilidad(Inmueble inmueble) {
        return inmueble.isDisponible() ? "Disponible" : "No disponible";
    }

    public int getColorDisponibilidad(Context context, Inmueble inmueble) {
        int color = inmueble.isDisponible()
                ? android.R.color.holo_green_dark
                : android.R.color.holo_red_dark;
        return ContextCompat.getColor(context, color);
    }

    // 🔹 LiveData filtrado
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

    // 🔹 Compatibilidad con fragments antiguos
    public LiveData<List<Inmueble>> getListaLiveData() {
        return getInmuebles();
    }

    public void cargarInmuebles() {
        cargarInmueblesDesdeApi();
    }
}
