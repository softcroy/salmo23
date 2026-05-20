package com.appsc.salmo23.biblia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.R;
import com.appsc.salmo23.biblia.favorito.FavModel;

import java.util.List;

public class SearchVersesAdapter extends RecyclerView.Adapter<SearchVersesAdapter.ViewHolder> {

    private List<FavModel> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FavModel item);
    }

    public SearchVersesAdapter(List<FavModel> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usa o nome que você definiu: item_search_card
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavModel item = items.get(position);
        
        holder.tvReference.setText(item.getReferencia());
        holder.tvText.setText(item.getTexto());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReference, tvText;

        ViewHolder(View itemView) {
            super(itemView);
            // Certifique-se que estes IDs estão no seu item_search_card.xml
            tvReference = itemView.findViewById(R.id.tvReference);
            tvText = itemView.findViewById(R.id.tvTextVerse);
        }
    }
}