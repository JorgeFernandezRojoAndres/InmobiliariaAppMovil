package com.jorge.inmobiliaria2025.viewmodel;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
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

/**
 * ✅ InmuebleViewModel
 * Unifica los datos locales (Room) y remotos (API .NET).
 * Elimina mocks y sincroniza directamente con el backend real.
 */
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

    public void cargarDesdeBundle(Bundle args) {
        if (args != null && args.containsKey("inmueble")) {
            Inmueble dato = (Inmueble) args.getSerializable("inmueble");
            inmuebleSeleccionado.setValue(dato);
        }
    }

    public void guardarInmueble(String direccion, String precioStr, boolean disponible) {
        if (direccion == null || direccion.trim().isEmpty() ||
                precioStr == null || precioStr.trim().isEmpty()) {
            estadoGuardado.postValue(EstadoGuardado.CAMPOS_VACIOS);
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr);
            Inmueble nuevo = new Inmueble(direccion.trim(), precio, disponible);
            insertar(nuevo);
            estadoGuardado.postValue(EstadoGuardado.EXITO);
        } catch (NumberFormatException e) {
            estadoGuardado.postValue(EstadoGuardado.PRECIO_INVALIDO);
        }
    }

    private void actualizarListaLocal(Inmueble nuevo) {
        List<Inmueble> actual = listaLiveData.getValue();
        if (actual == null) actual = new ArrayList<>();
        actual.add(nuevo);
        listaLiveData.postValue(actual);
    }

    // 🔹 Métodos de compatibilidad para Fragment antiguos
    public LiveData<List<Inmueble>> getListaLiveData() {
        return getInmuebles(); // Alias para compatibilidad con InmueblesFragment
    }

    public void cargarInmuebles() {
        cargarInmueblesDesdeApi(); // Alias para compatibilidad con InmueblesFragment
    }
}
