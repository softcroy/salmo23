package com.appsc.salmo23.novos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator; // Importante para o efeito mola
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.R;
import com.bumptech.glide.Glide;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.NoEncryption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation;

public class TodasOracoes_RecyclerViewAdapter extends RecyclerView.Adapter<TodasOracoes_RecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<Oracoes_GetDataAdapter> getDataAdapter;
    private List<Oracoes_GetDataAdapter> originalOrderList;

    public TodasOracoes_RecyclerViewAdapter(List<Oracoes_GetDataAdapter> getDataAdapter, Context context) {
        super();
        this.getDataAdapter = new ArrayList<>(getDataAdapter);
        this.originalOrderList = new ArrayList<>(getDataAdapter);
        this.context = context;

        if (!Hawk.isBuilt()) {
            Hawk.init(context).setEncryption(new NoEncryption()).build();
        }

        sortAndNotify();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_todas_oracoes, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return getDataAdapter.size();
    }

    private void sortAndNotify() {
        Collections.sort(getDataAdapter, (o1, o2) -> {
            boolean f1 = Hawk.get("fav_oracao_" + o1.getid(), false);
            boolean f2 = Hawk.get("fav_oracao_" + o2.getid(), false);

            if (f1 != f2) {
                return f1 ? -1 : 1;
            }
            return Integer.compare(originalOrderList.indexOf(o1), originalOrderList.indexOf(o2));
        });
        notifyDataSetChanged();
    }

    public void updateList(List<Oracoes_GetDataAdapter> newList) {
        if (newList != null) {
            this.originalOrderList = new ArrayList<>(newList);
            this.getDataAdapter = new ArrayList<>(newList);
            sortAndNotify();
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Oracoes_GetDataAdapter item = getDataAdapter.get(position);
        final String uniqueKey = "fav_oracao_" + item.getid();

        // Define o estado inicial do ícone
        boolean isFavorite = Hawk.get(uniqueKey, false);
        holder.favoriteButton.setImageResource(isFavorite ? R.drawable.ic_favorite_black : R.drawable.ic_favorite_border);

        // Clique no Favorito com Efeito Instagram
        holder.favoriteButton.setOnClickListener(v -> {
            boolean currentFav = Hawk.get(uniqueKey, false);
            boolean newState = !currentFav;
            Hawk.put(uniqueKey, newState);

            // ANIMAÇÃO DE PULSO (Estilo Instagram)
            v.setScaleX(0.7f); // Começa menor
            v.setScaleY(0.7f);

            v.animate()
                    .scaleX(1.0f) // Volta ao tamanho normal
                    .scaleY(1.0f)
                    .setDuration(300)
                    .setInterpolator(new OvershootInterpolator(3.0f)) // O segredo da "pulsada" está aqui
                    .withStartAction(() -> {
                        // Muda o ícone exatamente no início da animação
                        holder.favoriteButton.setImageResource(newState ? R.drawable.ic_favorite_black : R.drawable.ic_favorite_border);
                    })
                    .withEndAction(() -> {
                        // Só reordena a lista após a animação terminar para não travar o efeito visual
                        sortAndNotify();
                    })
                    .start();
        });

        // Clique para Abrir
        holder.abrir.setOnClickListener(v -> {
            Intent intent = new Intent(context, Play.class);
            intent.putExtra("Oracao", item.getoracao());
            intent.putExtra("Image", item.getimage());
            intent.putExtra("Sond", item.getsond());
            intent.putExtra("NomeOracao", item.getnome());
            intent.putExtra("Id_oracao", item.getid());
            context.startActivity(intent);
        });

        // Glide e preenchimento de textos
        Glide.with(context)
                .load(item.geticone())
                .transform(new CropCircleWithBorderTransformation(5, ContextCompat.getColor(context, R.color.grey)))
                .into(holder.image1);

        holder.id.setText(item.getid());
        holder.identificador.setText(item.getidentificador());
        holder.nome.setText(item.getnome());
        holder.data.setText(item.getdata());
        holder.icone.setText(item.geticone());
        holder.image.setText(item.getimage());
        holder.oracao.setText(item.getoracao());
        holder.sond.setText(item.getsond());
        holder.visualizacao.setText(item.getvisualizacao());
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView id, identificador, nome, icone, image, oracao, sond, visualizacao, data;
        public ImageView image1, favoriteButton;
        public CardView abrir;

        public ViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.id);
            identificador = itemView.findViewById(R.id.identificador);
            nome = itemView.findViewById(R.id.nome);
            icone = itemView.findViewById(R.id.icone);
            image = itemView.findViewById(R.id.image);
            oracao = itemView.findViewById(R.id.oracao);
            sond = itemView.findViewById(R.id.sond);
            visualizacao = itemView.findViewById(R.id.visualizacao);
            data = itemView.findViewById(R.id.data);
            image1 = itemView.findViewById(R.id.imagefeed);
            abrir = itemView.findViewById(R.id.cardview1);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
}