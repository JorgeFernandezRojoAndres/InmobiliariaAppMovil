package com.jorge.inmobiliaria2025.ui.Inmueble;

import android.app.Application;
import android.net.Uri;
import android.util.Log;
import java.util.Objects;

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
    private final MutableLiveData<String> metros = new MutableLiveData<>();
    private final MutableLiveData<Boolean> activo = new MutableLiveData<>();
    private final MutableLiveData<TipoInmueble> tipoSeleccionado = new MutableLiveData<>();

    public LiveData<String> getDireccion() { return direccion; }
    public LiveData<String> getPrecio() { return precio; }
    public LiveData<String> getMetros() { return metros; }
    public LiveData<Boolean> getActivo() { return activo; }
    public LiveData<TipoInmueble> getTipoSeleccionado() { return tipoSeleccionado; }
    public DetalleInmuebleViewModel(@NonNull Application app) {
        super(app);
        repo = new InmuebleRepository(app.getApplicationContext());
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
                               boolean activo, int indiceTipo, List<TipoInmueble> tipos, Uri imagenUri) {

        Inmueble actual = inmueble.getValue();
        if (actual == null) {
            mensajeToast.postValue("⚠️ No se pudo cargar el inmueble actual");
            return;
        }

        // 🔹 Usa valores actuales si están vacíos
        String dir = (direccion != null && !direccion.trim().isEmpty())
                ? direccion.trim()
                : actual.getDireccion();

        // 🔹 Normalizar precio (sin formatear, tal cual el usuario lo ingresa)
        String prec = (precio != null && !precio.trim().isEmpty())
                ? precio.replace(",", ".").trim()
                : new java.text.DecimalFormat("0.##").format(actual.getPrecio());

        // 🔹 Limpia metros: elimina símbolos o letras
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
        actualizado.setTipoId(
                (tipos != null && indiceTipo >= 0 && indiceTipo < tipos.size())
                        ? tipos.get(indiceTipo).getId()
                        : actual.getTipoId()
        );

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

        repo.actualizarInmuebleConImagenForm(actualizado, uriFinal)
                .observeForever(ok -> {
                    mensajeToast.postValue(Boolean.TRUE.equals(ok)
                            ? "✅ Inmueble actualizado correctamente"
                            : "⚠️ Error al guardar los cambios");
                    if (Boolean.TRUE.equals(ok)) {
                        accionNavegarAtras.postValue(null);
                        Log.i("DetalleVM", "💾 Inmueble actualizado y sincronizado con API");
                    }
                });
    }


    // 🔹 Mostrar inmueble actual en UI (sin ifs en fragment)
    private <T> void postIfChanged(MutableLiveData<T> liveData, T nuevoValor) {
        if (!Objects.equals(liveData.getValue(), nuevoValor)) {
            liveData.postValue(nuevoValor);
        }
    }

    private boolean enEdicion = false;
    public void setEnEdicion(boolean valor) { enEdicion = valor; }


    public void mostrarInmuebleEn(Inmueble inm) {
        if (enEdicion) return; // 🚫 No repoblar mientras el usuario edita

        Optional.ofNullable(inm)
                .ifPresentOrElse(
                        i -> {
                            inmueble.postValue(i);
                            imagenUrl.postValue(
                                    Optional.ofNullable(i.getImagenUrl())
                                            .filter(url -> !url.isEmpty())
                                            .orElse(null)
                            );

                            // ✳️ Emitir valores individuales solo si cambian
                            postIfChanged(direccion, i.getDireccion());
                            postIfChanged(precio, String.valueOf(i.getPrecio()));
                            postIfChanged(metros, String.valueOf(i.getMetrosCuadrados()));
                            postIfChanged(activo, i.isActivo());

                            // 🌀 Selección de tipo por ID, usando el LiveData existente
                            Optional.ofNullable(tiposInmueble.getValue())
                                    .flatMap(lista -> lista.stream()
                                            .filter(t -> t.getId() == i.getTipoId())
                                            .findFirst())
                                    .ifPresent(t -> postIfChanged(tipoSeleccionado, t));
                        },
                        () -> {
                            inmueble.postValue(null);
                            imagenUrl.postValue(null);
                        }
                );
    }

}
