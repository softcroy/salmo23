package com.appsc.salmo23.login;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.R;

import java.util.List;
import java.util.Random;

public class PedidoAdapterAll extends RecyclerView.Adapter<PedidoAdapterAll.PedidoViewHolder> {

    public interface OnLikeClickListener {
        void onLikeClicked(int position);
    }

    private final List<PedidoAll> listaPedidos;
    private final OnLikeClickListener likeClickListener;
    private final Context context;
    // IDs das suas novas imagens
    private final int[] imagensAmem = {R.drawable.a1, R.drawable.a2, R.drawable.b1, R.drawable.b2};

    public PedidoAdapterAll(Context context, List<PedidoAll> listaPedidos, OnLikeClickListener listener) {
        this.context = context;
        this.listaPedidos = listaPedidos;
        this.likeClickListener = listener;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedidos_all, parent, false);
        return new PedidoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        PedidoAll p = listaPedidos.get(position);
        holder.tvNomeUsuario.setText(p.getNome());
        holder.tvDataPedido.setText(p.getData());
        holder.tvPedidoTexto.setText(p.getPedido());
        holder.tvLikes.setText("Amém (" + p.getLikes() + ")");

        holder.tvPedidoTexto.setMaxLines(Integer.MAX_VALUE);
        holder.tvVerMais.setVisibility(View.GONE);

        holder.tvPedidoTexto.post(() -> {
            if (holder.tvPedidoTexto.getLineCount() > 2) {
                holder.tvVerMais.setVisibility(View.VISIBLE);
            } else {
                holder.tvVerMais.setVisibility(View.GONE);
            }
            holder.tvPedidoTexto.setMaxLines(2);
            holder.tvPedidoTexto.setEllipsize(TextUtils.TruncateAt.END);
        });

        View.OnClickListener expandir = v -> showDialog(p.getPedido());
        holder.tvVerMais.setOnClickListener(expandir);
        holder.tvPedidoTexto.setOnClickListener(expandir);
        holder.itemView.setOnClickListener(expandir);

        holder.tvLikes.setOnClickListener(v -> {
            if (likeClickListener != null) {
                animarAmem(v);
                likeClickListener.onLikeClicked(holder.getAdapterPosition());
            }
        });
    }

    /**
     * Animação atualizada: Usa ImageView e espalha os anjinhos
     */
    public void animarAmem(View viewMae) {
        if (viewMae == null) return;

        int quantidadeIcones = 10; // Aumentado para um efeito mais preenchido
        Random random = new Random();
        // Pegamos a raiz para a animação flutuar sobre outros elementos
        ViewGroup root = (ViewGroup) viewMae.getRootView();

        int[] localizacao = new int[2];
        viewMae.getLocationInWindow(localizacao);

        for (int i = 0; i < quantidadeIcones; i++) {
            final ImageView imgAnim = new ImageView(context);

            // Sorteia uma das 4 imagens (a1, a2, b1, b2)
            imgAnim.setImageResource(imagensAmem[random.nextInt(imagensAmem.length)]);

            // Tamanho variado para dar profundidade (entre 50dp e 90dp)
            int sizeDp = 50 + random.nextInt(40);
            int sizePx = (int) (sizeDp * context.getResources().getDisplayMetrics().density);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);
            imgAnim.setLayoutParams(params);

            // Posição inicial: Centro do botão clicado
            imgAnim.setX(localizacao[0] + (viewMae.getWidth() / 2f) - (sizePx / 2f));
            imgAnim.setY(localizacao[1]);

            root.addView(imgAnim);

            // Configuração do "Espalhar" (X) e "Subir" (Y)
            // translationX: vai para esquerda ou direita aleatoriamente
            float finalX = (random.nextFloat() * 600) - 300;
            // translationY: sobe entre 600 e 1000 pixels
            float finalY = -(600 + random.nextInt(400));

            imgAnim.animate()
                    .translationYBy(finalY)
                    .translationXBy(finalX)
                    .alpha(0f) // Vai sumindo
                    .rotation(random.nextInt(90) - 45) // Leve rotação aleatória
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(3000 + random.nextInt(2000)) // Velocidades diferentes
                    .setInterpolator(new DecelerateInterpolator()) // Começa rápido e para suave
                    .withEndAction(() -> root.removeView(imgAnim))
                    .start();
        }
    }

    private void showDialog(String textoCompleto) {
        TextView tv = new TextView(context);
        tv.setText(textoCompleto);
        tv.setTextSize(16f);
        tv.setPadding(40, 40, 40, 40);
        tv.setTextColor(Color.BLACK);

        new AlertDialog.Builder(context)
                .setView(tv)
                .setPositiveButton("Fechar", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return listaPedidos.size();
    }

    public static class PedidoViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNomeUsuario, tvPedidoTexto, tvDataPedido, tvLikes, tvVerMais;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeUsuario = itemView.findViewById(R.id.tvNomeUsuario);
            tvPedidoTexto = itemView.findViewById(R.id.tvPedidoTexto);
            tvDataPedido  = itemView.findViewById(R.id.tvDataPedido);
            tvLikes       = itemView.findViewById(R.id.tvLikes);
            tvVerMais     = itemView.findViewById(R.id.tvVerMais);
        }
    }
}