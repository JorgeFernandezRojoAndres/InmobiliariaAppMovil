package com.jorge.inmobiliaria2025.ui.contratos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.inmobiliaria2025.databinding.ItemContratoBinding;
import com.jorge.inmobiliaria2025.model.Contrato;
import com.jorge.inmobiliaria2025.model.Inmueble;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContratoAdapter extends RecyclerView.Adapter<ContratoAdapter.ViewHolder> {

    private List<Contrato> contratos;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Contrato contrato);
    }

    public ContratoAdapter(List<Contrato> contratos, OnItemClickListener listener) {
        this.contratos = contratos != null ? contratos : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<Contrato> nuevosContratos) {
        if (nuevosContratos == null) nuevosContratos = new ArrayList<>();

        final List<Contrato> nuevaLista = nuevosContratos;

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return contratos.size();
            }

            @Override
            public int getNewListSize() {
                return nuevaLista.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return contratos.get(oldItemPosition).getId() == nuevaLista.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Contrato oldContrato = contratos.get(oldItemPosition);
                Contrato newContrato = nuevaLista.get(newItemPosition);
                return oldContrato.getMontoMensual() == newContrato.getMontoMensual()
                        && oldContrato.getEstado().equals(newContrato.getEstado())
                        && oldContrato.getFechaInicio().equals(newContrato.getFechaInicio())
                        && oldContrato.getFechaFin().equals(newContrato.getFechaFin());
            }
        });

        contratos = nuevaLista;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemContratoBinding binding = ItemContratoBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contrato contrato = contratos.get(position);
        Inmueble inmueble = contrato.getInmueble();

        String direccion = (inmueble != null && inmueble.getDireccion() != null && !inmueble.getDireccion().isEmpty())
                ? inmueble.getDireccion()
                : "Inmueble #" + contrato.getIdInmueble();

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        holder.binding.tvDireccionContrato.setText("🏠 " + direccion);
        holder.binding.tvMontoContrato.setText("💰 " + formatoMoneda.format(contrato.getMontoMensual()));
        holder.binding.tvFechasContrato.setText(String.format("📅 %s → %s", contrato.getFechaInicio(), contrato.getFechaFin()));

        if (contrato.getEstado() != null && !contrato.getEstado().isEmpty()) {
            holder.binding.tvEstadoContrato.setVisibility(View.VISIBLE);
            holder.binding.tvEstadoContrato.setText("📋 Estado: " + contrato.getEstado());
        } else {
            holder.binding.tvEstadoContrato.setVisibility(View.GONE);
        }

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
