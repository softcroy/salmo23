package com.appsc.salmo23.biblia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.R;
import com.orhanobut.hawk.Hawk;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class VersiculoFragment extends Fragment {

    private RecyclerView rvVersiculos;
    private GridAdapter adapter;
    private List<Integer> verseList = new ArrayList<>();

    private String livroAtual;
    private int capituloAtual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_versiculo, container, false);

        rvVersiculos = view.findViewById(R.id.rvVersiculos);
        rvVersiculos.setLayoutManager(new GridLayoutManager(getContext(), 5));

        return view;
    }

    public void carregarVersiculos(String livro, int capitulo) {
        this.livroAtual = livro;
        this.capituloAtual = capitulo;

        if (adapter != null) {
            adapter.setSelectedPosition(-1);
            verseList.clear();
            adapter.notifyDataSetChanged();
        }

        String url = "https://softcroy.com/app_santos_v2/biblia/biblia_pt_br_arc/api/api.php?action=get_verses&livro="
                + livro + "&capitulo=" + capitulo;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    verseList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            verseList.add(response.getInt(i));
                        }
                        setupAdapter();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Erro ao carregar versículos", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        if (getContext() != null) {
            // Uso de requireContext() para garantir o contexto do fragmento
            RequestQueue queue = Volley.newRequestQueue(requireContext().getApplicationContext());
            queue.add(request);
        }
    }

    private void setupAdapter() {
        if (livroAtual == null || getContext() == null) return;

        final String key = "last_verse_" + livroAtual + "_" + capituloAtual;
        int ultimoVersiculo = Hawk.get(key, 1);

        adapter = new GridAdapter(verseList, verseNumber -> {
            Hawk.put(key, verseNumber);

            // Comunicação com o Fragment Pai (BibliaFragment)
            Fragment parent = getParentFragment();
            if (parent instanceof BibliaFragment) {
                ((BibliaFragment) parent).onVerseSelected(verseNumber);
            }
        });

        int posicaoParaMarcar = 0;
        for (int i = 0; i < verseList.size(); i++) {
            if (verseList.get(i) == ultimoVersiculo) {
                posicaoParaMarcar = i;
                break;
            }
        }

        adapter.setSelectedPosition(posicaoParaMarcar);
        rvVersiculos.setAdapter(adapter);
        rvVersiculos.scrollToPosition(posicaoParaMarcar);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Verifica o estado atual através do Fragment Pai
        Fragment parent = getParentFragment();
        if (parent instanceof BibliaFragment) {
            BibliaFragment fragmentPai = (BibliaFragment) parent;
            String livro = fragmentPai.getSelectedBook();
            int capitulo = fragmentPai.getSelectedChapter();

            if (verseList.isEmpty() && livro != null && !livro.isEmpty()) {
                carregarVersiculos(livro, capitulo);
            }
        }
    }
}