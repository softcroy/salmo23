package com.appsc.salmo23.figurinhas;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class StickerPackListActivity extends Fragment {
    public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";
    private RecyclerView packRecyclerView;
    private StickerPackListAdapter allStickerPacksListAdapter;
    private LoadStickerPacksTask loadStickerPacksTask;
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    private ArrayList<StickerPack> stickerPackList = new ArrayList<>();
    private ProgressBar progressBar; // Opcional: Adicione ao seu XML app_bar_main

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myFragmentView = inflater.inflate(R.layout.app_bar_main, container, false);
        packRecyclerView = myFragmentView.findViewById(R.id.sticker_pack_list);
        progressBar = myFragmentView.findViewById(R.id.entry_ponit_loader); // Verifique se este ID existe ou remova as referências

        if (getActivity() != null && getActivity().getIntent() != null) {
            ArrayList<StickerPack> incomingList = getActivity().getIntent().getParcelableArrayListExtra(EXTRA_STICKER_PACK_LIST_DATA);
            if (incomingList != null) {
                stickerPackList = incomingList;
            }
        }

        setupRecyclerView();
        return myFragmentView;
    }

    private void setupRecyclerView() {
        allStickerPacksListAdapter = new StickerPackListAdapter(stickerPackList);
        packRecyclerView.setAdapter(allStickerPacksListAdapter);
        packRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void reloadStickerPacks() {
        if (loadStickerPacksTask != null && !loadStickerPacksTask.isCancelled()) {
            loadStickerPacksTask.cancel(true);
        }
        loadStickerPacksTask = new LoadStickerPacksTask(this);
        loadStickerPacksTask.execute();
    }

    static class LoadStickerPacksTask extends AsyncTask<Void, Void, List<StickerPack>> {
        private final WeakReference<StickerPackListActivity> activityRef;

        LoadStickerPacksTask(StickerPackListActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            StickerPackListActivity activity = activityRef.get();
            if (activity != null && activity.progressBar != null) {
                activity.progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<StickerPack> doInBackground(Void... voids) {
            try {
                StickerPackListActivity activity = activityRef.get();
                if (activity == null || activity.getContext() == null) return null;

                // 1. Busca apenas a lista básica (JSON/Metadados)
                List<StickerPack> list = StickerPackLoader.fetchStickerPacks(activity.getContext());

                // 2. Validação LEVE (Verifica apenas o Tray Icon)
                // Não entra nas figurinhas individuais aqui para manter a velocidade
                for (StickerPack pack : list) {
                    StickerPackValidator.verifyPackMetadataValidity(activity.getContext(), pack);
                }

                return list;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<StickerPack> result) {
            StickerPackListActivity activity = activityRef.get();
            if (activity == null) return;

            if (activity.progressBar != null) activity.progressBar.setVisibility(View.GONE);

            if (result != null && !result.isEmpty()) {
                activity.stickerPackList = new ArrayList<>(result);
                if (activity.allStickerPacksListAdapter != null) {
                    activity.allStickerPacksListAdapter.setStickerPackList(result);
                }
                // Após carregar, verifica o status de "Adicionado" no WhatsApp em background
                activity.checkWhiteSpace(result);
            }
        }
    }

    private void checkWhiteSpace(List<StickerPack> list) {
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
        whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
        whiteListCheckAsyncTask.execute(list.toArray(new StickerPack[0]));
    }

    static class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, List<StickerPack>> {
        private final WeakReference<StickerPackListActivity> activityReference;

        WhiteListCheckAsyncTask(StickerPackListActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected final List<StickerPack> doInBackground(StickerPack... packs) {
            StickerPackListActivity activity = activityReference.get();
            if (activity == null || activity.getContext() == null) return new ArrayList<>();

            List<StickerPack> validatedPacks = new ArrayList<>();
            for (StickerPack pack : packs) {
                // Verifica se já está no WhatsApp de forma rápida
                pack.setIsWhitelisted(WhitelistCheck.isWhitelisted(activity.getContext(), pack.identifier));
                validatedPacks.add(pack);
            }
            return validatedPacks;
        }

        @Override
        protected void onPostExecute(List<StickerPack> packs) {
            StickerPackListActivity activity = activityReference.get();
            if (activity != null && activity.allStickerPacksListAdapter != null && !packs.isEmpty()) {
                activity.allStickerPacksListAdapter.setStickerPackList(packs);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadStickerPacks();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loadStickerPacksTask != null) loadStickerPacksTask.cancel(true);
        if (whiteListCheckAsyncTask != null) whiteListCheckAsyncTask.cancel(true);
    }
}