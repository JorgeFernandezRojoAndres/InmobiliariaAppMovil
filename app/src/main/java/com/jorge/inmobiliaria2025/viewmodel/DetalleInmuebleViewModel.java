package com.jorge.inmobiliaria2025.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.FloatValueHolder;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.data.InmuebleRepository;
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

    public DetalleInmuebleViewModel(@NonNull Application app) {
        super(app);
        repo = new InmuebleRepository(app.getApplicationContext());
        cargarTiposInmueble();
    }

    // Getter para el LiveData de metros formateados
    public LiveData<String> getMetrosFormateados() {
        return metrosFormateados;
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

    // 🔹 Guardar cambios del detalle del inmueble
    public void guardarCambios(String direccion, String metros, String precio,
                               boolean activo, int indiceTipo, List<TipoInmueble> tipos, Uri imagenUri) {

        if (direccion == null || direccion.isEmpty() || precio == null || precio.isEmpty()) {
            mensajeToast.postValue("⚠️ Complete todos los campos");
            return;
        }

        Inmueble actual = inmueble.getValue();
        if (actual == null) return;

        Double precioDouble = 0.0;
        try {
            precioDouble = Double.parseDouble(precio);
        } catch (NumberFormatException e) {
            mensajeToast.postValue("❌ Precio inválido");
            return;
        }

        Inmueble actualizado = new Inmueble(
                actual.getId(),
                direccion.trim(),
                precioDouble,
                activo
        );

        if (tipos != null && indiceTipo >= 0 && indiceTipo < tipos.size()) {
            actualizado.setTipoId(tipos.get(indiceTipo).getId());
        }

        if (imagenUri == null) {
            mensajeToast.postValue("⚠️ No se seleccionó ninguna imagen");
            return;
        }

        repo.actualizarInmuebleConImagenForm(actualizado, imagenUri)
                .observeForever(ok -> {
                    mensajeToast.postValue(Boolean.TRUE.equals(ok)
                            ? "✅ Inmueble actualizado correctamente"
                            : "⚠️ Error al guardar los cambios");
                    if (Boolean.TRUE.equals(ok)) {
                        accionNavegarAtras.postValue(null);
                        Log.i("DetalleVM", "💾 Inmueble actualizado y sincronizado con API");
                        // Actualiza la lista de inmuebles o la UI según sea necesario
                    }
                });
    }


    // 🔹 Mostrar inmueble actual en UI (sin ifs en fragment)
    public void mostrarInmuebleEn(Inmueble inm) {
        Optional.ofNullable(inm)
                .ifPresentOrElse(
                        i -> {
                            inmueble.postValue(i);
                            imagenUrl.postValue(
                                    Optional.ofNullable(i.getImagenUrl())
                                            .filter(url -> !url.isEmpty())
                                            .orElse(null)
                            );
                        },
                        () -> {
                            inmueble.postValue(null);
                            imagenUrl.postValue(null);
                        }
                );
    }
}
