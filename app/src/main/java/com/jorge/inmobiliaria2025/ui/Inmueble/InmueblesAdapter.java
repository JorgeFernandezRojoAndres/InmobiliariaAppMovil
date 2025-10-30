package com.jorge.inmobiliaria2025.ui.Inmueble;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
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

    public interface OnItemClickListener {
        void onItemClick(Inmueble inmueble);
    }

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

        // ✅ Manejo seguro de URL (si viene relativa le agregamos base)
        String url = i.getImagenUrl();
        if (url != null && !url.isEmpty() && !url.startsWith("http")) {
            url = "http://10.0.2.2:5000" + url;
        }

        Glide.with(holder.itemView.getContext())
                .load(url != null ? url : R.drawable.image_background)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.image_background)
                .into(holder.ivInmueble);

        holder.swDisponible.setOnCheckedChangeListener(null);
        holder.swDisponible.setChecked(i.isDisponible());

        holder.swDisponible.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (i.isDisponible() != isChecked) {
                i.setDisponible(isChecked);

                // ✅ Notificar solo al VM, sin lógica acá
                if (disponibilidadListener != null) {
                    disponibilidadListener.onDisponibilidadChanged(i);
                }
            }
        });

        // ✅ Click delega al Fragment / ViewModel
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void actualizarLista(List<Inmueble> nuevaLista) {
        if (nuevaLista == null) return;

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return lista.size(); }

            @Override
            public int getNewListSize() { return nuevaLista.size(); }

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
