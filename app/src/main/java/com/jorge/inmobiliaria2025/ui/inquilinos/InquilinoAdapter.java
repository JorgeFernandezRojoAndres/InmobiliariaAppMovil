package com.jorge.inmobiliaria2025.ui.inquilinos;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.Retrofit.RetrofitClient;
import com.jorge.inmobiliaria2025.databinding.ItemInquilinoBinding;
import com.jorge.inmobiliaria2025.model.InquilinoConInmueble;

import java.util.List;

public class InquilinoAdapter extends RecyclerView.Adapter<InquilinoAdapter.ViewHolder> {

    private List<InquilinoConInmueble> lista;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int inquilinoId);
    }

    public InquilinoAdapter(List<InquilinoConInmueble> lista, OnItemClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    public void setLista(List<InquilinoConInmueble> nuevaLista) {
        lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInquilinoBinding binding = ItemInquilinoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InquilinoConInmueble inquilino = lista.get(position);

        // Mostrar nombre y apellido del inquilino
        holder.binding.txtNombreInquilino.setText(inquilino.getNombre() + " " + inquilino.getApellido());

        // Mostrar dirección del inmueble
        holder.binding.txtDireccionInquilino.setText(inquilino.getDireccionInmueble());

        // Obtener URL de la imagen del inmueble
        String url = inquilino.getImagenUrlInmueble();

        // ✅ Si la URL es relativa, la completamos con la IP base de Retrofit
        if (url != null && !url.isEmpty() && !url.startsWith("http")) {
            url = RetrofitClient.BASE_URL + (url.startsWith("/") ? url.substring(1) : url);
        }

        // Cargar la imagen con Glide
        Glide.with(holder.itemView.getContext())
                .load(url != null && !url.isEmpty() ? url : R.drawable.ic_image_placeholder)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .centerCrop()
                .into(holder.binding.imgInquilinoInmueble);

        // ✅ Configurar clic con el id correcto
        holder.binding.btnVerInquilino.setOnClickListener(v -> listener.onItemClick(inquilino.getId()));
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemInquilinoBinding binding;

        public ViewHolder(@NonNull ItemInquilinoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
