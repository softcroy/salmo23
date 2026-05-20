package com.appsc.salmo23.biblia.favorito;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritoMarcadorFragment extends Fragment implements FavoritosAdapter.OnFavoritoClickListener {

    private RecyclerView recyclerView;
    private FavoritosAdapter adapter;
    private TextView tvEmptyState;
    private List<FavModel> listaMarcadores = new ArrayList<>();

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
        carregarMarcadoresSalvos();
    }

    private void carregarMarcadoresSalvos() {
        listaMarcadores.clear();

        // Lê a lista mestra global de marcadores (populada na ReadingActivity)
        List<String> masterMarcadores = Hawk.get("master_marcadores", new ArrayList<>());

        for (String vKey : masterMarcadores) {
            // Detalhes salvos para o card
            String data = Hawk.get("date_marcador_" + vKey, "Sem data");
            String textoVersiculo = Hawk.get("text_copy_" + vKey, "Toque para ler o versículo...");
            String corHex = Hawk.get("color_" + vKey, "#00000000");

            // --- CORREÇÃO DA REFERÊNCIA: Gênesis_1_2 -> Gênesis 1:2 ---
            String[] partes = vKey.split("_");
            String referencia = "";
            if (partes.length >= 3) {
                // Monta Livro + Espaço + Capítulo + Dois Pontos + Versículo
                referencia = partes[0] + " " + partes[1] + ":" + partes[2];
            } else {
                referencia = vKey.replace("_", " ");
            }

            listaMarcadores.add(new FavModel(vKey, referencia, textoVersiculo, null, data, corHex));
        }

        if (listaMarcadores.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Nenhum marcador salvo.");
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }

        // Tipo "marcador" configurado conforme sua solicitação anterior (apenas delete)
        adapter = new FavoritosAdapter(listaMarcadores, "marcador", this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onEditColorClick(FavModel item) {
        // No marcador o ícone de editar está oculto conforme solicitado,
        // mas o método permanece aqui por causa da interface.
    }

    @Override
    public void onDeleteClick(FavModel item, int position) {
        String vKey = item.getVKey();

        // 1. Remove da lista mestra global
        List<String> master = Hawk.get("master_marcadores", new ArrayList<>());
        master.remove(vKey);
        Hawk.put("master_marcadores", master);

        // 2. Remove da lista específica do livro (usada na ReadingActivity)
        String livro = vKey.split("_")[0];
        Set<String> marcadoresLivro = Hawk.get("marcadores_" + livro, new HashSet<>());
        marcadoresLivro.remove(vKey);
        Hawk.put("marcadores_" + livro, marcadoresLivro);

        // 3. Remove a data associada
        Hawk.delete("date_marcador_" + vKey);

        // Atualiza a UI
        listaMarcadores.remove(position);
        adapter.notifyItemRemoved(position);

        if (listaMarcadores.isEmpty()) tvEmptyState.setVisibility(View.VISIBLE);

        Toast.makeText(getContext(), "Marcador removido", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(FavModel item) {
        // Navegação para a ReadingActivity
        String[] partes = item.getVKey().split("_");
        if (partes.length >= 3) {
            String livro = partes[0];
            int capitulo = Integer.parseInt(partes[1]);
            int versiculo = Integer.parseInt(partes[2]);

            Intent intent = new Intent(getContext(), ReadingActivity.class);
            intent.putExtra("LIVRO", livro);
            intent.putExtra("CAPITULO", capitulo);
            intent.putExtra("VERSICULO", versiculo);
            startActivity(intent);
        }
    }
}