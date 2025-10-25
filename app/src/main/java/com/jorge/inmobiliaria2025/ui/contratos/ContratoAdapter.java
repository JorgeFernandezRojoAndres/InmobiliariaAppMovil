package com.jorge.inmobiliaria2025.ui.contratos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.databinding.ItemContratoBinding;
import com.jorge.inmobiliaria2025.model.Contrato;
import com.jorge.inmobiliaria2025.model.Inmueble;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContratoAdapter extends RecyclerView.Adapter<ContratoAdapter.ViewHolder> {

    private List<Contrato> contratos; // ✅ ahora mutable
    private final OnItemClickListener listener;

    // 🔹 Permite actualizar la lista dinámicamente desde el ViewModel
    public void updateData(List<Contrato> nuevosContratos) {
        this.contratos = nuevosContratos != null ? nuevosContratos : new ArrayList<>();
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Contrato contrato);
    }

    public ContratoAdapter(List<Contrato> contratos, OnItemClickListener listener) {
        this.contratos = contratos != null ? contratos : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 🔹 Usando ViewBinding para item_contrato.xml
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemContratoBinding binding = ItemContratoBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contrato contrato = contratos.get(position);

        // 🔹 Obtener dirección del inmueble si está presente
        Inmueble inmueble = contrato.getInmueble();
        String direccion = (inmueble != null && inmueble.getDireccion() != null && !inmueble.getDireccion().isEmpty())
                ? inmueble.getDireccion()
                : "Inmueble #" + contrato.getIdInmueble();

        holder.binding.tvDireccionContrato.setText("🏠 " + direccion);

        // 🔹 Formato de moneda (Argentina)
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        String montoFormateado = formatoMoneda.format(contrato.getMontoMensual());
        holder.binding.tvMontoContrato.setText("💰 " + montoFormateado);

        // 🔹 Fechas legibles
        String fechas = String.format("📅 %s → %s", contrato.getFechaInicio(), contrato.getFechaFin());
        holder.binding.tvFechasContrato.setText(fechas);

        // 🔹 Estado si existe
        if (contrato.getEstado() != null && !contrato.getEstado().isEmpty()) {
            holder.binding.tvEstadoContrato.setVisibility(View.VISIBLE);
            holder.binding.tvEstadoContrato.setText("📋 Estado: " + contrato.getEstado());
        } else {
            holder.binding.tvEstadoContrato.setVisibility(View.GONE);
        }

        // 🔹 Click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(contrato));
    }

    @Override
    public int getItemCount() {
        return contratos != null ? contratos.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemContratoBinding binding;

        ViewHolder(@NonNull ItemContratoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
