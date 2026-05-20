package com.appsc.salmo23.status;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.BuildConfig;
import com.appsc.salmo23.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RecyclerViewAdapter_status extends RecyclerView.Adapter<RecyclerViewAdapter_status.ViewHolder> {

    private final Context context;
    private final List<GetDataAdapter_status> getDataAdapter;
    private static final String TAG = RecyclerViewAdapter_status.class.getSimpleName();
    private final String VIEW;

    public RecyclerViewAdapter_status(List<GetDataAdapter_status> getDataAdapter, Context context) {
        this.getDataAdapter = getDataAdapter;
        this.context = context;
        if (isEnglish()) {
            VIEW = "https://softcroy.com/app_santos_v2/api3/status/shere_image_v2_en.php?id=";
        } else {
            VIEW = "https://softcroy.com/app_santos_v2/api3/status/shere_image_v2_br.php?id=";
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_items_status, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return getDataAdapter.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        GetDataAdapter_status item = getDataAdapter.get(position);

        // Carrega imagem principal
        Glide.with(context)
                .load(item.getimage())
                .placeholder(R.drawable.maos1)
                .centerCrop() // 🔥 faz a imagem preencher o ImageView
                .into(holder.imageWallpaper);


        // --- Compartilhar imagem ---
        holder.btnShare.setOnClickListener(v -> {
            Glide.with(context)
                    .asBitmap()
                    .load(item.getimage())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            shareBitmapViaFileProvider(holder.itemView, resource, "shared_status_" + item.getId() + ".png");
                            CountVisualizacao(String.valueOf(item.getId()));
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {}
                    });
        });

        // --- Definir como papel de parede ---
        // Comportamento: FIT-CENTER (escala proporcional para caber dentro do espaço desejado, sem cortes)
        holder.btnSetWallpaper.setOnClickListener(v -> {
            Glide.with(context)
                    .asBitmap()
                    .load(item.getimage())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            WallpaperManager wm = WallpaperManager.getInstance(context);

                            try {
                                // Tamanho total da tela
                                int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                                int screenHeight = context.getResources().getDisplayMetrics().heightPixels;

                                // Ajuste para excluir status e navigation bar (altura útil)
                                int statusBarHeight = context.getResources()
                                        .getIdentifier("status_bar_height", "dimen", "android");
                                int navigationBarHeight = context.getResources()
                                        .getIdentifier("navigation_bar_height", "dimen", "android");

                                int extraTop = 0;
                                int extraBottom = 0;

                                if (statusBarHeight > 0) {
                                    extraTop = context.getResources().getDimensionPixelSize(statusBarHeight);
                                }
                                if (navigationBarHeight > 0) {
                                    extraBottom = context.getResources().getDimensionPixelSize(navigationBarHeight);
                                }

                                int usableHeight = screenHeight - (extraTop + extraBottom);

                                int originalWidth = resource.getWidth();
                                int originalHeight = resource.getHeight();

                                // Escala proporcional à altura útil
                                float scale = (float) usableHeight / (float) originalHeight;
                                int newWidth = Math.round(originalWidth * scale);
                                int newHeight = usableHeight;

                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(resource, newWidth, newHeight, true);

                                // Cria bitmap do tamanho total da tela
                                Bitmap finalBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
                                android.graphics.Canvas canvas = new android.graphics.Canvas(finalBitmap);
                                canvas.drawColor(android.graphics.Color.BLACK);

                                // Centraliza imagem e ajusta sem corte vertical
                                int left = (screenWidth - newWidth) / 2;
                                int top = extraTop; // desloca para baixo, evitando corte superior
                                canvas.drawBitmap(scaledBitmap, left, top, null);

                                wm.setBitmap(finalBitmap, null, true, WallpaperManager.FLAG_SYSTEM);

                                Toast.makeText(context, "Papel de parede definido perfeitamente!", Toast.LENGTH_SHORT).show();
                                CountVisualizacao(String.valueOf(item.getId()));

                                scaledBitmap.recycle();
                                finalBitmap.recycle();

                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Erro ao definir papel de parede", Toast.LENGTH_SHORT).show();
                            } catch (OutOfMemoryError oom) {
                                oom.printStackTrace();
                                Toast.makeText(context, "Imagem muito grande — sem memória", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {}
                    });
        });

    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageWallpaper, btnShare, btnSetWallpaper;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageWallpaper = itemView.findViewById(R.id.imageWallpaper);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnSetWallpaper = itemView.findViewById(R.id.btnSetWallpaper);
        }
    }

    // Compartilhar imagem via FileProvider
    private void shareBitmapViaFileProvider(View view, Bitmap bitmap, String filename) {
        Context ctx = view.getContext();
        try {
            File cachePath = ctx.getExternalCacheDir();
            if (cachePath == null) {
                Toast.makeText(ctx, "Cache externo indisponível", Toast.LENGTH_SHORT).show();
                return;
            }

            File dir = new File(cachePath, "image_share");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, filename);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Uri uri = FileProvider.getUriForFile(ctx, BuildConfig.APPLICATION_ID + ".provider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, ctx.getString(R.string.app_name) + " " +
                    "https://play.google.com/store/apps/details?id=" + ctx.getPackageName());
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            ctx.startActivity(Intent.createChooser(shareIntent, "Compartilhar imagem"));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ctx, "Erro ao compartilhar imagem", Toast.LENGTH_SHORT).show();
        }
    }

    // Contagem no servidor
    private void CountVisualizacao(String id) {
        String urlWithId = VIEW + id;
        RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, urlWithId, null,
                response -> {},
                error -> VolleyLog.d(TAG, "Error: " + error.getMessage()));
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjReq.setRetryPolicy(policy);
        mRequestQueue.add(jsonObjReq);
    }

    private boolean isEnglish() {
        Locale locale = context.getResources().getConfiguration().locale;
        return locale.getLanguage().equals(Locale.ENGLISH.getLanguage());
    }
}