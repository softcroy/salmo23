package com.appsc.salmo23.novena;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.NoEncryption;
import com.appsc.salmo23.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListaNovenasAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private Context context;
    private List<Object> itensFormatados = new ArrayList<>();

    // Mantemos referências das listas originais para poder reordenar em tempo real
    private List<Novena> listaEmAndamento;
    private List<Novena> listaDisponiveis;

    public ListaNovenasAdapter(Context context, List<Novena> emAndamento, List<Novena> disponiveis) {
        this.context = context;
        this.listaEmAndamento = emAndamento;
        this.listaDisponiveis = disponiveis;

        if (!Hawk.isBuilt()) {
            Hawk.init(context).setEncryption(new NoEncryption()).build();
        }

        gerarItensFormatados();
    }

    // Centraliza a lógica de criação da lista com a ordem correta
    private void gerarItensFormatados() {
        itensFormatados.clear();

        if (!listaEmAndamento.isEmpty()) {
            boolean ehHistorico = listaEmAndamento.get(0).getAuxDiasConcluidos() >= 9;
            itensFormatados.add(ehHistorico ? "MINHAS GRAÇAS ALCANÇADAS" : "MINHAS NOVENAS");

            // Opcional: ordenar favoritos também em andamento
            ordenarListaPorFavorito(listaEmAndamento);
            itensFormatados.addAll(listaEmAndamento);
        }

        if (!listaDisponiveis.isEmpty()) {
            itensFormatados.add("TODAS NOVENAS");
            ordenarListaPorFavorito(listaDisponiveis);
            itensFormatados.addAll(listaDisponiveis);
        }
    }

    private void ordenarListaPorFavorito(List<Novena> lista) {

        Collections.sort(lista, (n1, n2) -> {

            boolean f1 = Hawk.get("fav_novena_" + n1.getId(), false);
            boolean f2 = Hawk.get("fav_novena_" + n2.getId(), false);

            // Favoritos sobem
            if (f1 != f2) {
                return f1 ? -1 : 1;
            }

            // NÃO favoritos voltam para posição original do JSON
            return Integer.compare(
                    n1.getOrdemOriginal(),
                    n2.getOrdemOriginal()
            );
        });
    }

    @Override
    public int getItemViewType(int position) {
        return (itensFormatados.get(position) instanceof String) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(context).inflate(R.layout.item_novena_lista, parent, false);
            return new ItemViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder h = (HeaderViewHolder) holder;
            h.txtTitulo.setText((String) itensFormatados.get(position));
        } else if (holder instanceof ItemViewHolder) {
            Novena n = (Novena) itensFormatados.get(position);
            ItemViewHolder item = (ItemViewHolder) holder;

            item.nome.setText(n.getNome());

            // Lógica de Favoritos com Movimentação para cima
            final String favKey = "fav_novena_" + n.getId();
            boolean isFavorite = Hawk.get(favKey, false);

            if (item.btnFavorito != null) {
                item.btnFavorito.setImageResource(isFavorite ? R.drawable.ic_favorite_black : R.drawable.ic_favorite_border);

                item.btnFavorito.setOnClickListener(v -> {
                    boolean newState = !Hawk.get(favKey, false);
                    Hawk.put(favKey, newState);

                    // Animação de pulso
                    v.setScaleX(0.7f);
                    v.setScaleY(0.7f);
                    v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(300)
                            .setInterpolator(new OvershootInterpolator(3.0f))
                            .withStartAction(() -> {
                                item.btnFavorito.setImageResource(newState ? R.drawable.ic_favorite_black : R.drawable.ic_favorite_border);
                            })
                            .withEndAction(() -> {
                                // REORDENA E MOVE PARA CIMA APÓS A ANIMAÇÃO
                                gerarItensFormatados();
                                notifyDataSetChanged();
                            })
                            .start();
                });
            }

            // --- Manutenção do restante da lógica original ---
            if (item.padroeiro != null) item.padroeiro.setText(n.getPadroeiro());
            if (item.txtDiaFesta != null) {
                String festa = n.getDiaFesta();
                item.txtDiaFesta.setText((festa != null && !festa.isEmpty()) ? "Festa: " + festa : "");
            }

            Glide.with(context).load(n.getImagem()).placeholder(R.drawable.maos1).circleCrop().into(item.imagem);

            boolean isIniciado = n.getAuxDataInicio() != null && !n.getAuxDataInicio().isEmpty();
            boolean isConcluida = n.getAuxDiasConcluidos() >= 9;

            if (isIniciado || isConcluida) {
                item.causa.setVisibility(View.VISIBLE);
                item.progressoLayout.setVisibility(View.VISIBLE);
                if (item.padroeiro != null) item.padroeiro.setVisibility(View.GONE);
                if (item.txtDiaFesta != null) {

    item.txtDiaFesta.setVisibility(View.VISIBLE);

    // remove ícone drawable
    item.txtDiaFesta.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

    // define tamanho do texto
    item.txtDiaFesta.setTextSize(10);

    String dataInicio = n.getAuxDataInicio();

    if (dataInicio != null && !dataInicio.isEmpty()) {
        item.txtDiaFesta.setText("Iniciada em: " + dataInicio);
    } else {
        item.txtDiaFesta.setText("Novena concluída");
    }
}
                item.causa.setText("Intenção: " + ((n.getAuxCausa() == null || n.getAuxCausa().isEmpty()) ? "Minha Intenção" : n.getAuxCausa()));

                if (isConcluida) {
                    if (item.lottieItem != null) { item.lottieItem.setVisibility(View.VISIBLE); item.lottieItem.playAnimation(); }
                    if (item.barraCircular != null) item.barraCircular.setVisibility(View.GONE);
                    if (item.txtDias != null) item.txtDias.setVisibility(View.GONE);
                } else {
                    if (item.lottieItem != null) item.lottieItem.setVisibility(View.GONE);
                    if (item.barraCircular != null) {
                        item.barraCircular.setVisibility(View.VISIBLE);
                        item.barraCircular.setProgressMax(9f);
                        item.barraCircular.setProgress((float) n.getAuxDiasConcluidos());
                    }
                    if (item.txtDias != null) {
                        item.txtDias.setVisibility(View.VISIBLE);
                        item.txtDias.setText(n.getAuxDiasConcluidos() + "/9");
                    }
                }
            } else {
                item.progressoLayout.setVisibility(View.GONE);
                item.causa.setVisibility(View.GONE);
                if (item.padroeiro != null) item.padroeiro.setVisibility(View.VISIBLE);
                if (item.txtDiaFesta != null) item.txtDiaFesta.setVisibility(View.VISIBLE);
            }

            item.itemView.setOnClickListener(v -> {
                Intent intent = (isIniciado || isConcluida) ? new Intent(context, NovenaActivityProgresso.class) : new Intent(context, NovenaActivity.class);
                intent.putExtra("novena_selecionada", n);
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() { return itensFormatados.size(); }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo;
        public HeaderViewHolder(View v) { super(v); txtTitulo = v.findViewById(android.R.id.text1); }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imagem, btnFavorito;
        TextView nome, causa, txtDias, padroeiro, txtDiaFesta;
        CircularProgressBar barraCircular;
        LottieAnimationView lottieItem;
        View progressoLayout;

        public ItemViewHolder(View v) {
            super(v);
            imagem = v.findViewById(R.id.imgNovenaIcon);
            nome = v.findViewById(R.id.txtNomeNovenaLista);
            causa = v.findViewById(R.id.txtCausaLista);
            padroeiro = v.findViewById(R.id.txtPadroeiroLista);
            txtDiaFesta = v.findViewById(R.id.txtDiaFestaLista);
            txtDias = v.findViewById(R.id.txtProgressoTexto);
            barraCircular = v.findViewById(R.id.progressCircular);
            lottieItem = v.findViewById(R.id.lottieItemConcluido);
            progressoLayout = v.findViewById(R.id.layoutProgresso);
            btnFavorito = v.findViewById(R.id.favoriteButton);
        }
    }
}