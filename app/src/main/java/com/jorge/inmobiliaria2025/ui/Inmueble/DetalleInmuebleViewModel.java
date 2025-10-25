package com.jorge.inmobiliaria2025.ui.Inmueble;

import android.app.Application;
import android.net.Uri;
import android.util.Log;
import java.util.Objects;
import android.view.View;

import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.model.TipoInmueble;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DetalleInmuebleViewModel extends AndroidViewModel {

    private final InmuebleRepository repo;
    private final MutableLiveData<Inmueble> inmueble = new MutableLiveData<>();
    private final MutableLiveData<List<TipoInmueble>> tiposInmueble = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> mensajeToast = new MutableLiveData<>();
    private final MutableLiveData<Void> accionNavegarAtras = new MutableLiveData<>();
    private final MutableLiveData<Uri> imagenSeleccionada = new MutableLiveData<>();
    private final MutableLiveData<String> imagenUrl = new MutableLiveData<>();

    private final MutableLiveData<String> metrosFormateados = new MutableLiveData<>();
    private final MutableLiveData<String> direccion = new MutableLiveData<>();
    private final MutableLiveData<String> precio = new MutableLiveData<>();
    private final MutableLiveData<Integer> visibilidadGuardar = new MutableLiveData<>(View.GONE);
    private final MutableLiveData<Integer> visibilidadEditar = new MutableLiveData<>(View.VISIBLE);
    private final MutableLiveData<Integer> visibilidadCambiarImg = new MutableLiveData<>(View.GONE);
    private final MutableLiveData<String> metros = new MutableLiveData<>();
    private final MutableLiveData<Boolean> activo = new MutableLiveData<>();
    private final MutableLiveData<TipoInmueble> tipoSeleccionado = new MutableLiveData<>();
    private final MutableLiveData<Boolean> modoEdicion = new MutableLiveData<>(false);
    public LiveData<Boolean> getModoEdicion() { return modoEdicion; }

    public LiveData<String> getDireccion() { return direccion; }
    public LiveData<String> getPrecio() { return precio; }
    public LiveData<String> getMetros() { return metros; }
    public LiveData<Integer> getVisibilidadGuardar() { return visibilidadGuardar; }
    public LiveData<Integer> getVisibilidadEditar() { return visibilidadEditar; }
    public LiveData<Integer> getVisibilidadCambiarImg() { return visibilidadCambiarImg; }
    public LiveData<Boolean> getActivo() { return activo; }
    public LiveData<TipoInmueble> getTipoSeleccionado() { return tipoSeleccionado; }
    public DetalleInmuebleViewModel(@NonNull Application app) {
        super(app);
        repo = new InmuebleRepository(app);

        cargarTiposInmueble();
    }



    // 🔹 LiveData públicos
    public LiveData<Inmueble> getInmueble() { return inmueble; }
    public LiveData<String> getMensajeToast() { return mensajeToast; }
    public LiveData<Void> getAccionNavegarAtras() { return accionNavegarAtras; }
    public LiveData<Uri> getImagenSeleccionada() { return imagenSeleccionada; }
    public LiveData<List<TipoInmueble>> getTiposInmueble() { return tiposInmueble; }
    public LiveData<String> getImagenUrl() { return imagenUrl; }

    // 🔹 Cargar inmueble recibido del argumento
    public void cargarInmueble(Inmueble recibido) {
        inmueble.postValue(recibido);

        if (recibido != null) {
            direccion.postValue(recibido.getDireccion());
            precio.postValue(String.valueOf(recibido.getPrecio()));
            metros.postValue(String.valueOf(recibido.getMetrosCuadrados()));
            activo.postValue(recibido.isActivo());

            // ✅ Cargar imagen si existe, o dejar null para que Glide use placeholder
            imagenUrl.postValue(
                    Optional.ofNullable(recibido.getImagenUrl())
                            .filter(url -> !url.isEmpty())
                            .orElse(null)
            );

            // 🌀 Cargar tipo de inmueble si ya está disponible
            Optional.ofNullable(tiposInmueble.getValue())
                    .flatMap(lista -> lista.stream()
                            .filter(t -> t.getId() == recibido.getTipoId())
                            .findFirst())
                    .ifPresent(tipoSeleccionado::postValue);
        } else {
            imagenUrl.postValue(null);
        }

        Log.d("DetalleVM", "📦 Inmueble recibido: " + (recibido != null ? recibido.getDireccion() : "null"));
    }

    // 🔹 Cargar lista de tipos de inmueble desde el repositorio
    private void cargarTiposInmueble() {
        repo.obtenerTiposInmueble().observeForever(lista -> {
            tiposInmueble.postValue(lista != null ? lista : new ArrayList<>());
            Log.d("DetalleVM", "🏗️ Tipos cargados: " + (lista != null ? lista.size() : 0));
        });
    }

    // 🔹 Procesar selección de imagen (desde ActivityResult)
    public void procesarSeleccionImagen(ActivityResult result, ImageView destino) {
        if (result.getData() != null && result.getData().getData() != null) {
            Uri uri = result.getData().getData();
            destino.setImageURI(uri);
            imagenSeleccionada.postValue(uri);
            Log.d("DetalleVM", "🖼️ Imagen seleccionada: " + uri);
        } else {
            mensajeToast.postValue("⚠️ No se seleccionó ninguna imagen");
        }
    }

    // 🔹 Formatea el valor de los metros cuadrados y lo envía a la vista
    public void formatearMetros(String valor) {
        // Eliminar caracteres no numéricos
        String limpio = valor.replaceAll("[^0-9]", "");
        String textoFormateado = limpio.isEmpty() ? "0 m²" : limpio + " m²";

        // Actualiza el LiveData con el valor formateado
        metrosFormateados.setValue(textoFormateado);
    }

    public void guardarCambios(String direccion, String metros, String precio,
                               boolean activo, int indiceTipo, List<TipoInmueble> tipos, Uri imagenUri,
                               InmuebleViewModel globalVM) {

        Inmueble actual = inmueble.getValue();
        if (actual == null) {
            mensajeToast.postValue("⚠️ No se pudo cargar el inmueble actual");
            return;
        }

        String dir = (direccion != null && !direccion.trim().isEmpty())
                ? direccion.trim()
                : actual.getDireccion();

        String prec = (precio != null && !precio.trim().isEmpty())
                ? precio.replace(",", ".").trim()
                : new java.text.DecimalFormat("0.##").format(actual.getPrecio());

        String met = (metros != null && !metros.trim().isEmpty())
                ? metros.replaceAll("[^0-9.]", "").trim()
                : String.valueOf(actual.getMetrosCuadrados());

        double precioDouble;
        int metrosInt;

        try {
            precioDouble = Double.parseDouble(prec);
        } catch (NumberFormatException e) {
            mensajeToast.postValue("❌ Precio inválido");
            return;
        }

        try {
            metrosInt = (int) Double.parseDouble(met);
        } catch (NumberFormatException e) {
            mensajeToast.postValue("❌ Metros cuadrados inválidos");
            return;
        }

        Inmueble actualizado = new Inmueble(
                actual.getId(),
                dir,
                precioDouble,
                activo
        );
        actualizado.setMetrosCuadrados(metrosInt);

        // 🔹 Determinar tipo seleccionado
        TipoInmueble tipo = (tipos != null && indiceTipo >= 0 && indiceTipo < tipos.size())
                ? tipos.get(indiceTipo)
                : Optional.ofNullable(tiposInmueble.getValue())
                .flatMap(lista -> lista.stream()
                        .filter(t -> t.getId() == actual.getTipoId())
                        .findFirst())
                .orElse(null);

        if (tipo != null) {
            actualizado.setTipoId(tipo.getId());
            actualizado.setTipoNombre(tipo.getNombre());
        } else {
            actualizado.setTipoId(actual.getTipoId());
            actualizado.setTipoNombre(actual.getTipoNombre());
        }

        Log.d("DetalleVM", "🔹 Guardando inmueble: tipoId=" + actualizado.getTipoId()
                + " tipoNombre=" + actualizado.getTipoNombre());

        // ✅ Mantener imagen existente
        Uri uriFinal = imagenUri != null
                ? imagenUri
                : (imagenUrl.getValue() != null ? Uri.parse(imagenUrl.getValue()) : null);

        if (uriFinal == null || uriFinal.toString().isEmpty()) {
            uriFinal = actual.getImagenUrl() != null ? Uri.parse(actual.getImagenUrl()) : null;
        }

        if (uriFinal == null) {
            mensajeToast.postValue("⚠️ No se seleccionó ninguna imagen");
            return;
        }

        // 🔹 Guardar mediante repo y actualizar LiveData **antes de navegar**
        final TipoInmueble tipoFinal = tipo;
        repo.actualizarInmuebleConImagenForm(actualizado, uriFinal)
                .observeForever(ok -> {
                    mensajeToast.postValue(Boolean.TRUE.equals(ok)
                            ? "✅ Inmueble actualizado correctamente"
                            : "⚠️ Error al guardar los cambios");

                    if (Boolean.TRUE.equals(ok)) {
                        // 🔹 Actualiza el LiveData local y tipo seleccionado inmediatamente
                        inmueble.postValue(actualizado);
                        tipoSeleccionado.postValue(tipoFinal);

                        // 🔹 Evita sobrescribir en mostrarInmuebleEn()
                        setEnEdicion(false);

                        // 🔹 Actualizar lista global también si se pasó el ViewModel
                        if (globalVM != null) {
                            globalVM.actualizarInmuebleEnLista(actualizado);
                        }

                        accionNavegarAtras.postValue(null);
                        Log.i("DetalleVM", "💾 Inmueble actualizado y LiveData local actualizado");
                    }
                });
    }

    private boolean enEdicion = false;
    public void setEnEdicion(boolean valor) { enEdicion = valor; }



    public void habilitarEdicion() {
        modoEdicion.postValue(true);
        visibilidadGuardar.postValue(View.VISIBLE);
        visibilidadEditar.postValue(View.GONE);
        visibilidadCambiarImg.postValue(View.VISIBLE);
    }


    // 🔹 Reemplaza formatearMetros(String)
    public void actualizarMetrosTexto(String texto) {
        if (texto == null) return;

        String limpio = texto.replaceAll("[^0-9]", "");
        String textoFormateado = limpio.isEmpty() ? "0 m²" : limpio + " m²";
        metrosFormateados.postValue(textoFormateado);
    }

    // 🔹 Getter para que el fragment observe el texto formateado
    public LiveData<String> getMetrosFormateados() {
        return metrosFormateados;
    }
}
