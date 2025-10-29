package com.jorge.inmobiliaria2025.ui.pagos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.model.Pago;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PagosAdapter extends RecyclerView.Adapter<PagosAdapter.ViewHolder> {

    private final Context context;
    private final List<Pago> pagos;
    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public PagosAdapter(Context context, List<Pago> pagos) {
        this.context = context;
        this.pagos = pagos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_pago, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pago p = pagos.get(position);

        // 🔹 Número de pago
        holder.tvNumero.setText(String.format(Locale.getDefault(), "Pago N° %d", p.getNumeroPago()));
        String fechaOriginal = p.getFechaPago(); // si tu modelo usa String
        String fechaFormateada = "Sin fecha";

        try {
            if (fechaOriginal != null && !fechaOriginal.isEmpty()) {
                // Parsear ISO-8601 (ej: 2025-10-22T00:00:00)
                String soloFecha = fechaOriginal.split("T")[0];
                String[] partes = soloFecha.split("-");
                // Formato a dd/MM/yyyy
                fechaFormateada = partes[2] + "/" + partes[1] + "/" + partes[0];
            }
        } catch (Exception e) {
            fechaFormateada = "Fecha inválida";
        }

        holder.tvFecha.setText("Fecha: " + fechaFormateada);


        // 🔹 Importe con formato local
        holder.tvImporte.setText("Importe: " + formatoMoneda.format(p.getImporte()));
    }

    @Override
    public int getItemCount() {
        return pagos != null ? pagos.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumero, tvFecha, tvImporte;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumero = itemView.findViewById(R.id.tvNumeroPago);
            tvFecha = itemView.findViewById(R.id.tvFechaPago);
            tvImporte = itemView.findViewById(R.id.tvImportePago);
        }
    }
}
