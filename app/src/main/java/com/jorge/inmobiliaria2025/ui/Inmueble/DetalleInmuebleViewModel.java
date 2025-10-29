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
import androidx.lifecycle.MediatorLiveData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DetalleInmuebleViewModel extends AndroidViewModel {
    private final MediatorLiveData<TipoInmueble> _tipoSeleccionadoMediator = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> modoEdicion = new MutableLiveData<>(false);
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

    public LiveData<Boolean> getModoEdicion() { return modoEdicion; }

    public LiveData<String> getDireccion() { return direccion; }
    public LiveData<String> getPrecio() { return precio; }
    public LiveData<String> getMetros() { return metros; }
    public LiveData<Integer> getVisibilidadGuardar() { return visibilidadGuardar; }
    public LiveData<Integer> getVisibilidadEditar() { return visibilidadEditar; }
    public LiveData<Integer> getVisibilidadCambiarImg() { return visibilidadCambiarImg; }
    public LiveData<Boolean> getActivo() { return activo; }
    public LiveData<TipoInmueble> getTipoSeleccionado() { return _tipoSeleccionadoMediator; }
    // 🔹 Setters públicos para actualizar LiveData desde la vista
    public void setDireccion(String value) { direccion.setValue(value); }
    public void setMetros(String value) { metros.setValue(value); }
    public void setPrecio(String value) { precio.setValue(value); }
    public void setActivo(boolean value) { activo.setValue(value); }
    public void setTipoSeleccionado(TipoInmueble tipo) { _tipoSeleccionadoMediator.setValue(tipo); }
    public void setImagenSeleccionada(Uri uri) { imagenSeleccionada.setValue(uri); }

    public DetalleInmuebleViewModel(@NonNull Application app) {
        super(app);
        repo = new InmuebleRepository(app);

        cargarTiposInmueble();

        // 🔴 Añade esta lógica para configurar el MediatorLiveData
        _tipoSeleccionadoMediator.addSource(inmueble, currentInmueble -> {
            Log.d("DetalleVM", "Mediator - Inmueble actualizado. Llamando sincronizarTipoSeleccionado.");
            sincronizarTipoSeleccionado(currentInmueble, tiposInmueble.getValue());
        });
        _tipoSeleccionadoMediator.addSource(tiposInmueble, currentTipos -> {
            Log.d("DetalleVM", "Mediator - Tipos actualizado. Llamando sincronizarTipoSeleccionado.");
            sincronizarTipoSeleccionado(inmueble.getValue(), currentTipos);
        });
    }
    // 🔴 Añade este método auxiliar a tu clase DetalleInmuebleViewModel
    private void sincronizarTipoSeleccionado(Inmueble currentInmueble, List<TipoInmueble> currentTipos) {
        Log.d("DetalleVM", "sincronizarTipoSeleccionado - Inmueble: " + (currentInmueble != null ? currentInmueble.getDireccion() + " (TipoID: " + currentInmueble.getTipoId() + ")" : "null"));
        Log.d("DetalleVM", "sincronizarTipoSeleccionado - Tipos cargados: " + (currentTipos != null ? currentTipos.size() + " elementos" : "null"));

        if (currentInmueble != null && currentTipos != null && !currentTipos.isEmpty()) {
            Optional<TipoInmueble> foundType = currentTipos.stream()
                    .filter(t -> t.getId() == currentInmueble.getTipoId())
                    .findFirst();

            if (foundType.isPresent()) {
                _tipoSeleccionadoMediator.postValue(foundType.get()); // Actualiza el MediatorLiveData
                Log.d("DetalleVM", "sincronizarTipoSeleccionado - Tipo encontrado y publicado: " + foundType.get().getNombre());
            } else {
                _tipoSeleccionadoMediator.postValue(null); // No se encontró el tipo, o manejar un default
                Log.w("DetalleVM", "sincronizarTipoSeleccionado - No se encontró el tipo con ID: " + currentInmueble.getTipoId() + " para el inmueble.");
            }
        } else {
            _tipoSeleccionadoMediator.postValue(null); // No hay datos suficientes para sincronizar
            Log.d("DetalleVM", "sincronizarTipoSeleccionado - Datos insuficientes (inmueble o tipos null/vacíos).");
        }
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

    public void procesarSeleccionImagen(ActivityResult result) {
        if (result != null && result.getData() != null && result.getData().getData() != null) {
            Uri uri = result.getData().getData();
            imagenSeleccionada.postValue(uri); // ✅ Publica la URI seleccionada
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

    // 🔹 Versión simplificada y desacoplada de la vista
    public void guardarCambios(InmuebleViewModel globalVM) {
        Inmueble actual = inmueble.getValue();
        if (actual == null) {
            mensajeToast.postValue("⚠️ No se pudo cargar el inmueble actual");
            return;
        }

        // 🔸 Usar los LiveData internos
        String dir = direccion.getValue();
        String prec = precio.getValue();
        String met = metros.getValue();
        Boolean act = activo.getValue();
        TipoInmueble tipo = _tipoSeleccionadoMediator.getValue();
        Uri uriSeleccionada = imagenSeleccionada.getValue();

        // 🔸 Validaciones básicas
        if (dir == null || dir.trim().isEmpty() ||
                prec == null || prec.trim().isEmpty() ||
                met == null || met.trim().isEmpty()) {
            mensajeToast.postValue("⚠️ Campos incompletos");
            return;
        }

        double precioDouble;
        int metrosInt;
        try {
            precioDouble = Double.parseDouble(prec.replace(",", "."));
            metrosInt = Integer.parseInt(met.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            mensajeToast.postValue("❌ Datos numéricos inválidos");
            return;
        }

        // 🧱 Crear el inmueble actualizado
        Inmueble actualizado = new Inmueble(
                actual.getId(),
                dir.trim(),
                precioDouble,
                act != null && act
        );
        actualizado.setMetrosCuadrados(metrosInt);

        // 🔸 Asignar tipo seleccionado
        if (tipo != null) {
            actualizado.setTipoId(tipo.getId());
            actualizado.setTipoNombre(tipo.getNombre());
        } else {
            actualizado.setTipoId(actual.getTipoId());
            actualizado.setTipoNombre(actual.getTipoNombre());
        }

        // 🔸 Resolver imagen final
        Uri uriFinal = uriSeleccionada != null
                ? uriSeleccionada
                : (imagenUrl.getValue() != null ? Uri.parse(imagenUrl.getValue()) : null);

        if (uriFinal == null) {
            mensajeToast.postValue("⚠️ No se seleccionó ninguna imagen");
            return;
        }

        Log.d("DetalleVM", "💾 Guardando inmueble id=" + actualizado.getId() +
                ", tipoId=" + actualizado.getTipoId() +
                ", tipoNombre=" + actualizado.getTipoNombre());

        // 🔹 Guardar mediante el repositorio
        repo.actualizarInmuebleConImagenForm(actualizado, uriFinal)
                .observeForever(ok -> {
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
                        Log.i("DetalleVM", "🏠 Inmueble actualizado en LiveData y lista global");
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
