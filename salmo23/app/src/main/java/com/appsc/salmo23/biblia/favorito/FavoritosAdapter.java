package com.appsc.salmo23.biblia.favorito;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.R;

import java.util.List;

public class FavoritosAdapter extends RecyclerView.Adapter<FavoritosAdapter.ViewHolder> {

    private List<FavModel> list;
    private String tipo; // "cor", "nota", "marcador"
    private OnFavoritoClickListener listener;

    public interface OnFavoritoClickListener {
        void onEditColorClick(FavModel item); // Usado para editar cor (aba Cor) ou texto (aba Nota)
        void onDeleteClick(FavModel item, int position);
        void onItemClick(FavModel item);
    }

    public FavoritosAdapter(List<FavModel> list, String tipo, OnFavoritoClickListener listener) {
        this.list = list;
        this.tipo = tipo;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorito_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavModel item = list.get(position);

        holder.tvRef.setText(item.getReferencia());
        holder.tvTexto.setText(item.getTexto());
        holder.tvData.setText(item.getData());

        // RESET DE VISIBILIDADE
        holder.sideBar.setVisibility(View.GONE);
        holder.layoutNota.setVisibility(View.GONE);
        holder.circleColor.setVisibility(View.GONE);
        holder.btnEdit.setVisibility(View.GONE);

        switch (tipo) {
            case "marcador":
                // Apenas delete disponível (conforme solicitado anteriormente)
                holder.btnEdit.setVisibility(View.GONE);
                break;

            case "nota":
                // Exibe a barra lateral, o box da nota e o LÁPIS para editar
                holder.sideBar.setVisibility(View.VISIBLE);
                holder.layoutNota.setVisibility(View.VISIBLE);
                holder.tvNotaConteudo.setText(item.getNota());

                holder.btnEdit.setVisibility(View.VISIBLE);
                holder.btnEdit.setImageResource(R.drawable.ic_mode_edit); // Garante que seja o ícone de lápis
                holder.btnEdit.setOnClickListener(v -> listener.onEditColorClick(item));
                break;

            case "cor":
                // Mostra o círculo e esconde o botão de editar (lápis)
                holder.circleColor.setVisibility(View.VISIBLE);
                holder.btnEdit.setVisibility(View.GONE);

                // Define a cor atual no círculo
                String corHex = item.getCor() != null ? item.getCor() : "#E0E0E0";
                holder.circleColor.getBackground().setTint(Color.parseColor(corHex));

                // IMPORTANTE: O clique para mudar a cor agora é no próprio círculo
                holder.circleColor.setOnClickListener(v -> listener.onEditColorClick(item));
                break;
        }

        // Lixeira
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item, position));

        // Clique no card para ir ao versículo selecionado
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRef, tvTexto, tvData, tvNotaConteudo;
        View sideBar, circleColor, layoutNota;
        ImageView btnDelete, btnEdit;

        ViewHolder(View v) {
            super(v);
            tvRef = v.findViewById(R.id.tvFavReferencia);
            tvTexto = v.findViewById(R.id.tvFavTextoVersiculo);
            tvData = v.findViewById(R.id.tvFavData);
            tvNotaConteudo = v.findViewById(R.id.tvFavNotaConteudo);
            sideBar = v.findViewById(R.id.viewSideColor);
            circleColor = v.findViewById(R.id.viewCircleColor);
            layoutNota = v.findViewById(R.id.layoutNotaExtra);
            btnDelete = v.findViewById(R.id.btnDeleteFav);
            btnEdit = v.findViewById(R.id.btnEditFav);
        }
    }
}