package com.appsc.salmo23.santos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.R;
import com.appsc.salmo23.novos.Play;
import com.bumptech.glide.Glide;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation;

public class RecyclerViewAdapter_oracoes_santos extends RecyclerView.Adapter<RecyclerViewAdapter_oracoes_santos.ViewHolder> {
    private Context context;
    private List<GetDataAdapter_oracoes_santos> getDataAdapter;

    public RecyclerViewAdapter_oracoes_santos(List<GetDataAdapter_oracoes_santos> getDataAdapter, Context context) {
        super();
        this.getDataAdapter = getDataAdapter;
        this.context = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_identificador_oracoes, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return getDataAdapter.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView id;
        public TextView identificador;
        public TextView nome;
        public TextView icone;
        public TextView image;
        public TextView oracao;
        public TextView sond;
        public TextView visualizacao;
        public TextView data;
        public ImageView image1;
        public CardView abrir;
        public ImageView favoriteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            id             = itemView.findViewById(R.id.id);
            identificador  = itemView.findViewById(R.id.identificador);
            nome           = itemView.findViewById(R.id.nome);
            icone          = itemView.findViewById(R.id.icone);
            image          = itemView.findViewById(R.id.image);
            oracao         = itemView.findViewById(R.id.oracao);
            sond           = itemView.findViewById(R.id.sond);
            visualizacao   = itemView.findViewById(R.id.visualizacao);
            data           = itemView.findViewById(R.id.data);

            image1          = itemView.findViewById(R.id.imagefeed);
            abrir           = itemView.findViewById(R.id.cardview1);
            favoriteButton  = itemView.findViewById(R.id.favoriteButton);

            abrir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    GetDataAdapter_oracoes_santos item = getDataAdapter.get(position);
                    Intent intent = new Intent(context, Play.class);
                    intent.putExtra("Oracao",       item.getoracao());
                    intent.putExtra("Image",        item.getimage());
                    intent.putExtra("Sond",         item.getsond());
                    intent.putExtra("NomeOracao",   item.getnome());
                    intent.putExtra("Id_oracao",    item.getid());
                    context.startActivity(intent);
                }
            });


        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        GetDataAdapter_oracoes_santos getDataAdapter1 = getDataAdapter.get(position);
        Glide.with(context)
                .load(getDataAdapter1.geticone())
                .transform(new CropCircleWithBorderTransformation(5, ContextCompat.getColor(context, R.color.grey)))
                .into(holder.image1);
        holder.id            .setText(getDataAdapter1.getid());
        holder.identificador .setText(getDataAdapter1.getidentificador());
        holder.nome          .setText(getDataAdapter1.getnome());
        holder.icone         .setText(getDataAdapter1.geticone());
        holder.image         .setText(getDataAdapter1.getimage());
        holder.oracao        .setText(getDataAdapter1.getoracao());
        holder.sond          .setText(getDataAdapter1.getsond());
        holder.data          .setText(getDataAdapter1.getdata());
    }
}