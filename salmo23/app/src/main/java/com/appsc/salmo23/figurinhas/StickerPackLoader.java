package com.appsc.salmo23.figurinhas;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.appsc.salmo23.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StickerPackLoader {

    @NonNull
    public static ArrayList<StickerPack> fetchStickerPacks(Context context) throws Exception {
        URL url = new URL("https://softcroy.com/app_santos_v2/api3/stickers/api.php");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        // Pega o package name do app atual dinamicamente
        String packageName = context.getPackageName();
        String postData = "package_name=" + packageName;

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = postData.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (InputStream inputStream = connection.getInputStream()) {
            List<StickerPack> stickerPackList = ContentFileParser.parseStickerPacks(inputStream);
            return new ArrayList<>(stickerPackList);
        } finally {
            connection.disconnect();
        }
    }

    public static Uri getStickerAssetUri(String identifier, String stickerFilename) {
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY1)
                .appendPath(StickerContentProvider.STICKERS_ASSET)
                .appendPath(identifier)
                .appendPath(stickerFilename)
                .build();
    }

    // MÉTODO QUE RESOLVE O ERRO NA VALIDAÇÃO:
    static byte[] fetchStickerAsset(@NonNull final String identifier, @NonNull final String name, ContentResolver contentResolver) throws IOException {
        try (InputStream inputStream = contentResolver.openInputStream(getStickerAssetUri(identifier, name));
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("Não foi possível abrir o stream para: " + name);
            }
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
    }
}