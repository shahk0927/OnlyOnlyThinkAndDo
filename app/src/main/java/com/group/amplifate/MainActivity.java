package com.group.amplifate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button mRegisterBtn, mLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRegisterBtn=findViewById(R.id.register_btn);
        mLoginBtn=findViewById(R.id.login_btn);

        mRegisterBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //start Register Activity
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));


                overridePendingTransition(R.anim.activity_move_in_left,R.anim.activity_move_out_right);

            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.activity_move_in_right,R.anim.activity_move_out_left);

            }
        });
    }
}
/* in this part(17)
* Publish Post to firebase.
* Post will contain user name, email, uid, dp, time of publish, title description, image
* user can publish post with or without image
* create AddPostActivity
* images can we imported from gallery or taken from camera
* */
