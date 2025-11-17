package com.jorge.inmobiliaria2025.ui.Inmueble;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import org.jspecify.annotations.Nullable;

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

    // === NUEVOS CAMPOS ===
    private final MutableLiveData<List<TipoInmueble>> tiposInmueble = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<TipoInmueble> tipoSeleccionado = new MutableLiveData<>();
    private final MutableLiveData<Void> accionLimpiarCampos = new MutableLiveData<>();
    private final MutableLiveData<Void> accionNavegarAtras = new MutableLiveData<>();
    private final MutableLiveData<String> usoSeleccionado = new MutableLiveData<>();

    public LiveData<Void> getAccionLimpiarCampos() { return accionLimpiarCampos; }
    public LiveData<Void> getAccionNavegarAtras() { return accionNavegarAtras; }
    public LiveData<List<TipoInmueble>> getTiposInmueble() { return tiposInmueble; }
    public LiveData<TipoInmueble> getTipoSeleccionado() { return tipoSeleccionado; }
    public void setTipoSeleccionado(TipoInmueble tipo) { tipoSeleccionado.postValue(tipo); }
    public LiveData<String> getUso() { return usoSeleccionado; }
    public void setUso(String valor) { usoSeleccionado.postValue(valor); }

    public InmuebleViewModel(@NonNull Application application) {
        super(application);

        // ✅ Inicializar correctamente la base local
        InmobiliariaDatabase db = InmobiliariaDatabase.getInstance(application.getApplicationContext());
        inmuebleDao = db.inmuebleDao();

        listaInmueblesRoom = inmuebleDao.obtenerTodos();
        repo = new InmuebleRepository(application);

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

    public void actualizarInmuebleEnLista(Inmueble actualizado) {
        if (actualizado == null) return;

        List<Inmueble> listaActual = listaLiveData.getValue();
        if (listaActual == null || listaActual.isEmpty()) return;

        // 🔹 Buscar y reemplazar el inmueble actualizado
        List<Inmueble> nuevaLista = new ArrayList<>(listaActual);
        for (int i = 0; i < nuevaLista.size(); i++) {
            if (nuevaLista.get(i).getId() == actualizado.getId()) {
                nuevaLista.set(i, actualizado);
                break;
            }
        }

        listaLiveData.postValue(nuevaLista);
        Log.i("InmuebleVM", "🔁 Lista actualizada con cambios del inmueble ID=" + actualizado.getId());
    }

    // ====================================================
    // 🔹 Procesar selección de imagen desde el Fragment
    // ====================================================
    private final MutableLiveData<Uri> imagenUriSeleccionadaLiveData = new MutableLiveData<>();
    public LiveData<Uri> getImagenUriSeleccionada() { return imagenUriSeleccionadaLiveData; }

    public void procesarSeleccionImagen(ActivityResult result) {
        if (result == null) return;

        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Uri uri = result.getData().getData();
            if (uri != null) {
                imagenUriSeleccionadaLiveData.postValue(uri);
                Log.d("InmuebleVM", "📸 Imagen seleccionada: " + uri);
            }
        } else {
            mensajeToast.postValue("⚠️ No se seleccionó ninguna imagen");
        }
    }

    // 🧭 ViewModel global para manejar navegación
    private NavViewModel navVM;
    public void setNavViewModel(NavViewModel navVM) { this.navVM = navVM; }


    public void onInmuebleClick(Inmueble inmueble) {
        if (inmueble == null) return;

        setInmuebleSeleccionado(inmueble);

        Bundle bundle = new Bundle();
        bundle.putSerializable("inmueble", inmueble);

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

        accionNavegarDetalle.postValue(bundle);
        Log.d("InmuebleVM", "➡️ Navegando al detalle: " + inmueble.getDireccion());
    }

    // 🔄 Cambiar disponibilidad (disponible / no disponible)
    public void onCambiarDisponibilidad(Inmueble inmueble) {
        if (inmueble == null) return;

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
    // =====================================================
// 🔹 Orquestador: llamado desde el Fragment
// =====================================================
    public void onGuardarInmuebleClick(String direccion, String precioStr, String metrosStr,
                                       int posTipo, String uso, Uri imagenUri) {

        List<TipoInmueble> tipos = tiposInmueble.getValue();

        if (tipos == null || tipos.isEmpty()) {
            Log.w("InmuebleVM", "⚠️ Tipos de inmueble no cargados o lista vacía");
            mensajeToast.postValue("⚠️ No se pudieron cargar los tipos de inmueble");
            return;
        }

        if (posTipo < 0 || posTipo >= tipos.size()) {
            Log.w("InmuebleVM", "⚠️ Posición de tipo inválida: " + posTipo);
            mensajeToast.postValue("⚠️ Seleccione un tipo de inmueble válido");
            return;
        }

        TipoInmueble tipoSeleccionado = tipos.get(posTipo);
        Log.d("InmuebleVM", "🧩 Tipo seleccionado: ID=" + tipoSeleccionado.getId() +
                ", Nombre=" + tipoSeleccionado.getNombre());

        if (direccion == null || direccion.trim().isEmpty()) {
            mensajeToast.postValue("⚠️ La dirección es obligatoria");
            return;
        }
        if (precioStr == null || precioStr.trim().isEmpty()) {
            mensajeToast.postValue("⚠️ El precio es obligatorio");
            return;
        }
        if (metrosStr == null || metrosStr.trim().isEmpty()) {
            mensajeToast.postValue("⚠️ Los metros cuadrados son obligatorios");
            return;
        }

        double precio;
        int metros;

        try {
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            mensajeToast.postValue("❌ Precio inválido");
            Log.e("InmuebleVM", "❌ Error al convertir precio: " + precioStr, e);
            return;
        }

        try {
            metros = Integer.parseInt(metrosStr);
        } catch (NumberFormatException e) {
            mensajeToast.postValue("❌ Metros inválidos");
            Log.e("InmuebleVM", "❌ Error al convertir metros: " + metrosStr, e);
            return;
        }

        setTipoSeleccionado(tipoSeleccionado);
        setUso(uso);

        Log.d("InmuebleVM", "📦 Preparando guardado -> Dir=" + direccion +
                ", Precio=" + precio + ", M2=" + metros +
                ", Tipo=" + tipoSeleccionado.getNombre() +
                ", Uso=" + uso);

        // ✅ Pasamos tipoSeleccionado al método de guardado
        guardarInmueble(
                direccion,
                String.valueOf(precio),
                String.valueOf(metros),
                false, // inactivo por defecto
                imagenUri,
                uso,
                tipoSeleccionado
        );
    }


    public enum EstadoGuardado { EXITO,}

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
    // 🔹 CARGA DE INMUEBLES (API + Room)
    // ==========================
    public void cargarInmueblesDesdeApi() {
        // aca recibimos  la respuesta del Repository
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
    // 🆕 SUBIR IMAGEN INDIVIDUAL
    // ==========================
    public void subirImagenInmueble(int idInmueble, Uri imagenUri) {
        if (imagenUri == null) {
            mensajeToast.postValue("⚠️ Seleccione una imagen antes de guardar");
            return;
        }

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

    // ✅ Versión final con tipo real desde API y uso
    public void guardarInmueble(String direccion, String precioTexto, String metrosTexto,
                                boolean disponible, Uri imagenUri, String uso, TipoInmueble tipoSeleccionado) {

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

        // 🔹 Crear el inmueble con los datos ingresados
        Inmueble nuevo = new Inmueble(direccion.trim(), precio, false);
        nuevo.setMetrosCuadrados(metros);

        // 🔹 Asignar tipo de inmueble real
        if (tipoSeleccionado != null) {
            nuevo.setTipoId(tipoSeleccionado.getId());
            nuevo.setTipoNombre(tipoSeleccionado.getNombre());
            Log.d("InmuebleVM", "🏷️ Tipo aplicado -> ID=" + tipoSeleccionado.getId() +
                    ", Nombre=" + tipoSeleccionado.getNombre());
        } else {
            nuevo.setTipoId(1);
            nuevo.setTipoNombre("Sin especificar");
            Log.w("InmuebleVM", "⚠️ TipoSeleccionado es null, se aplica valor por defecto");
        }

        // 🔹 Asignar uso
        if (uso != null && !uso.trim().isEmpty()) {
            nuevo.setUso(uso.trim());
        }

        // 🔹 Enviar al repositorio
        LiveData<Inmueble> creado = repo.crearInmueble(nuevo);
        creado.observeForever(new Observer<Inmueble>() {
            @Override
            public void onChanged(Inmueble inmuebleCreado) {
                creado.removeObserver(this);

                if (inmuebleCreado == null) {
                    mensajeToast.postValue("⚠️ Error al crear el inmueble en el servidor");
                    return;
                }

                mensajeToast.postValue("✅ Inmueble creado correctamente");

                // 🔹 Si se seleccionó una imagen, subirla
                if (imagenUri != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                            subirImagenInmueble(inmuebleCreado.getId(), imagenUri)
                    );
                }

                // 🔹 Actualizar vista y limpiar
                cargarInmueblesDesdeApi();
                estadoGuardado.postValue(EstadoGuardado.EXITO);
                accionLimpiarCampos.postValue(null);
                accionNavegarAtras.postValue(null);
            }
        });
    }

    // ==========================
    // 🔹 UTILIDADES VISUALES
    // ==========================
    public void mostrarToast(Context context, String mensaje) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
    }

    public LiveData<List<Inmueble>> getListaFiltrada() {
        return androidx.lifecycle.Transformations.map(listaLiveData, lista ->
                (lista == null) ? new ArrayList<>() : lista
        );
    }

    public LiveData<List<Inmueble>> getListaLiveData() { return getInmuebles(); }
    public void cargarInmuebles() { cargarInmueblesDesdeApi(); }
}

