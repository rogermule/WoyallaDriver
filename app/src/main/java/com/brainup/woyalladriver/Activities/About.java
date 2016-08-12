package com.brainup.woyalladriver.Activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.brainup.woyalladriver.R;

/**
 * Created by Rog on 2/21/16.
 */

public class About extends Activity {
    private TextView FaceBook_Link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        FaceBook_Link = (TextView) findViewById(R.id.txt_facebook);
        FaceBook_Link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/woyalla"));
                startActivity(browserIntent);
            }
        });
    }
}
