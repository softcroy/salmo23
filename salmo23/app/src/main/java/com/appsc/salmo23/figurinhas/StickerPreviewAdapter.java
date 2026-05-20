package com.appsc.salmo23.figurinhas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

public class StickerPreviewAdapter extends RecyclerView.Adapter<StickerPreviewViewHolder> {

    @NonNull
    private final StickerPack stickerPack;

    private final int cellSize;
    private final int cellPadding;
    private final int errorResource;
    private final LayoutInflater layoutInflater;

    StickerPreviewAdapter(
            @NonNull final LayoutInflater layoutInflater,
            final int errorResource,
            final int cellSize,
            final int cellPadding,
            @NonNull final StickerPack stickerPack) {
        this.cellSize = cellSize;
        this.cellPadding = cellPadding;
        this.layoutInflater = layoutInflater;
        this.errorResource = errorResource;
        this.stickerPack = stickerPack;
    }

    @NonNull
    @Override
    public StickerPreviewViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int i) {
        View itemView = layoutInflater.inflate(R.layout.sticker_image, viewGroup, false);
        StickerPreviewViewHolder vh = new StickerPreviewViewHolder(itemView);

        // Define o tamanho da célula para garantir o grid uniforme
        ViewGroup.LayoutParams layoutParams = vh.stickerPreviewView.getLayoutParams();
        layoutParams.height = cellSize;
        layoutParams.width = cellSize;
        vh.stickerPreviewView.setLayoutParams(layoutParams);
        vh.stickerPreviewView.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final StickerPreviewViewHolder viewHolder, final int i) {
        android.content.Context context = viewHolder.itemView.getContext();

        // Pega a URI da figurinha específica
        android.net.Uri stickerUri = StickerPackLoader.getStickerAssetUri(
                stickerPack.identifier,
                stickerPack.getStickers().get(i).imageFileName
        );

        // Arredondamento suave de 12dp (melhor visual para o grid interno que o de 25dp do ícone principal)
        int cornerRadius = (int) (12 * context.getResources().getDisplayMetrics().density);

        // Carregamento inteligente
        Glide.with(context)
                .load(stickerUri)
                .placeholder(errorResource) // Mostra o erro/loading enquanto baixa
                .override(160, 160) // Tamanho otimizado para economia de RAM
                .format(DecodeFormat.PREFER_RGB_565) // Economiza 50% de memória por imagem
                .transform(new CenterInside(), new RoundedCorners(cornerRadius))
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache total para o botão de adicionar ser instantâneo
                .into(viewHolder.stickerPreviewView);
    }

    @Override
    public int getItemCount() {
        // Nos detalhes, mostramos sempre TODAS as figurinhas do pacote
        return stickerPack.getStickers() != null ? stickerPack.getStickers().size() : 0;
    }
}