package com.jorge.inmobiliaria2025.ui.Inmueble;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.model.Inmueble;

import java.util.ArrayList;
import java.util.List;

public class InmueblesAdapter extends RecyclerView.Adapter<InmueblesAdapter.ViewHolder> {

    private final List<Inmueble> lista = new ArrayList<>();
    private final OnItemClickListener listener;
    private final OnDisponibilidadChangeListener disponibilidadListener;

    // 🔹 Interfaz para clics
    public interface OnItemClickListener {
        void onItemClick(Inmueble inmueble);
    }

    // 🔹 Interfaz para cambios de disponibilidad
    public interface OnDisponibilidadChangeListener {
        void onDisponibilidadChanged(Inmueble inmueble);
    }

    public InmueblesAdapter(List<Inmueble> listaInicial,
                            OnItemClickListener listener,
                            OnDisponibilidadChangeListener disponibilidadListener) {
        if (listaInicial != null) this.lista.addAll(listaInicial);
        this.listener = listener;
        this.disponibilidadListener = disponibilidadListener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return lista.get(position).getId();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inmueble, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Inmueble i = lista.get(position);

        holder.tvDireccion.setText(i.getDireccion());
        holder.tvPrecio.setText(
                holder.itemView.getContext().getString(R.string.precio_formato, i.getPrecio())
        );

        // 🖼️ Imagen real o fondo por defecto
        String imgUrl = i.getImagenUrl();
        Glide.with(holder.itemView.getContext())
                .load(imgUrl != null && !imgUrl.isEmpty() ? imgUrl : R.drawable.image_background)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.ivInmueble);

        // 🔄 Evita loops por reciclado del switch
        holder.swDisponible.setOnCheckedChangeListener(null);

// 🔒 Evita null o valores incorrectos
        boolean disponible = i.isDisponible();

        holder.swDisponible.setChecked(disponible);


        holder.swDisponible.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (i.isDisponible() != isChecked) { // ✅ Evita repeticiones innecesarias
                i.setDisponible(isChecked);
                if (disponibilidadListener != null) {
                    disponibilidadListener.onDisponibilidadChanged(i);
                }
            }
        });

        // 🎯 Click en la tarjeta completa
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(i);
            } else {
                // Navegación por defecto si no hay listener externo
                Bundle bundle = new Bundle();
                bundle.putSerializable("inmueble", i);
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_inmueblesFragment_to_detalleInmuebleFragment, bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // 🔹 Actualización eficiente con DiffUtil
    public void actualizarLista(List<Inmueble> nuevaLista) {
        if (nuevaLista == null) return;

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return lista.size();
            }

            @Override
            public int getNewListSize() {
                return nuevaLista.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return lista.get(oldItemPosition).getId() == nuevaLista.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Inmueble oldItem = lista.get(oldItemPosition);
                Inmueble newItem = nuevaLista.get(newItemPosition);
                return oldItem.getDireccion().equals(newItem.getDireccion())
                        && oldItem.getPrecio() == newItem.getPrecio()
                        && oldItem.isDisponible() == newItem.isDisponible()
                        && ((oldItem.getImagenUrl() == null && newItem.getImagenUrl() == null)
                        || (oldItem.getImagenUrl() != null && oldItem.getImagenUrl().equals(newItem.getImagenUrl())));
            }
        });

        lista.clear();
        lista.addAll(nuevaLista);
        diffResult.dispatchUpdatesTo(this);
    }

    // ✅ ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivInmueble;
        TextView tvDireccion, tvPrecio;
        SwitchCompat swDisponible;

        public ViewHolder(View itemView) {
            super(itemView);
            ivInmueble = itemView.findViewById(R.id.ivInmueble);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            swDisponible = itemView.findViewById(R.id.swDisponible);
        }
    }
}
