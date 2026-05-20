package com.appsc.salmo23.biblia.favorito;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.R;
import com.appsc.salmo23.biblia.ReadingActivity;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

public class FavoritoNotaFragment extends Fragment implements FavoritosAdapter.OnFavoritoClickListener {

    private RecyclerView recyclerView;
    private FavoritosAdapter adapter;
    private TextView tvEmptyState;
    private List<FavModel> listaNotas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_favoritos, container, false);
        recyclerView = v.findViewById(R.id.rvFavoritos);
        tvEmptyState = v.findViewById(R.id.tvEmptyState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarNotas();
    }

    private void carregarNotas() {
        listaNotas.clear();
        List<String> masterNotas = Hawk.get("master_notas", new ArrayList<>());

        for (String vKey : masterNotas) {
            if (Hawk.contains("nota_" + vKey)) {
                String textoVersiculo = Hawk.get("text_copy_" + vKey, "Texto não disponível");
                String notaConteudo = Hawk.get("nota_" + vKey, "");
                String data = Hawk.get("date_nota_" + vKey, "");

                // Formata a vKey (Ex: Genesis_1_15 -> Gênesis 1:15)
                String[] p = vKey.split("_");
                String referencia = p[0] + " " + p[1] + ":" + p[2];

                listaNotas.add(new FavModel(vKey, referencia, textoVersiculo, notaConteudo, data, null));
            }
        }

        if (listaNotas.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Você ainda não criou nenhuma nota.");
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }

        adapter = new FavoritosAdapter(listaNotas, "nota", this);
        recyclerView.setAdapter(adapter);
    }

    // --- CLIQUE NO LÁPIS (EDITAR TEXTO DA NOTA) ---
    @Override
    public void onEditColorClick(FavModel item) {
        abrirDialogoEdicao(item);
    }

    private void abrirDialogoEdicao(FavModel item) {
        final Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        View view = getLayoutInflater().inflate(R.layout.dialog_nota_duplo, null);
        dialog.setContentView(view);

        EditText etNota = view.findViewById(R.id.etNotaVersiculo);
        TextView tvRef = view.findViewById(R.id.tvReferenciaNota);
        TextView tvTexto = view.findViewById(R.id.tvTextoResumo);

        tvRef.setText(item.getReferencia());
        tvTexto.setText(item.getTexto());
        etNota.setText(item.getNota());

        // Ajusta teclado
        if (dialog.getWindow() != null)
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        view.findViewById(R.id.btnSalvarNota).setOnClickListener(v -> {
            String novoTexto = etNota.getText().toString().trim();
            if (!novoTexto.isEmpty()) {
                Hawk.put("nota_" + item.getVKey(), novoTexto);
                carregarNotas(); // Atualiza a lista
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "A nota não pode estar vazia", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btnCancelarNota).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // --- CLIQUE NA LIXEIRA ---
    @Override
    public void onDeleteClick(FavModel item, int position) {
        Hawk.delete("nota_" + item.getVKey());
        Hawk.delete("date_nota_" + item.getVKey());

        List<String> master = Hawk.get("master_notas", new ArrayList<>());
        master.remove(item.getVKey());
        Hawk.put("master_notas", master);

        listaNotas.remove(position);
        adapter.notifyItemRemoved(position);
        if (listaNotas.isEmpty()) tvEmptyState.setVisibility(View.VISIBLE);
    }

    // --- CLIQUE NO CARD (IR PARA A LEITURA) ---
    @Override
    public void onItemClick(FavModel item) {
        String[] partes = item.getVKey().split("_");
        if (partes.length >= 3) {
            Intent intent = new Intent(getContext(), ReadingActivity.class);
            intent.putExtra("LIVRO", partes[0]);
            intent.putExtra("CAPITULO", Integer.parseInt(partes[1]));
            intent.putExtra("VERSICULO", Integer.parseInt(partes[2]));
            startActivity(intent);
        }
    }
}