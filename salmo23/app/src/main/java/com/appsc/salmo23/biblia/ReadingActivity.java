package com.appsc.salmo23.biblia;

import static com.appsc.salmo23.figurinhas.StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.Principal_Oracao;
import com.appsc.salmo23.R;
import com.appsc.salmo23.figurinhas.EntryActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.orhanobut.hawk.Hawk;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ReadingActivity extends AppCompatActivity {

    private RecyclerView rvLeitura;
    private ImageView fabPrev, fabNext;
    private Button btnMarcarLido;
    private ReadingAdapter adapter;
    private List<VerseModel> verseList = new ArrayList<>();

    private String livroSelecionado;
    private int capituloSelecionado;
    private int versiculoAlvo;

    // --- ÁUDIO ---
    private MediaPlayer mediaPlayer;
    private int currentPlayingIndex = -1;
    private ToggleButton btnPlayPause;

    private MediaPlayer bgMediaPlayer;
    private float musicVolume = 0.5f;
    private List<String> musicUrls = new ArrayList<>();
    private List<String> musicNames = new ArrayList<>();
    private ArrayAdapter<String> musicSpinnerAdapter;

    // --- ANUNCIO ---

    private FrameLayout adContainerView;
    private AdView adView;

    TextView tv;
    private static final String AD_BANNER_ID = "ca-app-pub-3882038212063780/3070020738";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, true);
        if (getSupportActionBar() != null) {

            tv = new TextView(getApplicationContext());
            tv.setText(getString(R.string.favoritos));
            tv.setTextSize(23);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
            tv.setTypeface(tf);
            getSupportActionBar().setDisplayOptions(androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            getSupportActionBar().setCustomView(tv);
        }
        if (!Hawk.get("Assinatura").equals("Assinado")) {
            loadBanner();
        }

        if (!Hawk.isBuilt()) Hawk.init(this).build();

        livroSelecionado = getIntent().getStringExtra("LIVRO");
        capituloSelecionado = getIntent().getIntExtra("CAPITULO", 1);
        versiculoAlvo = getIntent().getIntExtra("VERSICULO", 1);

        rvLeitura = findViewById(R.id.rvLeituraCompleta);
        fabPrev = findViewById(R.id.fabPrevious);
        fabNext = findViewById(R.id.fabNext);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnMarcarLido = findViewById(R.id.btnMarcarLido);

        rvLeitura.setLayoutManager(new LinearLayoutManager(this));

        configurarMusicaFundo();

        fabPrev.setOnClickListener(v -> mudarCapitulo(-1));
        fabNext.setOnClickListener(v -> mudarCapitulo(1));
        btnPlayPause.setOnClickListener(v -> toggleAudio());
        btnMarcarLido.setOnClickListener(v -> marcarComoLido());

        atualizarTela();
    }

    // Auxiliar para data formatada (ex: Mar, 05, 2026)
    private String getDataFormatada() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", new Locale("pt", "BR"));
        String data = sdf.format(Calendar.getInstance().getTime());
        return data.substring(0, 1).toUpperCase() + data.substring(1);
    }

    private void marcarComoLido() {
        String key = "lido_" + livroSelecionado + "_" + capituloSelecionado;
        boolean jaLido = Hawk.get(key, false);
        List<Integer> lidos = Hawk.get("lista_lidos_" + livroSelecionado, new ArrayList<>());

        if (jaLido) {
            // Lógica para DESMARCAR
            Hawk.put(key, false);
            lidos.remove(Integer.valueOf(capituloSelecionado));
            Toast.makeText(this, "Capítulo marcado como não lido", Toast.LENGTH_SHORT).show();
        } else {
            // Lógica para MARCAR
            Hawk.put(key, true);
            if (!lidos.contains(capituloSelecionado)) {
                lidos.add(capituloSelecionado);
            }
            Toast.makeText(this, "Capítulo concluído!", Toast.LENGTH_SHORT).show();
        }

        // Salva a lista atualizada e recalcula o progresso
        Hawk.put("lista_lidos_" + livroSelecionado, lidos);

        // Atualiza o progresso (ajuste o total de capítulos se necessário ou use uma variável dinâmica)
        int totalCapsLivro = 50; // Sugestão: buscar o total real do livro se possível
        int percent = (lidos.size() * 100) / totalCapsLivro;
        Hawk.put("progress_" + livroSelecionado, Math.min(percent, 100));

        // Atualiza o visual do botão imediatamente
        verificarBotaoLido();
    }

    private void verificarBotaoLido() {
        boolean jaLido = Hawk.get("lido_" + livroSelecionado + "_" + capituloSelecionado, false);

        // O botão agora fica sempre habilitado para permitir desmarcar
        btnMarcarLido.setEnabled(true);

        if (jaLido) {
            btnMarcarLido.setText("Lido ✓");
            // Opcional: mudar a cor para indicar estado ativo (ex: verde ou dourado)
            btnMarcarLido.setAlpha(0.7f);
        } else {
            btnMarcarLido.setText("Marcar como lido");
            btnMarcarLido.setAlpha(1.0f);
        }
    }

    private void mudarCapitulo(int direcao) {
        int novoCap = capituloSelecionado + direcao;
        if (novoCap < 1) {
            Toast.makeText(this, "Você está no primeiro capítulo", Toast.LENGTH_SHORT).show();
            return;
        }
        pararAudio();
        this.capituloSelecionado = novoCap;
        this.versiculoAlvo = 1;
        atualizarTela();
    }

    private void atualizarTela() {
        tv.setText(livroSelecionado + " " + capituloSelecionado);
        verificarBotaoLido();
        fetchFullChapter();
    }

    private void fetchFullChapter() {
        String url = "https://softcroy.com/app_santos_v2/biblia/biblia_pt_br_arc/api/api.php?action=get_full_text&livro="
                + livroSelecionado + "&capitulo=" + capituloSelecionado;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() == 0) {
                            voltarAoInicio();
                            return;
                        }
                        verseList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            verseList.add(new VerseModel(obj.getInt("versiculo"), obj.getString("texto")));
                        }
                        adapter = new ReadingAdapter(verseList);
                        rvLeitura.setAdapter(adapter);
                        scrollToVerse();
                    } catch (JSONException e) { voltarAoInicio(); }
                }, error -> voltarAoInicio()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void voltarAoInicio() {
        Toast.makeText(this, "Fim do livro! Voltando ao Capítulo 1.", Toast.LENGTH_SHORT).show();
        this.capituloSelecionado = 1;
        this.versiculoAlvo = 1;
        atualizarTela();
    }

    private void configurarMusicaFundo() {
        View bgCircle = findViewById(R.id.bgCircle);
        LottieAnimationView lottieMusic = findViewById(R.id.lottieMusic);
        ImageView imgMusicalNote = findViewById(R.id.imgMusicalNote);
        ToggleButton tgMusic = findViewById(R.id.tgMusic);

        // 1. Define 'true' para tocar direto na primeira instalação
        boolean isEnabled = Hawk.get("bg_music_enabled", true);
        tgMusic.setChecked(isEnabled);

        // 2. Ajuste visual inicial
        if (isEnabled) {
            bgCircle.setVisibility(View.GONE);
            lottieMusic.setVisibility(View.VISIBLE);
            imgMusicalNote.setVisibility(View.VISIBLE);
            lottieMusic.playAnimation();
        }

        // 3. CHAMA A LISTA: A lambda agora é aceita como Runnable
        fetchMusicList(() -> {
            // Este bloco só roda quando o download da API termina
            if (tgMusic.isChecked() && !musicUrls.isEmpty()) {
                int ultimaPos = Hawk.get("bg_music_pos", 0);
                startBackgroundMusic(ultimaPos);
            }
        }, tgMusic);

        // 4. Listener para cliques manuais
        tgMusic.setOnCheckedChangeListener((btn, isChecked) -> {
            Hawk.put("bg_music_enabled", isChecked);
            if (isChecked) {
                bgCircle.setVisibility(View.GONE);
                lottieMusic.setVisibility(View.VISIBLE);
                imgMusicalNote.setVisibility(View.VISIBLE);
                lottieMusic.playAnimation();

                if (!musicUrls.isEmpty()) {
                    startBackgroundMusic(Hawk.get("bg_music_pos", 0));
                }
                abrirDialogoConfigMusica(tgMusic);
            } else {
                lottieMusic.cancelAnimation();
                lottieMusic.setVisibility(View.GONE);
                imgMusicalNote.setVisibility(View.GONE);
                bgCircle.setVisibility(View.VISIBLE);
                stopBackgroundMusic();
            }
        });
    }
    private void abrirDialogoConfigMusica(ToggleButton tgMusic) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_config_musica, null);
        builder.setView(view);

        Spinner spMusic = view.findViewById(R.id.spMusic);
        SeekBar sbVolume = view.findViewById(R.id.sbVolume);

        // Configura o Spinner com as músicas já carregadas
        musicSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, musicNames);
        spMusic.setAdapter(musicSpinnerAdapter);

        // Restaura a última posição e volume salvos
        int ultimaPos = Hawk.get("bg_music_pos", 0);
        int ultimoVol = Hawk.get("bg_music_vol", 50);

        if (ultimaPos < musicNames.size()) spMusic.setSelection(ultimaPos);
        sbVolume.setProgress(ultimoVol);

        AlertDialog dialog = builder.create();

        // Lógica do Spinner no Diálogo
        spMusic.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Hawk.put("bg_music_pos", position);
                // Se o botão principal estiver ligado, troca a música imediatamente
                if (tgMusic.isChecked()) {
                    startBackgroundMusic(position);
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Lógica da SeekBar no Diálogo
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

        // Ao fechar o diálogo, se o ToggleButton estiver desligado mas o usuário mexeu,
        // podemos decidir ligar ou apenas salvar. Aqui, vamos garantir que reflita o estado.
        dialog.setOnDismissListener(d -> {
            // Se houver música selecionada e o botão estiver OFF, liga ao sair se desejar
            // Ou simplesmente mantém o estado do tgMusic.
            if (tgMusic.isChecked()) {
                Hawk.put("bg_music_enabled", true);
            }
        });

        dialog.show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (bgMediaPlayer != null && !bgMediaPlayer.isPlaying() && Hawk.get("bg_music_enabled", false)) {
            bgMediaPlayer.start();
        }
        if (adView != null) {
            adView.resume();
        }
        esconderBotoesFisicos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pararAudio();
        stopBackgroundMusic();
        if (adView != null) {
            adView.destroy();
        }
    }

    private void playVerse(int index) {
        if (index >= verseList.size()) {
            pararAudio();
            return;
        }

        currentPlayingIndex = index;
        VerseModel v = verseList.get(index);
        String audioUrl = ("https://softcroy.com/app_santos_v2/biblia/biblia_pt_br_arc/audio/"
                + livroSelecionado + "_" + capituloSelecionado + "_" + v.getVersiculo() + ".mp3").replace(" ", "_");

        try {
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();

                // Ativa o estado Checked para mostrar o ícone de PAUSE do seu selector
                btnPlayPause.setChecked(true);

                adapter.notifyDataSetChanged();
                rvLeitura.smoothScrollToPosition(index);
            });

            mediaPlayer.setOnCompletionListener(mp -> playVerse(index + 1));

        } catch (Exception e) {
            playVerse(index + 1);
        }
    }

    private void toggleAudio() {
        // Se o player existe e está tocando, pausamos
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            // Desativamos o Checked para mostrar o ícone de PLAY do seu selector
            btnPlayPause.setChecked(false);
        } else {
            // Se estava pausado ou parado, inicia o áudio
            playVerse((currentPlayingIndex == -1) ? 0 : currentPlayingIndex);
        }
    }

    private void pararAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        currentPlayingIndex = -1;

        // Volta para o ícone de PLAY
        btnPlayPause.setChecked(false);

        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void startBackgroundMusic(int pos) {
        if (pos < 0 || pos >= musicUrls.size()) return;
        try {
            if (bgMediaPlayer != null) bgMediaPlayer.release();
            bgMediaPlayer = new MediaPlayer();
            bgMediaPlayer.setDataSource(musicUrls.get(pos));
            bgMediaPlayer.setLooping(true);
            bgMediaPlayer.setVolume(musicVolume, musicVolume);
            bgMediaPlayer.prepareAsync();
            bgMediaPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void stopBackgroundMusic() {
        if (bgMediaPlayer != null) { bgMediaPlayer.stop(); bgMediaPlayer.release(); bgMediaPlayer = null; }
    }

    // Adicionamos 'Runnable callback' como o primeiro parâmetro
    private void fetchMusicList(Runnable callback, ToggleButton tg) {
        String url = "https://softcroy.com/app_santos_v2/biblia/musicas/api_musicas.php";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
            try {
                musicNames.clear();
                musicUrls.clear();

                for (int i = 0; i < response.length(); i++) {
                    JSONObject obj = response.getJSONObject(i);
                    musicNames.add(Locale.getDefault().getLanguage().equals("en") ? obj.getString("nome_en") : obj.getString("nome_pt"));
                    musicUrls.add(obj.getString("link"));
                }

                if (musicSpinnerAdapter != null) musicSpinnerAdapter.notifyDataSetChanged();

                // EXECUTAR O CALLBACK:
                // Isso avisa o sistema que as músicas terminaram de baixar
                if (callback != null) {
                    callback.run();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            // Trate erros de rede aqui se necessário
        });

        Volley.newRequestQueue(this).add(request);
    }

    private void scrollToVerse() {
        rvLeitura.post(() -> {
            if (versiculoAlvo > 0 && versiculoAlvo <= verseList.size()) {
                ((LinearLayoutManager) rvLeitura.getLayoutManager()).scrollToPositionWithOffset(versiculoAlvo - 1, 0);
            }
        });
    }

    private void abrirOpcoesVersiculo(VerseModel v) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_verse_options, null);
        bottomSheet.setContentView(view);
        String vKey = livroSelecionado + "_" + capituloSelecionado + "_" + v.getVersiculo();

        view.findViewById(R.id.btnMarcador).setOnClickListener(v1 -> {
            Set<String> marcadores = Hawk.get("marcadores_" + livroSelecionado, new HashSet<>());
            List<String> masterMarcadores = Hawk.get("master_marcadores", new ArrayList<>());
            Set<String> novoSet = new HashSet<>(marcadores);

            if (novoSet.contains(vKey)) {
                novoSet.remove(vKey);
                masterMarcadores.remove(vKey);
                Hawk.delete("date_marcador_" + vKey);
            } else {
                novoSet.add(vKey);
                if (!masterMarcadores.contains(vKey)) masterMarcadores.add(vKey);
                Hawk.put("date_marcador_" + vKey, getDataFormatada());
                Hawk.put("text_copy_" + vKey, v.getTexto());
            }

            Hawk.put("marcadores_" + livroSelecionado, novoSet);
            Hawk.put("master_marcadores", masterMarcadores);
            adapter.notifyDataSetChanged();
            bottomSheet.dismiss();
        });

        view.findViewById(R.id.btnNota).setOnClickListener(v1 -> {
            bottomSheet.dismiss();
            abrirDialogoNota(v, vKey);
        });

        view.findViewById(R.id.btnCompartilhar).setOnClickListener(v1 -> {
            String linkApp = "https://play.google.com/store/apps/details?id=" + getPackageName();
            String shareBody = livroSelecionado + " " + capituloSelecionado + ":" + v.getVersiculo() + "\n" + v.getTexto();
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain"); i.putExtra(Intent.EXTRA_TEXT, shareBody + "\n\n" + linkApp);
            startActivity(Intent.createChooser(i, "Compartilhar"));
            bottomSheet.dismiss();
        });

        configurarCliqueCor(view.findViewById(R.id.colorNone),   vKey, "#00000000", bottomSheet, v.getTexto());
        configurarCliqueCor(view.findViewById(R.id.colorYellow), vKey, "#FFF9C4", bottomSheet, v.getTexto());
        configurarCliqueCor(view.findViewById(R.id.colorGreen),  vKey, "#C8E6C9", bottomSheet, v.getTexto());
        configurarCliqueCor(view.findViewById(R.id.colorBlue),   vKey, "#BBDEFB", bottomSheet, v.getTexto());
        configurarCliqueCor(view.findViewById(R.id.colorPink),   vKey, "#F8BBD0", bottomSheet, v.getTexto());
        configurarCliqueCor(view.findViewById(R.id.colorOrange), vKey, "#FFE0B2", bottomSheet, v.getTexto());
        configurarCliqueCor(view.findViewById(R.id.colorPurple), vKey, "#E1BEE7", bottomSheet, v.getTexto());
        view.findViewById(R.id.btnFechar).setOnClickListener(v1 -> bottomSheet.dismiss());
        bottomSheet.show();
    }

    private void configurarCliqueCor(View view, String key, String colorHex, BottomSheetDialog dialog, String texto) {
        if (view == null) return;
        view.setOnClickListener(v -> {
            List<String> masterColors = Hawk.get("master_colors", new ArrayList<>());
            if (colorHex.equals("#00000000")) {
                Hawk.delete("color_" + key);
                Hawk.delete("date_color_" + key);
                masterColors.remove(key);
            } else {
                Hawk.put("color_" + key, colorHex);
                Hawk.put("date_color_" + key, getDataFormatada());
                Hawk.put("text_copy_" + key, texto);
                if (!masterColors.contains(key)) masterColors.add(key);
            }
            Hawk.put("master_colors", masterColors);
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });
    }

    private void abrirDialogoNota(VerseModel v, String verseKey) {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        View view = getLayoutInflater().inflate(R.layout.dialog_nota_duplo, null);
        dialog.setContentView(view);

        EditText etNota = view.findViewById(R.id.etNotaVersiculo);
        ImageView btnDeletar = view.findViewById(R.id.btnDeletarNota);

        ((TextView)view.findViewById(R.id.tvReferenciaNota)).setText(livroSelecionado + " " + capituloSelecionado + ":" + v.getVersiculo());
        ((TextView)view.findViewById(R.id.tvTextoResumo)).setText(v.getTexto());
        etNota.setText(Hawk.get("nota_" + verseKey, ""));
        btnDeletar.setVisibility(Hawk.contains("nota_" + verseKey) ? View.VISIBLE : View.GONE);

        if (dialog.getWindow() != null) dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        btnDeletar.setOnClickListener(v1 -> {
            new AlertDialog.Builder(this).setTitle("Excluir Nota").setMessage("Deseja apagar esta anotação?")
                    .setPositiveButton("Sim", (d, w) -> {
                        List<String> masterNotas = Hawk.get("master_notas", new ArrayList<>());
                        masterNotas.remove(verseKey);
                        Hawk.put("master_notas", masterNotas);
                        Hawk.delete("nota_" + verseKey);
                        Hawk.delete("date_nota_" + verseKey);
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    })
                    .setNegativeButton("Não", null).show();
        });

        view.findViewById(R.id.btnSalvarNota).setOnClickListener(v1 -> {
            String txt = etNota.getText().toString().trim();
            List<String> masterNotas = Hawk.get("master_notas", new ArrayList<>());
            if (!txt.isEmpty()) {
                Hawk.put("nota_" + verseKey, txt);
                Hawk.put("date_nota_" + verseKey, getDataFormatada());
                Hawk.put("text_copy_" + verseKey, v.getTexto());
                if (!masterNotas.contains(verseKey)) masterNotas.add(verseKey);
            } else {
                Hawk.delete("nota_" + verseKey);
                Hawk.delete("date_nota_" + verseKey);
                masterNotas.remove(verseKey);
            }
            Hawk.put("master_notas", masterNotas);
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        view.findViewById(R.id.btnCancelarNota).setOnClickListener(v1 -> dialog.dismiss());
        dialog.show();
    }

    private class ReadingAdapter extends RecyclerView.Adapter<ReadingAdapter.ViewHolder> {
        private List<VerseModel> items;
        public ReadingAdapter(List<VerseModel> items) { this.items = items; }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_reading_verse, p, false));
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VerseModel v = items.get(position);
            holder.tvNum.setText(String.valueOf(v.getVersiculo()));
            holder.tvText.setText(v.getTexto());
            String vKey = livroSelecionado + "_" + capituloSelecionado + "_" + v.getVersiculo();

            if (position == currentPlayingIndex) {
                holder.itemView.setBackgroundColor(Color.parseColor("#33A07844"));
            } else {
                holder.itemView.setBackgroundColor(Color.parseColor(Hawk.get("color_" + vKey, "#00000000")));
            }

            Set<String> marcadores = Hawk.get("marcadores_" + livroSelecionado, new HashSet<>());
            holder.imgMarcador.setVisibility(marcadores.contains(vKey) ? View.VISIBLE : View.GONE);
            holder.imgNota.setVisibility(Hawk.contains("nota_" + vKey) ? View.VISIBLE : View.GONE);

            holder.itemView.setOnClickListener(view -> abrirOpcoesVersiculo(v));
        }

        @Override public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNum, tvText; ImageView imgNota, imgMarcador;
            ViewHolder(View v) {
                super(v);
                tvNum = v.findViewById(R.id.tvNumVersiculo);
                tvText = v.findViewById(R.id.tvTextoVersiculo);
                imgNota = v.findViewById(R.id.imgNotaIcon);
                imgMarcador = v.findViewById(R.id.imgMarcadorIcon);
            }
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        fecharEMudarTela();
        return true;
    }

    // Modifique o método onBackPressed para usar a mesma inteligência de retorno
    @Override
    public void onBackPressed() {
        fecharEMudarTela();
    }
    private void fecharEMudarTela() {
        pararAudio();
        stopBackgroundMusic();

        // Verifica se a intent traz o ID de controle enviado exclusivamente pela tela de Notificação
        if (getIntent().hasExtra("ID_SEND_TO") && getIntent().getStringExtra("ID_SEND_TO") != null) {
            // Se veio da notificação, retorna explicitamente para ela mantendo a pilha estável
            Intent intentVoltar = new Intent(ReadingActivity.this, NotificacaoVersciculo.class);

            // Repassa os extras originais para que a NotificacaoVersciculo recarregue o mesmo estado perfeitamente
            intentVoltar.putExtra("id_registro", getIntent().getStringExtra("ID_SEND_TO"));

            // Reconstrói a String combinada original que a NotificacaoVersciculo espera receber para o Regex
            String livro = getIntent().getStringExtra("LIVRO");
            int capitulo = getIntent().getIntExtra("CAPITULO", 1);
            int versiculo = getIntent().getIntExtra("VERSICULO", 1);
            intentVoltar.putExtra("versciculo", livro + " " + capitulo + ":" + versiculo + " - ");

            intentVoltar.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intentVoltar);
        } else {
            // Fluxo Padrão Original do Aplicativo se veio da navegação normal de abas
            try {
                if (Principal_Oracao.getmInstanceActivity() != null) {
                    Principal_Oracao.getmInstanceActivity().OPENN();
                } else {
                    // Fallback de segurança se a instância principal sumir da memória
                    Intent intentEntry = new Intent(ReadingActivity.this, EntryActivity.class);
                    intentEntry.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentEntry);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Garante que o usuário não fique preso na tela se houver qualquer erro inesperado
                Intent intentEntry = new Intent(ReadingActivity.this, EntryActivity.class);
                intentEntry.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentEntry);
            }
        }
        finish();
    }
    private void loadBanner() {
        if (!com.google.android.ump.UserMessagingPlatform.getConsentInformation(this).canRequestAds()) {
            return;
        }

        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder()
                        .setTestDeviceIds(Arrays.asList("ABCDEF012345"))
                        .build()
        );

        adContainerView = findViewById(R.id.ad_view_container);

        adView = new AdView(this);
        adView.setAdUnitId(AD_BANNER_ID);

        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
    private AdSize getAdSize() {
        Display display = this.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;
        float adWidthPixels = adContainerView.getWidth();

        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adView != null) {
            adView.pause();
        }
    }
    private void esconderBotoesFisicos() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        final WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            // REMOVIDO: statusBars() -> Assim a hora continua aparecendo
            controller.hide(WindowInsets.Type.navigationBars());
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    } else {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        // REMOVIDO: SYSTEM_UI_FLAG_FULLSCREEN e LAYOUT_FULLSCREEN
        );
    }
}
}