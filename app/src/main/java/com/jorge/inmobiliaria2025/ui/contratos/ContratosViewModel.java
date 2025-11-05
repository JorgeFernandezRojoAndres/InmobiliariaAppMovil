package com.jorge.inmobiliaria2025.ui.contratos;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.localdata.SessionManager;
import com.jorge.inmobiliaria2025.model.Contrato;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContratosViewModel extends AndroidViewModel {

    private static final String TAG = "CONTRATOS_VM";

    private final MutableLiveData<List<Contrato>> contratos = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Bundle> accionNavegarADetalle = new MutableLiveData<>();

    // ✅ LiveData para mostrar mensaje de renovación
    private final MutableLiveData<String> mensajeRenovacion = new MutableLiveData<>();


    private final ContratoRepository repo;
    private final SessionManager sessionManager;
    private final MutableLiveData<Boolean> volverAlMapaEvent = new MutableLiveData<>();
    public LiveData<Boolean> getVolverAlMapaEvent() { return volverAlMapaEvent; }
    public void onVolverAlMapa() {
        volverAlMapaEvent.setValue(true);
    }

    public ContratosViewModel(@NonNull Application app) {
        super(app);
        sessionManager = SessionManager.getInstance(getApplication());
        repo = new ContratoRepository(getApplication());

        repo.getContratosLiveData().observeForever(lista -> {
            if (lista == null) {
                contratos.postValue(Collections.emptyList());
            } else {
                List<Contrato> copia = new ArrayList<>(lista);

                // Formatear las fechas antes de agregar a la lista
                for (Contrato contrato : copia) {
                    // Convertir el String de fecha a Date
                    SimpleDateFormat sdfEntrada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date fechaInicio = null;
                    Date fechaFin = null;

                    try {
                        fechaInicio = sdfEntrada.parse(contrato.getFechaInicio()); // Parsear el String a Date
                        fechaFin = sdfEntrada.parse(contrato.getFechaFin()); // Parsear el String a Date
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    // Asegurarse de que las fechas estén en formato DD/MM/YYYY
                    contrato.setFechaInicio(formatearFechaParaMostrar(fechaInicio));
                    contrato.setFechaFin(formatearFechaParaMostrar(fechaFin));
                }

                copia.sort((c1, c2) -> {
                    int estadoCompare = c1.getEstado().compareToIgnoreCase(c2.getEstado());
                    if (estadoCompare != 0) return estadoCompare;
                    return c2.getFechaInicio().compareToIgnoreCase(c1.getFechaInicio());
                });

                contratos.postValue(copia);  // Actualiza el LiveData con la lista de contratos formateada
            }
        });


    }

    public LiveData<List<Contrato>> getContratos() {
        return contratos;
    }

    public LiveData<Bundle> getAccionNavegarADetalle() {
        return accionNavegarADetalle;
    }

    public void cargarContratos() {
        String token = sessionManager.obtenerToken();
        if (token == null || token.isEmpty()) {
            contratos.postValue(Collections.emptyList());
            return;
        }
        repo.cargarContratosTodos();
    }

    public void onContratoSeleccionado(Contrato contrato) {
        if (contrato == null) return;
        Bundle bundle = new Bundle();
        bundle.putSerializable("contratoSeleccionado", contrato);
        navegarADetalle(bundle);
    }

    public void navegarADetalle(Bundle args) {
        if (args == null) {
            Log.w(TAG, "⚠️ Navegación ignorada: args == null");
            return;
        }
        accionNavegarADetalle.postValue(args);
    }

    // ✅ Renovar contrato (con conversión de Date + BigDecimal)
    public void renovarContrato(int idContrato, Date inicio, Date fin, BigDecimal monto) {
        String token = sessionManager.obtenerToken();
        if (token == null || token.isEmpty()) {
            mensajeRenovacion.postValue("Sesión expirada");
            return;
        }

        // ✅ Convertir fechas en formato DD/MM/YYYY para visualización
        String inicioFormateado = formatearFechaParaMostrar(inicio);
        String finFormateado = formatearFechaParaMostrar(fin);

        // Convertir fechas y monto antes de llamar al repo (formato 'yyyy-MM-dd' para el repositorio)
        SimpleDateFormat sdfRepo = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String inicioStr = sdfRepo.format(inicio);
        String finStr = sdfRepo.format(fin);
        String montoStr = monto.toPlainString();

        // Llamada al repositorio para renovar el contrato
        repo.renovarContrato(idContrato, inicioStr, finStr, montoStr, new ContratoRepository.CallbackRenovar() {
            @Override
            public void onSuccess(String mensaje) {
                mensajeRenovacion.postValue(mensaje != null ? mensaje : "Contrato renovado correctamente");
                cargarContratos();
            }

            @Override
            public void onError(String mensaje) {
                mensajeRenovacion.postValue(mensaje);
            }
        });

        // Mostrar las fechas formateadas en la UI (opcional, depende de la UI que estés utilizando)
        // se puede llamar a una función que muestre estas fechas formateadas
        Log.d(TAG, "Contrato renovado: Fecha inicio: " + inicioFormateado + ", Fecha fin: " + finFormateado);
    }

    // Método para convertir la fecha en formato DD/MM/YYYY
    public String formatearFechaParaMostrar(Date fecha) {
        if (fecha == null) {
            return "N/A";  // Retorna "N/A" o un valor predeterminado si la fecha es null
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(fecha);  // Formatea la fecha solo si no es null
    }

}
