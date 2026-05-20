package com.appsc.salmo23;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.NoEncryption;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Oracoes extends Fragment {

    private MediaPlayer mPlayer = null;    // Áudio da oração (voz)
    private MediaPlayer bgMediaPlayer = null; // Música de fundo

    private ToggleButton play1;
    private TextView oracao;
    private Button sha;

    // Variáveis da Música de Fundo (tgMusic)
    private float musicVolume = 0.5f;
    private List<String> musicUrls = new ArrayList<>();
    private List<String> musicNames = new ArrayList<>();
    private ToggleButton tgMusic;
    private LottieAnimationView lottieMusic;
    private View bgCircle;
    private ImageView imgMusicalNote;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.oracoes1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicialização do Hawk sem criptografia
        if (!Hawk.isBuilt()) {
            Hawk.init(requireContext()).setEncryption(new NoEncryption()).build();
        }

        oracao = view.findViewById(R.id.oracao);
        play1 = view.findViewById(R.id.play1);
        sha = view.findViewById(R.id.nao);

        Typeface tf = Typeface.createFromAsset(requireActivity().getAssets(), "fonts/font.ttf");
        setupOracao(getString(R.string.oracao1), tf);

        // Configuração do tgMusic e Música de Fundo
        configurarMusicaFundo(view);

        sha.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.oracao1) + "  " + getString(R.string.app_name) + " " + "https://play.google.com/store/apps/details?id=" + requireActivity().getPackageName());
            startActivity(Intent.createChooser(shareIntent, "Compartilhar"));
        });

        play1.setOnClickListener(v -> {
            if (play1.isChecked()) {
                playy1();
            } else {
                stopOração();
            }
        });
    }

    private void configurarMusicaFundo(View view) {
        bgCircle = view.findViewById(R.id.bgCircle);
        lottieMusic = view.findViewById(R.id.lottieMusic);
        imgMusicalNote = view.findViewById(R.id.imgMusicalNote);
        tgMusic = view.findViewById(R.id.tgMusic);

        boolean isEnabled = Hawk.get("bg_music_enabled", true);
        tgMusic.setChecked(isEnabled);
        musicVolume = Hawk.get("bg_music_vol", 50) / 100f;

        if (isEnabled) {
            bgCircle.setVisibility(View.GONE);
            lottieMusic.setVisibility(View.VISIBLE);
            imgMusicalNote.setVisibility(View.VISIBLE);
            lottieMusic.playAnimation();
        }

        fetchMusicList(() -> {
            if (tgMusic.isChecked() && !musicUrls.isEmpty()) {
                iniciarBackgroundMusic(Hawk.get("bg_music_pos", 0));
            }
        });

        tgMusic.setOnCheckedChangeListener((btn, isChecked) -> {
            Hawk.put("bg_music_enabled", isChecked);
            if (isChecked) {
                bgCircle.setVisibility(View.GONE);
                lottieMusic.setVisibility(View.VISIBLE);
                imgMusicalNote.setVisibility(View.VISIBLE);
                lottieMusic.playAnimation();
                if (!musicUrls.isEmpty()) iniciarBackgroundMusic(Hawk.get("bg_music_pos", 0));
                abrirDialogoConfigMusica();
            } else {
                lottieMusic.cancelAnimation();
                lottieMusic.setVisibility(View.GONE);
                imgMusicalNote.setVisibility(View.GONE);
                bgCircle.setVisibility(View.VISIBLE);
                stopBackgroundMusic();
            }
        });
    }

    private void fetchMusicList(Runnable callback) {
        String url = "https://softcroy.com/app_santos_v2/biblia/musicas/api_musicas.php";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
            try {
                musicNames.clear(); musicUrls.clear();
                for (int i = 0; i < response.length(); i++) {
                    JSONObject obj = response.getJSONObject(i);
                    musicNames.add(obj.getString("nome_pt")); // ou nome_en conforme a lógica
                    musicUrls.add(obj.getString("link"));
                }
                if (callback != null) callback.run();
            } catch (Exception e) { e.printStackTrace(); }
        }, null);
        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void iniciarBackgroundMusic(int pos) {
        if (pos < 0 || pos >= musicUrls.size()) return;
        try {
            if (bgMediaPlayer != null) bgMediaPlayer.release();
            bgMediaPlayer = new MediaPlayer();
            bgMediaPlayer.setDataSource(musicUrls.get(pos));
            bgMediaPlayer.setLooping(true);
            bgMediaPlayer.setVolume(musicVolume, musicVolume);
            bgMediaPlayer.prepareAsync();
            bgMediaPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void stopBackgroundMusic() {
        if (bgMediaPlayer != null) {
            bgMediaPlayer.stop();
            bgMediaPlayer.release();
            bgMediaPlayer = null;
        }
    }

    private void abrirDialogoConfigMusica() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_config_musica, null);
        builder.setView(view);

        Spinner spMusic = view.findViewById(R.id.spMusic);
        SeekBar sbVolume = view.findViewById(R.id.sbVolume);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, musicNames);
        spMusic.setAdapter(adapter);
        spMusic.setSelection(Hawk.get("bg_music_pos", 0));
        sbVolume.setProgress(Hawk.get("bg_music_vol", 50));

        spMusic.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int position, long id) {
                Hawk.put("bg_music_pos", position);
                if (tgMusic.isChecked()) iniciarBackgroundMusic(position);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                musicVolume = p / 100f;
                if (bgMediaPlayer != null) bgMediaPlayer.setVolume(musicVolume, musicVolume);
                Hawk.put("bg_music_vol", p);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        builder.create().show();
    }

    private void playy1() {
        if (mPlayer != null) mPlayer.release();
        mPlayer = MediaPlayer.create(getActivity(), R.raw.sond1);

        if (bgMediaPlayer != null) bgMediaPlayer.setVolume(0.1f, 0.1f);

        mPlayer.start();
        mPlayer.setOnCompletionListener(mp -> {
            if (Principal_Oracao.getmInstanceActivity() != null) {
                Principal_Oracao.getmInstanceActivity().OPENN();
            }
            stopOração();
        });
    }

    private void stopOração() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        play1.setChecked(false);
        if (bgMediaPlayer != null) bgMediaPlayer.setVolume(musicVolume, musicVolume);
    }

    private void setupOracao(String text, Typeface tf) {
        oracao.setText(text);
        oracao.setTextSize(26);
        oracao.setTextColor(Color.parseColor("#737373"));
        oracao.setTypeface(tf);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bgMediaPlayer != null && !bgMediaPlayer.isPlaying() && Hawk.get("bg_music_enabled", true)) {
            bgMediaPlayer.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayer != null) stopOração();
        if (bgMediaPlayer != null && bgMediaPlayer.isPlaying()) bgMediaPlayer.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopBackgroundMusic();
    }
}