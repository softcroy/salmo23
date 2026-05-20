package com.appsc.salmo23.figurinhas;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.R;

public class StickerPackListItemViewHolder extends RecyclerView.ViewHolder {

    View container;
    ImageView packTrayImage;
    TextView titleView;
    TextView publisherView;
    TextView filesizeView;
    ImageView favButton;
    TextView downloadsView;
    TextView createdView;

    // As 5 imagens de prévia fixas no item_pack.xml
    ImageView stickerOne, stickerTwo, stickerThree, stickerFour, stickerFive;

    StickerPackListItemViewHolder(final View itemView) {
        super(itemView);
        // O container principal para o clique é o CardView
        container = itemView.findViewById(R.id.card_view);
        // Dados do pacote
        packTrayImage = itemView.findViewById(R.id.pack_try_image);
        titleView = itemView.findViewById(R.id.item_pack_name);
        publisherView = itemView.findViewById(R.id.item_pack_publisher);
        filesizeView = itemView.findViewById(R.id.item_pack_size);
        favButton = itemView.findViewById(R.id.image_view_item_pack_fav);

        // Imagens de prévia
        stickerOne = itemView.findViewById(R.id.sticker_one);
        stickerTwo = itemView.findViewById(R.id.sticker_two);
        stickerThree = itemView.findViewById(R.id.sticker_three);
        stickerFour = itemView.findViewById(R.id.sticker_four);
        stickerFive = itemView.findViewById(R.id.sticker_five);

        downloadsView = itemView.findViewById(R.id.item_pack_downloads);
        createdView = itemView.findViewById(R.id.item_pack_created);
    }
}