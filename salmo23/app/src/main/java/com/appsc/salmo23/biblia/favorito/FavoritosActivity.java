package com.appsc.salmo23.biblia.favorito;

import static com.appsc.salmo23.figurinhas.StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

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
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.orhanobut.hawk.Hawk;

import java.util.Arrays;

public class FavoritosActivity extends AppCompatActivity {

    private FrameLayout adContainerView;
    private AdView adView;
    private static final String AD_BANNER_ID = "ca-app-pub-3882038212063780/3070020738";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_favoritos_parent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, true);
        if (getSupportActionBar() != null) {

            TextView tv = new TextView(getApplicationContext());
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
        TabLayout tabLayout = findViewById(R.id.tabLayoutFav);
        ViewPager2 viewPager = findViewById(R.id.viewPagerFav);

        // Passamos 'this' (a Activity) para o adapter
        viewPager.setAdapter(new InnerPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Cor"); break;
                case 1: tab.setText("Nota"); break;
                case 2: tab.setText("Marcador"); break;
            }
        }).attach();
    }

    private static class InnerPagerAdapter extends FragmentStateAdapter {
        public InnerPagerAdapter(@NonNull FragmentActivity fragmentActivity) { 
            super(fragmentActivity); 
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new FavoritoCorFragment();
                case 1: return new FavoritoNotaFragment();
                case 2: return new FavoritoMarcadorFragment();
                default: return new FavoritoNotaFragment();
            }
        }

        @Override
        public int getItemCount() { 
            return 3;
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        Principal_Oracao.getmInstanceActivity().OPENN();
        finish();
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Principal_Oracao.getmInstanceActivity().OPENN();
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
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
        esconderBotoesFisicos();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
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