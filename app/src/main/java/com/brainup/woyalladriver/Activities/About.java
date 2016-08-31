package com.brainup.woyalladriver.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.brainup.woyalladriver.R;

/**
 * Created by Rog on 2/21/16.
 */

public class About extends AppCompatActivity {
    private TextView FaceBook_Link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_about));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FaceBook_Link = (TextView) findViewById(R.id.txt_facebook);
        FaceBook_Link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(About.this.getString(R.string.fb_link)));
                startActivity(browserIntent);
            }
        });
    }
}
