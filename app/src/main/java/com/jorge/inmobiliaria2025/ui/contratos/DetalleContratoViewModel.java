    package com.jorge.inmobiliaria2025.ui.contratos;

    import android.app.Application;
    import android.os.Bundle;
    import android.util.Log;

    import androidx.annotation.NonNull;
    import androidx.lifecycle.AndroidViewModel;
    import androidx.lifecycle.LiveData;
    import androidx.lifecycle.MutableLiveData;

    import com.jorge.inmobiliaria2025.model.Contrato;

    public class DetalleContratoViewModel extends AndroidViewModel {

        private static final String TAG = "DetalleContratoVM";

        private final MutableLiveData<Contrato> contrato = new MutableLiveData<>();
        private final MutableLiveData<Bundle> accionNavegarAPagos = new MutableLiveData<>();

        public DetalleContratoViewModel(@NonNull Application app) {
            super(app);
            Log.d(TAG, "🧩 ViewModel creado");
        }

        // =============================
        // 🔹 Observables públicos
        // =============================
        public LiveData<Contrato> getContrato() {
            return contrato;
        }

        public LiveData<Bundle> getAccionNavegarAPagos() {
            return accionNavegarAPagos;
        }

        // =============================
        // 🔹 Inicialización desde argumentos
        // =============================
        public void inicializarDesdeArgs(Bundle args) {
            if (args == null) {
                Log.w(TAG, "⚠️ inicializarDesdeArgs() recibió args = null");
                return;
            }

            try {
                Contrato recibido = (Contrato) args.getSerializable("contratoSeleccionado");
                if (recibido != null) {
                    contrato.postValue(recibido);
                    Log.d(TAG, "✅ Contrato inicializado con ID=" + recibido.getId());
                } else {
                    Log.w(TAG, "⚠️ No se encontró 'contratoSeleccionado' en args o es null");
                }
            } catch (Exception e) {
                Log.e(TAG, "💥 Error al deserializar contrato: " + e.getMessage());
            }
        }

        // =============================
        // 🔹 Acción: navegar a pagos
        // =============================
        public void onVerPagosClick() {
            Contrato actual = contrato.getValue();

            if (actual == null) {
                Log.w(TAG, "⚠️ onVerPagosClick(): contrato actual es null, no se puede navegar");
                return;
            }

            Log.d(TAG, "➡️ Navegando a pagos del contrato ID=" + actual.getId());
            Bundle bundle = new Bundle();
            bundle.putSerializable("contratoSeleccionado", actual);
            accionNavegarAPagos.postValue(bundle);
        }

        public void limpiarAccionNavegar() {
            if (accionNavegarAPagos.getValue() != null) {
                Log.d(TAG, "🧹 limpiando accionNavegarAPagos para evitar doble navegación");
                accionNavegarAPagos.setValue(null);
            }
        }

        @Override
        protected void onCleared() {
            super.onCleared();
            Log.d(TAG, "🧩 ViewModel destruido (onCleared)");
        }
        public void cargarContratoPorId(int id) {
            Log.d(TAG, "📡 cargarContratoPorId() llamado con ID=" + id);
            // TODO: Si querés, podés implementar la recarga desde el repositorio más adelante
        }


    }
