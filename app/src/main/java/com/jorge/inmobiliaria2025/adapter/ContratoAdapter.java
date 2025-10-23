package com.jorge.inmobiliaria2025.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.inmobiliaria2025.R;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contrato, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contrato contrato = contratos.get(position);

        // 🔹 Obtener dirección del inmueble si está presente
        Inmueble inmueble = contrato.getInmueble();
        String direccion = (inmueble != null && inmueble.getDireccion() != null && !inmueble.getDireccion().isEmpty())
                ? inmueble.getDireccion()
                : "Inmueble #" + contrato.getIdInmueble();

        holder.tvDireccion.setText("🏠 " + direccion);

        // 🔹 Formato de moneda (Argentina)
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        String montoFormateado = formatoMoneda.format(contrato.getMontoMensual());
        holder.tvMonto.setText("💰 " + montoFormateado);

        // 🔹 Fechas legibles
        String fechas = String.format("📅 %s → %s", contrato.getFechaInicio(), contrato.getFechaFin());
        holder.tvFechas.setText(fechas);

        // 🔹 Estado si existe
        if (contrato.getEstado() != null && !contrato.getEstado().isEmpty()) {
            holder.tvEstado.setVisibility(View.VISIBLE);
            holder.tvEstado.setText("📋 Estado: " + contrato.getEstado());
        } else {
            holder.tvEstado.setVisibility(View.GONE);
        }

        // 🔹 Click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(contrato));
    }

    @Override
    public int getItemCount() {
        return contratos != null ? contratos.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDireccion, tvMonto, tvFechas, tvEstado;
        CardView card;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardContrato);
            tvDireccion = itemView.findViewById(R.id.tvDireccionContrato);
            tvMonto = itemView.findViewById(R.id.tvMontoContrato);
            tvFechas = itemView.findViewById(R.id.tvFechasContrato);
            tvEstado = itemView.findViewById(R.id.tvEstadoContrato);
        }
    }
}
