package com.appsc.salmo23.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter;
import com.sangcomz.fishbun.define.Define;
import com.appsc.salmo23.R;
import com.appsc.salmo23.constants.Constants;
import com.appsc.salmo23.utils.FileUtils;
import com.appsc.salmo23.utils.StickerPacksManager;
import com.appsc.salmo23.whatsapp_api.Sticker;
import com.appsc.salmo23.whatsapp_api.StickerContentProvider2;
import com.appsc.salmo23.whatsapp_api.StickerPack;
import com.appsc.salmo23.whatsapp_api.StickerPackDetailsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NewStickerPackActivity extends AppCompatActivity {
    ImageAdapter imageAdapter;
    EditText nameEdit;
    EditText authorEdit;

    LottieAnimationView empty;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sticker_pack);

        // UI references.
        nameEdit = findViewById(R.id.sticker_pack_name_edit);
        empty=findViewById(R.id.animation_view);
        authorEdit = findViewById(R.id.sticker_pack_author_edit);
        FrameLayout btnCreate = findViewById(R.id.btn_create_pack);
        btnCreate.setOnClickListener(v -> {
            empty.setVisibility(View.GONE);
            FishBun.with(NewStickerPackActivity.this)
                    .setImageAdapter(new GlideAdapter())
                    .setMaxCount(30)
                    .exceptGif(true)
                    .setActionBarColor(Color.parseColor("#128c7e"), Color.parseColor("#128c7e"), false)
                    .setMinCount(3).setActionBarTitleColor(Color.parseColor("#ffffff"))
                    .startAlbum();
        });

        GridView gridview = findViewById(R.id.sticker_pack_grid_images_preview);
        imageAdapter = new ImageAdapter(this);
        gridview.setAdapter(imageAdapter);

        gridview.setOnItemClickListener((parent, v, position, id) -> {
            Toast.makeText(NewStickerPackActivity.this, R.string.Image_removed, Toast.LENGTH_SHORT).show();
            imageAdapter.uries.remove(position);
            imageAdapter.notifyDataSetChanged();
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Html.fromHtml("<center>" + getString(R.string.novo_pacote) +" \uD83D\uDCA1"+"</center>"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }
        
    }
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_sticker_pack, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.save_sticker_pack) {
            if (validateValues()) {
                Toast.makeText(this, R.string.You_have_to_fill_all_empty_spaces, Toast.LENGTH_SHORT).show();
            } else {
                saveStickerPack(imageAdapter.uries, nameEdit.getText().toString(), authorEdit.getText().toString());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validateValues() {
        return nameEdit.getText().toString().trim().length() == 0 || authorEdit.getText().toString().trim().length() == 0 || imageAdapter.uries.size() == 0;
    }

    private void saveStickerPack(List<Uri> uries, String name, String author) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.Wait_a_moment_while_we_process_your_stickers)); // Setting Message
        progressDialog.setTitle(getString(R.string.Processing_images)); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);
        new Thread(() -> {
            try {

                Intent intent = new Intent(NewStickerPackActivity.this, StickerPackDetailsActivity.class);
                intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, true);

                String identifier = "." + FileUtils.generateRandomIdentifier();
                StickerPack stickerPack = new StickerPack(identifier, name, author, Objects.requireNonNull(uries.toArray())[0].toString(), "", "", "", "");

                //Save the sticker images locally and get the list of new stickers for pack
                List<Sticker> stickerList = StickerPacksManager.saveStickerPackFilesLocally(stickerPack.identifier, uries, NewStickerPackActivity.this);
                stickerPack.setStickers(stickerList);

                //Generate image tray icon
                String stickerPath = Constants.STICKERS_DIRECTORY_PATH  + identifier;
                String trayIconFile = FileUtils.generateRandomIdentifier() + ".png";
                StickerPacksManager.createStickerPackTrayIconFile(uries.get(0), Uri.parse(stickerPath + "/" + trayIconFile), NewStickerPackActivity.this);
                stickerPack.trayImageFile = trayIconFile;

                //Save stickerPack created to write in json
                StickerPacksManager.stickerPacksContainer.addStickerPack(stickerPack);
                StickerPacksManager.saveStickerPacksToJson(StickerPacksManager.stickerPacksContainer);
                insertStickerPackInContentProvider(stickerPack);

                //Start new activity with stickerpack information
                intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, stickerPack);
                startActivity(intent);
                NewStickerPackActivity.this.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();
        }).start();
    }

    private void insertStickerPackInContentProvider(StickerPack stickerPack) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("stickerPack", new Gson().toJson(stickerPack));
        getContentResolver().insert(StickerContentProvider2.AUTHORITY_URI, contentValues);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Define.ALBUM_REQUEST_CODE) {

            ArrayList<Uri> uries = new ArrayList<>();
            if (resultCode == RESULT_OK) {
                uries = data.getParcelableArrayListExtra(Define.INTENT_PATH);
                if (uries.size() > 0) {
                    imageAdapter.uries = uries;
                    imageAdapter.notifyDataSetChanged();
                    ((TextView) findViewById(R.id.stickers_selected_textview)).setText(uries.size() + " stickers selected");
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        esconderBotoesFisicos();
    }
    class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return uries.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            SimpleDraweeView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new SimpleDraweeView(mContext);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setAdjustViewBounds(true);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (SimpleDraweeView) convertView;
            }

            imageView.setImageURI(uries.get(position));
            return imageView;
        }

        // references to our images
        ArrayList<Uri> uries = new ArrayList<>();
    }
    private void esconderBotoesFisicos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    }
}

