package com.example.keepfresh;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
//식품정보 입력 페이지
public class item_information_typing extends AppCompatActivity {

    private Button move_main_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_information_typing);

        move_main_btn = findViewById(R.id.move_main_btn);
        move_main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(item_information_typing.this , MainActivity.class);
                startActivity(intent); //액티비티 이동
            }
        });

    }
}