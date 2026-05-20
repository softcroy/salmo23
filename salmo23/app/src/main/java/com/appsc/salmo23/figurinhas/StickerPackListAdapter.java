package com.appsc.salmo23.figurinhas;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation;
import androidx.core.content.ContextCompat;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StickerPackListAdapter extends RecyclerView.Adapter<StickerPackListItemViewHolder> {
    @NonNull
    private List<StickerPack> stickerPacks;
    private List<StickerPack> originalOrderList;

    StickerPackListAdapter(@NonNull List<StickerPack> stickerPacks) {
        this.stickerPacks = new ArrayList<>(stickerPacks);
        this.originalOrderList = new ArrayList<>(stickerPacks);
    }

    @NonNull
    @Override
    public StickerPackListItemViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int i) {
        final Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View stickerPackRow = layoutInflater.inflate(R.layout.item_pack, viewGroup, false);
        return new StickerPackListItemViewHolder(stickerPackRow);
    }

    @Override
    public void onBindViewHolder(@NonNull final StickerPackListItemViewHolder viewHolder, final int index) {
        StickerPack pack = stickerPacks.get(index);
        final Context context = viewHolder.container.getContext();

        // --- LÓGICA DE FAVORITOS ---
        final String uniqueKey = "fav_" + pack.identifier;
        boolean isFavorite = Hawk.get(uniqueKey, false);

        if (viewHolder.favButton != null) {
            viewHolder.favButton.setImageResource(isFavorite ? R.drawable.ic_favorite_black : R.drawable.ic_favorite_border);
            viewHolder.favButton.setOnClickListener(v -> {
                boolean currentStatus = Hawk.get(uniqueKey, false);
                boolean newState = !currentStatus;
                Hawk.put(uniqueKey, newState);

                v.setScaleX(0.7f);
                v.setScaleY(0.7f);
                v.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(300)
                        .setInterpolator(new OvershootInterpolator(3.0f))
                        .withStartAction(() -> viewHolder.favButton.setImageResource(newState ? R.drawable.ic_favorite_black : R.drawable.ic_favorite_border))
                        .withEndAction(this::sortAndNotify)
                        .start();
            });
        }

        // --- DADOS DO PACOTE ---
        viewHolder.titleView.setText(pack.name);
        viewHolder.publisherView.setText(R.string.app_name);

        if (viewHolder.downloadsView != null) viewHolder.downloadsView.setText(String.valueOf(pack.download));
        if (viewHolder.createdView != null) viewHolder.createdView.setText(pack.data);

        if (viewHolder.filesizeView != null) {
            long totalBytes = pack.getTotalSize();
            if (totalBytes <= 0 && pack.getStickers() != null) {
                totalBytes = pack.getStickers().size() * 46080L;
            }
            viewHolder.filesizeView.setText(android.text.format.Formatter.formatShortFileSize(context, totalBytes));
        }

        // --- ÍCONE PRINCIPAL ---
        Glide.with(context)
                .load(StickerPackLoader.getStickerAssetUri(pack.identifier, pack.trayImageFile))
                .placeholder(R.drawable.sticker_error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new CropCircleWithBorderTransformation(5, ContextCompat.getColor(context, R.color.grey)))
                .into(viewHolder.packTrayImage);

        viewHolder.container.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), StickerPackDetailsActivity.class);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, true);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, pack);
            view.getContext().startActivity(intent);
        });

        // --- MINIATURAS (LIMITADO A 5 PARA PERFORMANCE) ---
        int previewSize = (int) (70 * context.getResources().getDisplayMetrics().density);
        int cornerRadius = (int) (12 * context.getResources().getDisplayMetrics().density);

        List<Sticker> stickers = pack.getStickers();
        ImageView[] previewImages = {
                viewHolder.stickerOne, viewHolder.stickerTwo, viewHolder.stickerThree,
                viewHolder.stickerFour, viewHolder.stickerFive
        };

        // Número de figurinhas que vamos mostrar (no máximo 5)
        int stickersToShow = (stickers != null) ? Math.min(stickers.size(), 5) : 0;

        for (int i = 0; i < previewImages.length; i++) {
            if (i < stickersToShow) {
                previewImages[i].setVisibility(View.VISIBLE);

                Glide.with(context)
                        .load(StickerPackLoader.getStickerAssetUri(pack.identifier, stickers.get(i).imageFileName))
                        .placeholder(R.drawable.sticker_error)
                        .override(previewSize, previewSize)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .transform(new CenterInside(), new RoundedCorners(cornerRadius))
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache completo para não baixar de novo
                        .into(previewImages[i]);
            } else {
                // Importante: Limpar o ImageView e esconder se não houver figurinha
                Glide.with(context).clear(previewImages[i]);
                previewImages[i].setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return stickerPacks.size();
    }

    private void sortAndNotify() {
        Collections.sort(stickerPacks, (p1, p2) -> {
            boolean f1 = Hawk.get("fav_" + p1.identifier, false);
            boolean f2 = Hawk.get("fav_" + p2.identifier, false);
            if (f1 != f2) return f1 ? -1 : 1;
            return Integer.compare(originalOrderList.indexOf(p1), originalOrderList.indexOf(p2));
        });
        notifyDataSetChanged();
    }

    public void setStickerPackList(List<StickerPack> newList) {
        if (newList != null) {
            this.originalOrderList = new ArrayList<>(newList);
            this.stickerPacks = new ArrayList<>(newList);
            sortAndNotify();
        }
    }
}