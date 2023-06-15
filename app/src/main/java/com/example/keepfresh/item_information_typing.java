package com.example.keepfresh;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;

//식품정보 입력 페이지
public class item_information_typing extends AppCompatActivity {

    private Button cancel_btn;
    private Button submit_btn;

    private Realm realm;
    private Realm exp_realm;

    SimpleDateFormat idFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_information_typing);

        realm = Realm.getDefaultInstance();
        exp_realm = Realm.getDefaultInstance();

        cancel_btn = findViewById(R.id.cancel_btn);
        submit_btn = findViewById(R.id.submit_btn);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(item_information_typing.this , MainActivity.class);
                startActivity(intent); //액티비티 이동
            }
        });

        submit_btn.setOnClickListener(new View.OnClickListener() {
            EditText itemEditText = findViewById(R.id.item_id);
            Spinner spinner = findViewById(R.id.spinner);
            EditText expEditText = findViewById(R.id.expiration_id);

            @Override
            public void onClick(View v) {
                String nameText = itemEditText.getText().toString();
                String selectedStorageText = spinner.getSelectedItem().toString();
                int selectedStorage = 0;
                if(selectedStorageText == "실온보관") {
                    selectedStorage = 0;
                } else if(selectedStorageText == "냉장보관") {
                    selectedStorage = 1;
                } else if(selectedStorageText == "냉동보관") {
                    selectedStorage = 2;
                }
                int expText = Integer.parseInt(expEditText.getText().toString());
                createTuple(nameText, selectedStorage, expText);
            }
        });

    }


    public void createTuple(final String name, final int storage, final int expireDate){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ItemList itemList = realm.createObject(ItemList.class);

                /*******input_date 설정*******/
                itemList.setInputDate(new Date());

                /*******name 설정*******/
                itemList.setName(name);

                /*******id 설정*******/
                String id = idFormat.format(itemList.getInputDate()) + String.valueOf(itemList.getStorage());
                itemList.setId(id);

                /*******storage 설정*******/
                itemList.setStorage(storage);

                /*******expire_date 설정*******/
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(itemList.getInputDate());
                calendar.add(Calendar.DATE, expireDate);
                itemList.setExpireDate(calendar.getTime());
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        exp_realm.close();
        realm.close();
    }

}