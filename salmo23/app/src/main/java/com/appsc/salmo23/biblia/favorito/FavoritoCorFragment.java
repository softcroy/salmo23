package com.appsc.salmo23.biblia.favorito;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.R;
import com.appsc.salmo23.biblia.ReadingActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

public class FavoritoCorFragment extends Fragment implements FavoritosAdapter.OnFavoritoClickListener {

    private RecyclerView recyclerView;
    private FavoritosAdapter adapter;
    private TextView tvEmptyState;
    private List<FavModel> listaCores = new ArrayList<>();

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
        carregarCores();
    }

    private void carregarCores() {
        listaCores.clear();
        List<String> masterColors = Hawk.get("master_colors", new ArrayList<>());

        for (String vKey : masterColors) {
            String corHex = Hawk.get("color_" + vKey, "#00000000");
            if (!corHex.equals("#00000000")) {
                String texto = Hawk.get("text_copy_" + vKey, "Texto não disponível");
                String data = Hawk.get("date_color_" + vKey, "");

                String[] p = vKey.split("_");
                String referencia = p[0] + " " + p[1] + ":" + p[2];

                listaCores.add(new FavModel(vKey, referencia, texto, null, data, corHex));
            }
        }

        if (listaCores.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }

        adapter = new FavoritosAdapter(listaCores, "cor", this);
        recyclerView.setAdapter(adapter);
    }

    // --- CLIQUE NO CÍRCULO COLORIDO (MUDAR COR) ---
    @Override
    public void onEditColorClick(FavModel item) {
        abrirSeletorCores(item);
    }

    private void abrirSeletorCores(FavModel item) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(getContext());
        View view = getLayoutInflater().inflate(R.layout.layout_verse_options, null);
        bottomSheet.setContentView(view);

        // Escondemos os botões que não são de cor para este diálogo específico
        view.findViewById(R.id.btnMarcador).setVisibility(View.GONE);
        view.findViewById(R.id.btnNota).setVisibility(View.GONE);
        view.findViewById(R.id.btnCompartilhar).setVisibility(View.GONE);

        // Configura cada cor para salvar no Hawk e atualizar a lista
        configurarOpcaoCor(view.findViewById(R.id.colorYellow), item, "#FFF9C4", bottomSheet);
        configurarOpcaoCor(view.findViewById(R.id.colorGreen),  item, "#C8E6C9", bottomSheet);
        configurarOpcaoCor(view.findViewById(R.id.colorBlue),   item, "#BBDEFB", bottomSheet);
        configurarOpcaoCor(view.findViewById(R.id.colorPink),   item, "#F8BBD0", bottomSheet);
        configurarOpcaoCor(view.findViewById(R.id.colorOrange), item, "#FFE0B2", bottomSheet);
        configurarOpcaoCor(view.findViewById(R.id.colorPurple), item, "#E1BEE7", bottomSheet);

        bottomSheet.show();
    }

    private void configurarOpcaoCor(View v, FavModel item, String hex, BottomSheetDialog dialog) {
        v.setOnClickListener(view -> {
            Hawk.put("color_" + item.getVKey(), hex);
            carregarCores(); // Recarrega para refletir a nova cor
            dialog.dismiss();
        });
    }

    // --- CLIQUE NA LIXEIRA ---
    @Override
    public void onDeleteClick(FavModel item, int position) {
        Hawk.delete("color_" + item.getVKey());
        Hawk.delete("date_color_" + item.getVKey());

        List<String> master = Hawk.get("master_colors", new ArrayList<>());
        master.remove(item.getVKey());
        Hawk.put("master_colors", master);

        listaCores.remove(position);
        adapter.notifyItemRemoved(position);
    }

    // --- CLIQUE NO CARD (IR PARA LEITURA) ---
    @Override
    public void onItemClick(FavModel item) {
        String[] p = item.getVKey().split("_");
        Intent intent = new Intent(getContext(), ReadingActivity.class);
        intent.putExtra("LIVRO", p[0]);
        intent.putExtra("CAPITULO", Integer.parseInt(p[1]));
        intent.putExtra("VERSICULO", Integer.parseInt(p[2]));
        startActivity(intent);
    }
}