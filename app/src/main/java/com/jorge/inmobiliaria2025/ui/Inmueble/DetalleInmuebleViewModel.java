package com.jorge.inmobiliaria2025.ui.Inmueble;

import android.app.Application;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.model.TipoInmueble;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DetalleInmuebleViewModel extends AndroidViewModel {

    private final InmuebleRepository repo;

    // ==== LiveData principales ====
    private final MutableLiveData<Inmueble> inmueble = new MutableLiveData<>();
    private final MutableLiveData<List<TipoInmueble>> tiposInmueble = new MutableLiveData<>(new ArrayList<>());
    private final MediatorLiveData<TipoInmueble> tipoSeleccionadoMediator = new MediatorLiveData<>();

    private final MutableLiveData<Boolean> modoEdicion = new MutableLiveData<>(false);
    private final MutableLiveData<Uri> imagenSeleccionada = new MutableLiveData<>();
    private final MutableLiveData<String> imagenUrl = new MutableLiveData<>();

    // ==== Campos de formulario ====
    private final MutableLiveData<String> direccion = new MutableLiveData<>();
    private final MutableLiveData<String> precio = new MutableLiveData<>();
    private final MutableLiveData<String> metros = new MutableLiveData<>();
    private final MutableLiveData<Boolean> activo = new MutableLiveData<>();
    private final MutableLiveData<String> metrosFormateados = new MutableLiveData<>();

    // ==== Visibilidad botones ====
    private final MutableLiveData<Integer> visibilidadGuardar = new MutableLiveData<>(View.GONE);
    private final MutableLiveData<Integer> visibilidadEditar = new MutableLiveData<>(View.VISIBLE);
    private final MutableLiveData<Integer> visibilidadCambiarImg = new MutableLiveData<>(View.GONE);

    // ==== Eventos UI ====
    private final MutableLiveData<String> mensajeToast = new MutableLiveData<>();
    private final MutableLiveData<Void> accionNavegarAtras = new MutableLiveData<>();

    public DetalleInmuebleViewModel(@NonNull Application app) {
        super(app);
        repo = new InmuebleRepository(app);

        cargarTiposInmueble();

        // Sincronizar tipo seleccionado automáticamente
        tipoSeleccionadoMediator.addSource(inmueble, currentInmueble ->
                sincronizarTipoSeleccionado(currentInmueble, tiposInmueble.getValue()));
        tipoSeleccionadoMediator.addSource(tiposInmueble, currentTipos ->
                sincronizarTipoSeleccionado(inmueble.getValue(), currentTipos));
    }

    // ==== Sincronización tipo seleccionado ====
    private void sincronizarTipoSeleccionado(Inmueble currentInmueble, List<TipoInmueble> currentTipos) {
        if (currentInmueble != null && currentTipos != null && !currentTipos.isEmpty()) {
            Optional<TipoInmueble> foundType = currentTipos.stream()
                    .filter(t -> t.getId() == currentInmueble.getTipoId())
                    .findFirst();

            tipoSeleccionadoMediator.postValue(foundType.orElse(null));
        } else {
            tipoSeleccionadoMediator.postValue(null);
        }
    }

    // ==== Getters LiveData ====
    public LiveData<Inmueble> getInmueble() { return inmueble; }
    public LiveData<List<TipoInmueble>> getTiposInmueble() { return tiposInmueble; }
    public LiveData<TipoInmueble> getTipoSeleccionado() { return tipoSeleccionadoMediator; }

    public LiveData<String> getDireccion() { return direccion; }
    public LiveData<String> getPrecio() { return precio; }
    public LiveData<String> getMetros() { return metros; }
    public LiveData<String> getMetrosFormateados() { return metrosFormateados; }
    public LiveData<Boolean> getActivo() { return activo; }

    public LiveData<Integer> getVisibilidadGuardar() { return visibilidadGuardar; }
    public LiveData<Integer> getVisibilidadEditar() { return visibilidadEditar; }
    public LiveData<Integer> getVisibilidadCambiarImg() { return visibilidadCambiarImg; }

    public LiveData<Uri> getImagenSeleccionada() { return imagenSeleccionada; }
    public LiveData<String> getImagenUrl() { return imagenUrl; }

    public LiveData<String> getMensajeToast() { return mensajeToast; }
    public LiveData<Void> getAccionNavegarAtras() { return accionNavegarAtras; }
    public LiveData<Boolean> getModoEdicion() { return modoEdicion; }

    // ==== Setters ====
    public void setDireccion(String value) { direccion.setValue(value); }
    public void setPrecio(String value) { precio.setValue(value); }
    public void setMetros(String value) { metros.setValue(value); }
    public void setActivo(boolean value) { activo.setValue(value); }
    public void setTipoSeleccionado(TipoInmueble tipo) { tipoSeleccionadoMediator.setValue(tipo); }
    public void setImagenSeleccionada(Uri uri) { imagenSeleccionada.setValue(uri); }

    // ==== Cargar datos ====
    public void cargarInmueble(Inmueble recibido) {
        inmueble.setValue(recibido);

        if (recibido != null) {
            direccion.setValue(recibido.getDireccion());
            precio.setValue(String.valueOf(recibido.getPrecio()));
            metros.setValue(String.valueOf(recibido.getMetrosCuadrados()));
            activo.setValue(recibido.isActivo());
            imagenUrl.setValue(
                    Optional.ofNullable(recibido.getImagenUrl())
                            .filter(url -> !url.isEmpty())
                            .orElse(null)
            );
        } else {
            imagenUrl.setValue(null);
        }
    }

    private void cargarTiposInmueble() {
        repo.obtenerTiposInmueble().observeForever(lista ->
                tiposInmueble.postValue(lista != null ? lista : new ArrayList<>()));
    }

    // ==== Selección de imagen ====
    public void procesarSeleccionImagen(ActivityResult result) {
        if (result != null && result.getData() != null && result.getData().getData() != null) {
            Uri uri = result.getData().getData();
            imagenSeleccionada.postValue(uri);
        } else {
            mensajeToast.postValue("⚠️ No se seleccionó ninguna imagen");
        }
    }

    // ==== Guardar cambios ====
    public void guardarCambios(InmuebleViewModel globalVM) {
        Inmueble actual = inmueble.getValue();
        if (actual == null) {
            mensajeToast.postValue("⚠️ No se pudo cargar el inmueble actual");
            return;
        }

        String dir = safeText(direccion);
        String prec = safeText(precio);
        String met = safeText(metros);
        Boolean act = activo.getValue();
        TipoInmueble tipo = tipoSeleccionadoMediator.getValue();
        Uri uriSeleccionada = imagenSeleccionada.getValue();

        if (dir.isEmpty() || prec.isEmpty() || met.isEmpty()) {
            mensajeToast.postValue("⚠️ Campos incompletos");
            return;
        }

        double precioDouble = parseDoubleSafe(prec, "❌ Precio inválido");
        int metrosInt = parseIntSafe(met, "❌ Metros inválidos");
        if (precioDouble <= 0 || metrosInt <= 0) return;

        Inmueble actualizado = construirInmuebleActualizado(actual, dir, precioDouble, metrosInt, act, tipo);
        Uri uriFinal = resolverUriFinal(uriSeleccionada, imagenUrl.getValue());

        if (uriFinal == null) {
            guardarSinImagen(actualizado, globalVM);
        } else {
            guardarConImagen(actualizado, uriFinal, globalVM);
        }
    }


    // ==== Helpers privados ====
    private String safeText(MutableLiveData<String> field) {
        return Optional.ofNullable(field.getValue()).orElse("").trim();
    }

    private double parseDoubleSafe(String valor, String mensajeError) {
        try {
            return Double.parseDouble(valor.replace(",", "."));
        } catch (NumberFormatException e) {
            mensajeToast.postValue(mensajeError);
            return -1;
        }
    }

    private int parseIntSafe(String valor, String mensajeError) {
        try {
            return Integer.parseInt(valor.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            mensajeToast.postValue(mensajeError);
            return -1;
        }
    }

    private Inmueble construirInmuebleActualizado(Inmueble base, String dir, double precioDouble, int metrosInt,
                                                  Boolean act, TipoInmueble tipo) {
        Inmueble actualizado = new Inmueble(base.getId(), dir, precioDouble, act != null && act);
        actualizado.setMetrosCuadrados(metrosInt);
        if (tipo != null) {
            actualizado.setTipoId(tipo.getId());
            actualizado.setTipoNombre(tipo.getNombre());
        } else {
            actualizado.setTipoId(base.getTipoId());
            actualizado.setTipoNombre(base.getTipoNombre());
        }
        return actualizado;
    }

    private Uri resolverUriFinal(Uri uriSeleccionada, String imagenUrlActual) {
        if (uriSeleccionada != null) return uriSeleccionada;
        if (imagenUrlActual != null && !imagenUrlActual.isEmpty()) return Uri.parse(imagenUrlActual);
        return null;
    }

    private void guardarSinImagen(Inmueble actualizado, InmuebleViewModel globalVM) {
        repo.actualizarInmueble(actualizado, null).observeForever(ok -> {
            manejarResultadoGuardar(ok, actualizado, globalVM);
        });
    }

    private void guardarConImagen(Inmueble actualizado, Uri uriFinal, InmuebleViewModel globalVM) {
        repo.actualizarInmuebleConImagenForm(actualizado, uriFinal)
                .observeForever(ok -> manejarResultadoGuardar(ok, actualizado, globalVM));
    }

    private void manejarResultadoGuardar(Boolean ok, Inmueble actualizado, InmuebleViewModel globalVM) {
        mensajeToast.postValue(Boolean.TRUE.equals(ok)
                ? "✅ Inmueble actualizado correctamente"
                : "⚠️ Error al guardar los cambios");

        if (Boolean.TRUE.equals(ok)) {
            inmueble.postValue(actualizado);
            setEnEdicion(false);
            if (globalVM != null && actualizado.getId() != 0) {
                globalVM.actualizarInmuebleEnLista(actualizado);
            }
            accionNavegarAtras.postValue(null);
        }
    }

    // ==== Edición ====
    private boolean enEdicion = false;
    public void setEnEdicion(boolean valor) { enEdicion = valor; }

    public void habilitarEdicion() {
        modoEdicion.postValue(true);
        visibilidadGuardar.postValue(View.VISIBLE);
        visibilidadEditar.postValue(View.GONE);
        visibilidadCambiarImg.postValue(View.VISIBLE);
    }

    // ==== Formato metros ====
    public void actualizarMetrosTexto(String texto) {
        if (texto == null) return;
        String limpio = texto.replaceAll("[^0-9]", "");
        String textoFormateado = limpio.isEmpty() ? "0 m²" : limpio + " m²";
        metrosFormateados.postValue(textoFormateado);
    }
}
