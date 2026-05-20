package com.appsc.salmo23.figurinhas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

public class StickerPackValidator {

    private static final int CHAR_COUNT_MAX = 128;
    private static final long ONE_KIBIBYTE = 1024; // Corrigido para 1024 bytes padrão
    private static final int TRAY_IMAGE_FILE_SIZE_MAX_KB = 50;
    private static final int STICKER_FILE_SIZE_MAX_KB = 100;
    private static final int TRAY_IMAGE_DIMENSION_MIN = 24;
    private static final int TRAY_IMAGE_DIMENSION_MAX = 512;

    /**
     * VALIDAÇÃO LEVE (Metadata):
     * Usada na StickerPackListActivity para carregar a lista rápido.
     * Ela verifica apenas textos e o ícone pequeno do pacote (Tray).
     */
    static void verifyPackMetadataValidity(@NonNull Context context, @NonNull StickerPack stickerPack) {
        if (TextUtils.isEmpty(stickerPack.identifier)) {
            throw new IllegalStateException("O identificador do pacote está vazio.");
        }
        if (stickerPack.identifier.length() > CHAR_COUNT_MAX) {
            throw new IllegalStateException("O identificador do pacote é muito longo.");
        }
        if (TextUtils.isEmpty(stickerPack.name)) {
            throw new IllegalStateException("O nome do pacote está vazio.");
        }
        if (TextUtils.isEmpty(stickerPack.publisher)) {
            throw new IllegalStateException("O autor (publisher) do pacote está vazio.");
        }
        if (TextUtils.isEmpty(stickerPack.trayImageFile)) {
            throw new IllegalStateException("O ícone (tray) do pacote não foi definido.");
        }

        // Valida apenas o ícone de bandeja (Tray Icon)
        try {
            byte[] bytes = StickerPackLoader.fetchStickerAsset(
                    stickerPack.identifier,
                    stickerPack.trayImageFile,
                    context.getContentResolver()
            );

            if (bytes.length > TRAY_IMAGE_FILE_SIZE_MAX_KB * ONE_KIBIBYTE) {
                throw new IllegalStateException("O ícone do pacote deve ter menos de 50 KB.");
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap == null) {
                throw new IllegalStateException("Não foi possível decodificar o ícone do pacote.");
            }

            if (bitmap.getHeight() > TRAY_IMAGE_DIMENSION_MAX || bitmap.getHeight() < TRAY_IMAGE_DIMENSION_MIN ||
                    bitmap.getWidth() > TRAY_IMAGE_DIMENSION_MAX || bitmap.getWidth() < TRAY_IMAGE_DIMENSION_MIN) {
                throw new IllegalStateException("As dimensões do ícone do pacote são inválidas.");
            }

        } catch (IOException e) {
            throw new IllegalStateException("Erro ao carregar o ícone: " + stickerPack.trayImageFile, e);
        }
    }

    /**
     * VALIDAÇÃO COMPLETA:
     * Usada na StickerPackDetailsActivity.
     * Ela baixa e verifica TODAS as figurinhas do pacote antes de permitir a adição ao WhatsApp.
     */
    static void verifyStickerPackValidity(@NonNull Context context, @NonNull StickerPack stickerPack) {
        // Primeiro valida os metadados
        verifyPackMetadataValidity(context, stickerPack);

        List<Sticker> stickers = stickerPack.getStickers();
        if (stickers == null || stickers.size() < 3) {
            throw new IllegalStateException("O pacote deve ter pelo menos 3 figurinhas.");
        }
        if (stickers.size() > 30) {
            throw new IllegalStateException("O pacote não pode ter mais de 30 figurinhas.");
        }

        // Valida cada figurinha individualmente (Isso dispara o download se necessário)
        for (Sticker sticker : stickers) {
            try {
                byte[] bytes = StickerPackLoader.fetchStickerAsset(
                        stickerPack.identifier,
                        sticker.imageFileName,
                        context.getContentResolver()
                );

                if (bytes.length > STICKER_FILE_SIZE_MAX_KB * ONE_KIBIBYTE) {
                    throw new IllegalStateException("A figurinha " + sticker.imageFileName + " excede 100 KB.");
                }

                // Nota: Não decodificamos o Bitmap de todas aqui para economizar RAM,
                // apenas garantimos que o arquivo foi baixado e existe.

            } catch (IOException e) {
                throw new IllegalStateException("Erro ao baixar figurinha: " + sticker.imageFileName);
            }
        }
    }
}