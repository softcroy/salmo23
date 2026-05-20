package com.appsc.salmo23.figurinhas;

import android.util.JsonReader;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ContentFileParser {

    @NonNull
    public static List<StickerPack> parseStickerPacks(@NonNull InputStream inputStream) throws IOException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream))) {
            return readData(reader);
        }
    }

    @NonNull
    private static List<StickerPack> readData(JsonReader reader) throws IOException {
        List<StickerPack> stickerPackList = new ArrayList<>();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("sticker_packs".equals(name)) {
                reader.beginArray();
                while (reader.hasNext()) {
                    stickerPackList.add(readStickerPack(reader));
                }
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return stickerPackList;
    }

    @NonNull
    private static StickerPack readStickerPack(JsonReader reader) throws IOException {
        String identifier = "";
        String name = "";
        String publisher = "";
        String trayImageFile = "";
        String publisherEmail = "";
        String publisherWebsite = "";
        String privacyPolicyWebsite = "";
        String licenseAgreementWebsite = "";
        List<Sticker> stickers = null;

        // Novas variáveis para download e data
        int download = 0;
        String data = "";

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            switch (key) {
                case "identifier": identifier = reader.nextString(); break;
                case "name": name = reader.nextString(); break;
                case "publisher": publisher = reader.nextString(); break;
                case "tray_image_file": trayImageFile = reader.nextString(); break;
                case "publisher_email": publisherEmail = reader.nextString(); break;
                case "publisher_website": publisherWebsite = reader.nextString(); break;
                case "privacy_policy_website": privacyPolicyWebsite = reader.nextString(); break;
                case "license_agreement_website": licenseAgreementWebsite = reader.nextString(); break;
                case "stickers": stickers = readStickers(reader); break;

                // Lendo os novos campos vindos do JSON do servidor
                case "download": download = reader.nextInt(); break;
                case "data": data = reader.nextString(); break;

                default: reader.skipValue(); break;
            }
        }
        reader.endObject();

        StickerPack pack = new StickerPack(identifier, name, publisher, trayImageFile, publisherEmail, publisherWebsite, privacyPolicyWebsite, licenseAgreementWebsite);
        pack.setStickers(stickers);

        // Atribuindo os novos valores ao objeto pack
        pack.download = download;
        pack.data = data;

        return pack;
    }

    @NonNull
    private static List<Sticker> readStickers(JsonReader reader) throws IOException {
        List<Sticker> stickers = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            String imageFile = "";
            List<String> emojis = new ArrayList<>();
            long size = 0;

            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                if ("image_file".equals(key)) {
                    imageFile = reader.nextString();
                } else if ("emojis".equals(key)) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        emojis.add(reader.nextString());
                    }
                    reader.endArray();
                } else if ("size".equals(key)) {
                    size = reader.nextLong();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();

            stickers.add(new Sticker(imageFile, emojis, size));
        }
        reader.endArray();
        return stickers;
    }
}