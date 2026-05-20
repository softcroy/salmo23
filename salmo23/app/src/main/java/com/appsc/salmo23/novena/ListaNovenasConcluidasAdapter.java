package com.appsc.salmo23.novena;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.appsc.salmo23.R;

import java.util.ArrayList;
import java.util.List;

public class ListaNovenasConcluidasAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private Context context;
    private List<Object> itensFormatados = new ArrayList<>();

    public ListaNovenasConcluidasAdapter(Context context, List<Novena> emAndamento, List<Novena> disponiveis) {
        this.context = context;
        if (!emAndamento.isEmpty()) {
            boolean ehHistorico = emAndamento.get(0).getAuxDiasConcluidos() >= 9;
            itensFormatados.add(ehHistorico ? "MINHAS GRAÇAS ALCANÇADAS" : "NOVENAS EM ANDAMENTO");
            itensFormatados.addAll(emAndamento);
        }
        if (!disponiveis.isEmpty()) {
            itensFormatados.add("OUTRAS NOVENAS");
            itensFormatados.addAll(disponiveis);
        }
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
            // Infla o layout específico para itens concluídos
            View v = LayoutInflater.from(context).inflate(R.layout.item_novena_concluida, parent, false);
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

            // Define o texto da causa vindo do objeto Novena
            item.causa.setText("Intenção:" + n.getAuxCausa());

            Glide.with(context)
                    .load(n.getImagem())
                    .placeholder(R.drawable.maos1)
                    .circleCrop()
                    .into(item.imagem);

            boolean isIniciado = n.getAuxDataInicio() != null && !n.getAuxDataInicio().isEmpty();
            boolean isConcluida = n.getAuxDiasConcluidos() >= 9;

            if (isConcluida) {
                // CONFIGURAÇÃO CONCLUÍDA: Ativa animação Lottie

                Context context = item.data.getContext();

                // 1. Criamos os Drawables dos ícones
                Drawable drawable1 = ContextCompat.getDrawable(context, R.drawable.day1);
                Drawable drawable9 = ContextCompat.getDrawable(context, R.drawable.day9);

                if (drawable1 != null && drawable9 != null) {
                    // Define o tamanho dos ícones (ajuste os valores se precisar deles maiores ou menores)
                    drawable1.setBounds(0, 0, drawable1.getIntrinsicWidth(), drawable1.getIntrinsicHeight());
                    drawable9.setBounds(0, 0, drawable9.getIntrinsicWidth(), drawable9.getIntrinsicHeight());

                    // 2. Montamos o texto base usando marcações (como "[icon]") para saber onde colocar as imagens
                    String textoBase = "[icon] " + n.getAuxDataInicio() + "\n[icon] " + n.getAuxDataTermino();
                    SpannableString spannable = new SpannableString(textoBase);

                    // 3. Substituímos a primeira marcação pelo ícone do Day 1
                    int primeiroIconeInicio = textoBase.indexOf("[icon]");
                    spannable.setSpan(new ImageSpan(drawable1, ImageSpan.ALIGN_BOTTOM),
                            primeiroIconeInicio, primeiroIconeInicio + 6, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                    // 4. Substituímos a segunda marcação pelo ícone do Day 9
                    int segundoIconeInicio = textoBase.lastIndexOf("[icon]");
                    spannable.setSpan(new ImageSpan(drawable9, ImageSpan.ALIGN_BOTTOM),
                            segundoIconeInicio, segundoIconeInicio + 6, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                    // Aplica o texto formatado com os ícones embutidos
                    item.data.setText(spannable);
                } else {
                    // Fallback caso os drawables falhem por algum motivo
                    item.data.setText(n.getAuxDataInicio() + "\n" + n.getAuxDataTermino());
                }

                // Limpa os CompoundDrawables antigos das laterais para não duplicar
                item.data.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                if (item.lottieItem != null) {
                    item.lottieItem.setVisibility(View.VISIBLE);
                    item.lottieItem.playAnimation();
                }
                if (item.barraCircular != null) item.barraCircular.setVisibility(View.GONE);
                if (item.txtDias != null) item.txtDias.setVisibility(View.GONE);
            } else {
                // CONFIGURAÇÃO EM ANDAMENTO: Mantém o CircularProgressBar
                item.data.setText("Iniciado em: " + n.getAuxDataInicio());
                item.data.setTextColor(Color.parseColor("#757575"));
                item.data.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                if (item.lottieItem != null) item.lottieItem.setVisibility(View.GONE);
                if (item.barraCircular != null) item.barraCircular.setVisibility(View.VISIBLE);
                if (item.txtDias != null) {
                    item.txtDias.setVisibility(View.VISIBLE);
                    item.txtDias.setText(n.getAuxDiasConcluidos() + "/9");
                }
                if (item.barraCircular != null) {
                    item.barraCircular.setProgressMax(9f);
                    item.barraCircular.setProgress((float) n.getAuxDiasConcluidos());
                }
            }

            // LÓGICA DE CLIQUE ATUALIZADA
            item.itemView.setOnClickListener(v -> {
                Intent intent;
                if (isConcluida) {
                    // Direciona para a Activity de Histórico/Leitura criada especificamente para finalizadas
                    intent = new Intent(context, NovenaActivityProgressoConcluidas.class);
                } else if (isIniciado) {
                    // Direciona para a Activity de Progresso Ativa para continuar marcando dias
                    intent = new Intent(context, NovenaActivityProgresso.class);
                } else {
                    // Direciona para a tela de Introdução caso ainda não tenha começado
                    intent = new Intent(context, NovenaActivity.class);
                }
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
        ImageView imagem;
        TextView nome, causa, data, txtDias;
        CircularProgressBar barraCircular;
        LottieAnimationView lottieItem;
        View progressoLayout;

        public ItemViewHolder(View v) {
            super(v);
            imagem = v.findViewById(R.id.imgNovenaIcon);
            nome = v.findViewById(R.id.txtNomeNovenaLista);
            causa = v.findViewById(R.id.txtCausaLista);
            data = v.findViewById(R.id.txtDataInicioLista);
            txtDias = v.findViewById(R.id.txtProgressoTexto);
            barraCircular = v.findViewById(R.id.progressCircular);
            lottieItem = v.findViewById(R.id.lottieItemConcluido);
            progressoLayout = v.findViewById(R.id.layoutProgresso);
        }
    }
}