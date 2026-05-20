package com.appsc.salmo23.novena;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.Assinatura;
import com.appsc.salmo23.Principal_Oracao;
import com.appsc.salmo23.R;

public class ProgressoAdapter extends RecyclerView.Adapter<ProgressoAdapter.ViewHolder> {

    private Context context;
    private Novena novena;
    private String dataInicio;
    private int diasConcluidos;
    private boolean isAssinado;

    public ProgressoAdapter(Context context, Novena novena, String dataInicio, int diasConcluidos, boolean isAssinado) {
        this.context = context;
        this.novena = novena;
        this.dataInicio = dataInicio;
        this.diasConcluidos = diasConcluidos;
        this.isAssinado = isAssinado;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dia_progresso, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int diaAtual = position + 1;
        holder.txtTitulo.setText("Dia " + diaAtual);

        int corDourada = Color.parseColor("#35AEFF");
        int corCinza = Color.parseColor("#CCCCCC");

        boolean bloqueadoPorData = NovenaActivity.estaBloqueadoPorData(dataInicio, position);
        boolean diaJaFinalizado = novena.getDatasConclusao() != null && novena.getDatasConclusao().containsKey(diaAtual);

        // --- VALIDAÇÃO DA ASSINATURA (SÓ LIMITA DO DIA 7 EM DIANTE SE JÁ TRANCOU NO DIA 6) ---
        boolean bloqueadoPorAssinatura = (diaAtual >= 7) && !isAssinado && (diasConcluidos >= 6);

        // --- LÓGICA DAS LINHAS TRACEJADAS (PROGRESSO VISUAL) ---
        if (position > 0) {
            holder.lineTop.setBackgroundTintList(ColorStateList.valueOf(position <= diasConcluidos ? corDourada : corCinza));
        }
        if (position < getItemCount() - 1) {
            holder.lineBottom.setBackgroundTintList(ColorStateList.valueOf(position < diasConcluidos ? corDourada : corCinza));
        }

        // --- LÓGICA DE ESTADOS DOS CARDS ---
        if (bloqueadoPorAssinatura) {
            // SÓ EXIBE ESTE BLOQUEIO PREMIUM SE ELE ESTIVER TRAVADO NO DIA 6
            holder.imgIndicator.setImageResource(R.drawable.ic_circle_outline);
            holder.imgIndicator.setColorFilter(corCinza);
            holder.imgAction.setImageResource(R.drawable.ic_lock);
            holder.imgAction.setVisibility(View.VISIBLE);
            holder.txtDataConclusao.setVisibility(View.GONE);
            holder.cardDia.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            holder.txtTitulo.setTextColor(Color.parseColor("#9E9E9E"));
            holder.txtSubtitulo.setTextColor(Color.parseColor("#9E9E9E"));
            holder.txtSubtitulo.setText("Premium (Disponível na assinatura)");

        } else if (position < diasConcluidos) {
            // DIA CONCLUÍDO
            holder.imgIndicator.setImageResource(R.drawable.ic_completed);
            holder.imgAction.setImageResource(R.drawable.ic_check_gold);
            holder.imgAction.setVisibility(View.VISIBLE);
            holder.cardDia.setCardBackgroundColor(Color.parseColor("#7FCFFF"));
            holder.txtTitulo.setTextColor(Color.WHITE);
            holder.txtSubtitulo.setTextColor(Color.WHITE);
            holder.txtSubtitulo.setText("Concluído");

            String dataConclusao = novena.getDataDoDia(diaAtual);
            holder.txtDataConclusao.setVisibility(dataConclusao != null ? View.VISIBLE : View.GONE);
            holder.txtDataConclusao.setText(dataConclusao);

        } else if (position == diasConcluidos && !bloqueadoPorData) {
            // DIA ATUAL DISPONÍVEL
            holder.imgIndicator.setImageResource(R.drawable.ic_circle_outline);
            holder.imgIndicator.setColorFilter(corDourada);
            holder.imgAction.setVisibility(View.GONE);
            holder.txtDataConclusao.setVisibility(View.GONE);
            holder.cardDia.setCardBackgroundColor(corDourada);
            holder.txtTitulo.setTextColor(Color.WHITE);
            holder.txtSubtitulo.setTextColor(Color.WHITE);
            holder.txtSubtitulo.setText("Toque para começar");

        } else {
            // DIA BLOQUEADO NORMAL (MANTÉM VISUAL COMUM ATÉ TERMINAR O DIA 6)
            holder.imgIndicator.setImageResource(R.drawable.ic_circle_outline);
            holder.imgIndicator.setColorFilter(corCinza);
            holder.imgAction.setImageResource(R.drawable.ic_lock);
            holder.imgAction.setVisibility(View.VISIBLE);
            holder.txtDataConclusao.setVisibility(View.GONE);
            holder.cardDia.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            holder.txtTitulo.setTextColor(Color.parseColor("#9E9E9E"));
            holder.txtSubtitulo.setTextColor(Color.parseColor("#9E9E9E"));

            if (position == diasConcluidos && bloqueadoPorData) {
                holder.txtSubtitulo.setText("Aguarde até amanhã");
            } else {
                holder.txtSubtitulo.setText("Bloqueado");
            }
        }

        // --- CLIQUE ATUALIZADO ---
        // --- CLIQUE ATUALIZADO ---
        holder.itemView.setOnClickListener(v -> {
            if (bloqueadoPorAssinatura) {
                // Redireciona diretamente para a tela de Assinatura
                Intent intent = new Intent(context, Assinatura.class);
                context.startActivity(intent);
            } else if (diasConcluidos >= 9) {
                mostrarDialogoFelicitacaoDireto();
            } else if (position > diasConcluidos) {
                Toast.makeText(context, "Conclua os dias anteriores primeiro!", Toast.LENGTH_SHORT).show();
            } else if (position == diasConcluidos && bloqueadoPorData) {
                Toast.makeText(context, "Este dia ainda não está disponível. Aguarde 24h!", Toast.LENGTH_SHORT).show();
            } else {
                if (novena.getListaRezasCompletas() != null && position < novena.getListaRezasCompletas().size()) {
                    Intent intent = new Intent(context, DetalheDiaActivity.class);
                    intent.putExtra("titulo_dia", "Dia " + diaAtual);
                    intent.putExtra("imagem_topo", novena.getImagem());
                    intent.putExtra("json_dia_completo", novena.getListaRezasCompletas().get(position));
                    intent.putExtra("id_novena", novena.getId());
                    intent.putExtra("dia_index", position);
                    intent.putExtra("ja_concluido", diaJaFinalizado);

                    if (context instanceof Activity) {
                        ((Activity) context).startActivityForResult(intent, 100);
                    } else {
                        context.startActivity(intent);
                    }
                } else if (novena.getListaRezas() != null && position < novena.getListaRezas().size()) {
                    Intent intent = new Intent(context, DetalheDiaActivity.class);
                    intent.putExtra("titulo_dia", "Dia " + diaAtual);
                    intent.putExtra("oracao_texto", novena.getListaRezas().get(position));
                    intent.putExtra("id_novena", novena.getId());
                    intent.putExtra("dia_index", position);
                    intent.putExtra("ja_concluido", diaJaFinalizado);

                    if (context instanceof Activity) {
                        ((Activity) context).startActivityForResult(intent, 100);
                    } else {
                        context.startActivity(intent);
                    }
                }
            }
        });

        holder.lineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.lineBottom.setVisibility(position == getItemCount() - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    private void mostrarDialogoFelicitacaoDireto() {
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View layout = activity.getLayoutInflater().inflate(R.layout.dialog_novena_concluida, null);
        builder.setView(layout);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        layout.findViewById(R.id.btnFecharFelicitacao).setOnClickListener(v -> {
            dialog.dismiss();

            if (Principal_Oracao.getmInstanceActivity() != null) {
                Principal_Oracao.getmInstanceActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ConcluidasFragment())
                        .commitAllowingStateLoss();

                View principalView = Principal_Oracao.getmInstanceActivity().findViewById(R.id.bottom_navigation);
                if (principalView instanceof com.google.android.material.bottomnavigation.BottomNavigationView) {
                    ((com.google.android.material.bottomnavigation.BottomNavigationView) principalView)
                            .getMenu().findItem(R.id.nav_concluidas).setChecked(true);
                }

                Principal_Oracao.getmInstanceActivity().OPENN();
            }
            activity.finish();
        });

        dialog.show();
    }

    @Override
    public int getItemCount() { return 9; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtSubtitulo, txtDataConclusao;
        ImageView imgIndicator, imgAction;
        View lineTop, lineBottom;
        CardView cardDia;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtDiaTitulo);
            txtSubtitulo = itemView.findViewById(R.id.txtSubtitulo);
            txtDataConclusao = itemView.findViewById(R.id.txtDataConclusao);
            imgIndicator = itemView.findViewById(R.id.imgIndicator);
            imgAction = itemView.findViewById(R.id.imgAction);
            lineTop = itemView.findViewById(R.id.lineTop);
            lineBottom = itemView.findViewById(R.id.lineBottom);
            cardDia = itemView.findViewById(R.id.cardDia);
        }
    }
}