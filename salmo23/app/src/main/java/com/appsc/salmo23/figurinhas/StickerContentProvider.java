package com.appsc.salmo23.figurinhas;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appsc.salmo23.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StickerContentProvider extends ContentProvider {
    public static final String STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier";
    public static final String STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name";
    public static final String STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher";
    public static final String STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon";
    public static final String ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link";
    public static final String IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link";
    public static final String PUBLISHER_EMAIL = "sticker_pack_publisher_email";
    public static final String PUBLISHER_WEBSITE = "sticker_pack_publisher_website";
    public static final String PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website";
    public static final String LICENSE_AGREENMENT_WEBSITE = "sticker_pack_license_agreement_website";
    public static final String STICKER_FILE_NAME_IN_QUERY = "sticker_file_name";
    public static final String STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji";

    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static final String METADATA = "metadata";
    private static final int METADATA_CODE = 1;
    private static final int METADATA_CODE_FOR_SINGLE_PACK = 2;
    static final String STICKERS = "stickers";
    private static final int STICKERS_CODE = 3;
    static final String STICKERS_ASSET = "stickers_asset";
    private static final int STICKERS_ASSET_CODE = 4;

    private List<StickerPack> stickerPackList;

    @Override
    public boolean onCreate() {
        final String authority = BuildConfig.CONTENT_PROVIDER_AUTHORITY1;
        MATCHER.addURI(authority, METADATA, METADATA_CODE);
        MATCHER.addURI(authority, METADATA + "/*", METADATA_CODE_FOR_SINGLE_PACK);
        MATCHER.addURI(authority, STICKERS + "/*", STICKERS_CODE);
        MATCHER.addURI(authority, STICKERS_ASSET + "/*/*", STICKERS_ASSET_CODE);
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int code = MATCHER.match(uri);
        if (code == METADATA_CODE) return getPackForAllStickerPacks(uri);
        if (code == METADATA_CODE_FOR_SINGLE_PACK) return getCursorForSingleStickerPack(uri);
        if (code == STICKERS_CODE) return getStickersForAStickerPack(uri);
        return null;
    }

    @Nullable
    @Override
    public AssetFileDescriptor openAssetFile(@NonNull Uri uri, @NonNull String mode) {
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() < 3) return null;

        String identifier = pathSegments.get(pathSegments.size() - 2);
        String fileName = pathSegments.get(pathSegments.size() - 1);

        File cacheDir = new File(getContext().getCacheDir(), "stickers/" + identifier);
        if (!cacheDir.exists()) cacheDir.mkdirs();

        File file = new File(cacheDir, fileName);

        try {
            if (!file.exists()) {
                URL url = new URL("https://softcroy.com/app_santos_v2/api3/stickers/uploads/" + identifier + "/" + fileName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (InputStream is = connection.getInputStream();
                         FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = is.read(buffer)) != -1) fos.write(buffer, 0, read);
                    }
                }
                connection.disconnect();
            }
            return new AssetFileDescriptor(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY), 0, -1);
        } catch (IOException e) {
            Log.e("StickerProvider", "Erro ao baixar arquivo: " + identifier + "/" + fileName, e);
            return null;
        }
    }

    private List<StickerPack> getStickerPackList() {
        // Recarregar sempre que a lista estiver nula ou vazia para garantir sincronia com a API
        if (stickerPackList == null || stickerPackList.isEmpty()) {
            try {
                stickerPackList = StickerPackLoader.fetchStickerPacks(getContext());
            } catch (Exception e) {
                stickerPackList = new ArrayList<>();
            }
        }
        return stickerPackList;
    }

    private Cursor getPackForAllStickerPacks(@NonNull Uri uri) {
        return getStickerPackInfo(uri, getStickerPackList());
    }

    private Cursor getCursorForSingleStickerPack(@NonNull Uri uri) {
        final String identifier = uri.getLastPathSegment();
        for (StickerPack stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.identifier)) {
                return getStickerPackInfo(uri, Collections.singletonList(stickerPack));
            }
        }
        return getStickerPackInfo(uri, new ArrayList<>());
    }

    @NonNull
    private Cursor getStickerPackInfo(@NonNull Uri uri, @NonNull List<StickerPack> stickerPackList) {
        MatrixCursor cursor = new MatrixCursor(new String[]{
                STICKER_PACK_IDENTIFIER_IN_QUERY, STICKER_PACK_NAME_IN_QUERY, STICKER_PACK_PUBLISHER_IN_QUERY,
                STICKER_PACK_ICON_IN_QUERY, ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                PUBLISHER_EMAIL, PUBLISHER_WEBSITE, PRIVACY_POLICY_WEBSITE, LICENSE_AGREENMENT_WEBSITE
        });

        // Gera o link da Play Store automaticamente usando o pacote atual do app
        String playStoreLink = "https://play.google.com/store/apps/details?id=" + getContext().getPackageName();

        for (StickerPack stickerPack : stickerPackList) {
            cursor.addRow(new Object[]{
                    stickerPack.identifier,
                    stickerPack.name,
                    stickerPack.publisher,
                    stickerPack.trayImageFile,
                    playStoreLink, // Valor setado dinamicamente aqui
                    stickerPack.iosAppStoreLink,
                    stickerPack.publisherEmail,
                    stickerPack.publisherWebsite,
                    stickerPack.privacyPolicyWebsite,
                    stickerPack.licenseAgreementWebsite
            });
        }
        return cursor;
    }

    @NonNull
    private Cursor getStickersForAStickerPack(@NonNull Uri uri) {
        final String identifier = uri.getLastPathSegment();
        MatrixCursor cursor = new MatrixCursor(new String[]{STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY});
        for (StickerPack stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.identifier)) {
                for (Sticker sticker : stickerPack.getStickers()) {
                    cursor.addRow(new Object[]{sticker.imageFileName, TextUtils.join(",", sticker.emojis)});
                }
            }
        }
        return cursor;
    }

    @Override public String getType(@NonNull Uri uri) { return null; }
    @Override public int delete(@NonNull Uri uri, String s, String[] sa) { return 0; }
    @Override public Uri insert(@NonNull Uri uri, ContentValues v) { return null; }
    @Override public int update(@NonNull Uri uri, ContentValues v, String s, String[] sa) { return 0; }
}