package com.appsc.salmo23.login;

import android.content.Context;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.Principal_Oracao;
import com.appsc.salmo23.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    public interface OnLikeClickListener {
        void onLikeClicked(int position);
    }

    // 🔹 Nova interface para atualizar pedidos
    public interface OnPedidoExcluidoListener {
        void onPedidoExcluido();
    }

    private final List<Pedido> listaPedidos;
    private final OnLikeClickListener likeClickListener;
    private final Context context;
    private final String currentAndroidId;
    private final String packageName;

    // 🔹 Listener de exclusão
    private OnPedidoExcluidoListener pedidoExcluidoListener;
    private final int[] imagensAmem = {R.drawable.a1, R.drawable.a2, R.drawable.b1, R.drawable.b2};

    public void setOnPedidoExcluidoListener(OnPedidoExcluidoListener listener) {
        this.pedidoExcluidoListener = listener;
    }

    public PedidoAdapter(Context context, List<Pedido> listaPedidos, OnLikeClickListener listener) {
        this.context = context;
        this.listaPedidos = listaPedidos;
        this.likeClickListener = listener;
        this.currentAndroidId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        this.packageName = context.getPackageName();
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedidos, parent, false);
        return new PedidoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido p = listaPedidos.get(position);
        holder.tvNomeUsuario.setText(p.getNome());
        holder.tvDataPedido.setText(p.getData());
        holder.tvPedidoTexto.setText(p.getPedido());
        holder.tvLikes.setText("Amém (" + p.getLikes() + ")");

        holder.btnExcluir.setOnClickListener(v -> {
            String url = "https://softcroy.com/app_santos_v2/api3/login/delete_pedido.php";
            StringRequest req = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject obj = new JSONObject(response);
                            int success = obj.optInt("success");
                            if (success == 1) {
                                int pos = holder.getAdapterPosition();
                                listaPedidos.remove(pos);
                                notifyItemRemoved(pos);
                                Toast.makeText(context, "Pedido excluído com sucesso", Toast.LENGTH_SHORT).show();
                                Principal_Oracao.getmInstanceActivity().ATUALIZAR();

                                // 🔹 Notifica o fragment para atualizar lista
                                if (pedidoExcluidoListener != null) {
                                    pedidoExcluidoListener.onPedidoExcluido();
                                }

                            } else {
                                String msg = obj.optString("message", "Erro ao excluir");
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, "Resposta inválida do servidor", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(context, "Erro de rede ao excluir pedido", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("androidid", currentAndroidId);
                    params.put("app", packageName);
                    params.put("data", p.getData());
                    params.put("pedido", p.getPedido());
                    return params;
                }
            };
            Volley.newRequestQueue(context).add(req);
        });

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

    @Override
    public int getItemCount() {
        return listaPedidos.size();
    }

    public static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeUsuario, tvPedidoTexto, tvDataPedido, tvLikes;
        Button btnExcluir;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeUsuario = itemView.findViewById(R.id.tvNomeUsuario);
            tvPedidoTexto = itemView.findViewById(R.id.tvPedidoTexto);
            tvDataPedido  = itemView.findViewById(R.id.tvDataPedido);
            tvLikes       = itemView.findViewById(R.id.tvLikes);
            btnExcluir    = itemView.findViewById(R.id.btnExcluir);
        }
    }
}