package com.sabo.sabostore.Activity.Main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.github.chrisbanes.photoview.PhotoView;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.EventBus.UpdateStatusUserEvent;
import com.sabo.sabostore.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import maes.tech.intentanim.CustomIntent;

public class ItemDetailImagePreviewActivity extends AppCompatActivity {

    private PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail_image_preview);

        initViews();
    }

    private void initViews() {
        photoView = findViewById(R.id.pvItems);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Item Preview");

        if (!Common.selectedItemImage.equals("") || Common.selectedItemImage.isEmpty())
            Picasso.get().load(Common.selectedItemImage).into(photoView);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                CustomIntent.customType(this, Common.Anim_Fadein_to_Fadeout);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(this, Common.Anim_Fadein_to_Fadeout);
    }
}
