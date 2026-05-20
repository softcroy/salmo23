package com.appsc.salmo23.biblia;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.R;

import java.util.List;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {
    private List<Integer> items;
    private OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(int value);
    }

    public GridAdapter(List<Integer> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    // MÉTODO ESSENCIAL: Atualiza a lista sem precisar recriar o Adapter no Fragment
    public void setItems(List<Integer> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int val = items.get(position);
        holder.tv.setText(String.valueOf(val));

        // Carrega a sua fonte customizada
        Typeface customFont = ResourcesCompat.getFont(holder.itemView.getContext(), R.font.font);

        // --- LÓGICA DE CORES E ESTILO ---
        if (selectedPosition == position) {
            // DESTAQUE: Azul e Negrito
            holder.tv.setTextColor(Color.parseColor("#35AEFF"));
            holder.tv.setTypeface(customFont, Typeface.BOLD);
            holder.itemView.setBackgroundColor(Color.parseColor("#15A07844"));
        } else {
            // NORMAL: Cinza (grey1) e Fonte Normal
            holder.tv.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.grey1));
            holder.tv.setTypeface(customFont, Typeface.NORMAL);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                int previousSelected = selectedPosition;
                selectedPosition = currentPos;

                // Atualiza apenas os itens que mudaram para evitar piscadas na tela
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);

                if (listener != null) {
                    listener.onItemClick(val);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        ViewHolder(View iv) {
            super(iv);
            tv = iv.findViewById(R.id.tvGridItem); // Certifique-se que o ID no XML é este
        }
    }
}