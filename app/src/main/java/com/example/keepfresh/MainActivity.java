package com.example.keepfresh;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.keepfresh.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private Realm realm;
    private Realm exp_realm;
    SimpleDateFormat idFormat;

    private LinearLayout container;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 네비게이션 관련 기본생성 코드
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        /*************************************************
        * TODO 모델로 인식할 클래스에 대한 addFood 작업 필요   *
        ************************************************/

        realm = Realm.getDefaultInstance();
    }
    // expList에 정보 넣기 위한 포맷 설정(모델에서 인식할 클래스에 대한 유통기한)
    public void addFood(final String name, final int storage, final int expire_date){

        //이미 있으면 생성하지 않음
        if(exp_realm.where(ExpList.class).equalTo("name", name).findAll().size() != 0)
            return;

        exp_realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ExpList expList = exp_realm.createObject(ExpList.class);
                expList.setName(name);
                expList.setStorage(storage);
                expList.setExpireDate(expire_date);
            }
        });
    }

    // DB에 정보 추가할 튜플 생성
    public void createTuple(final String name){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ItemList itemList = realm.createObject(ItemList.class);

                /*******input_date 설정*******/
                itemList.setInputDate(new Date());

                /*******name 설정*******/
                itemList.setName(name);

                /*******id 설정*******/
                String id = idFormat.format(itemList.getInputDate());
                itemList.setId(id);

                //나머지 data는 expList table을 참고해서 설정함
                ExpList expList = exp_realm.where(ExpList.class).equalTo("name", name).findAll().first();

                /*******storage 설정*******/
                if(expList.getStorage() == 0)
                    itemList.setStorage(0);
                else if(expList.getStorage() == 1)
                    itemList.setStorage(1);
                else if(expList.getStorage() == 2)
                    itemList.setStorage(2);
                else
                    itemList.setStorage(3);

                /*******expire_date 설정*******/
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(itemList.getInputDate());
                calendar.add(Calendar.DATE, expList.getExpireDate());
                itemList.setExpireDate(calendar.getTime());
            }
        });
    }

    // ItemList에 저장된 식품정보 불러오기
    private void showResult(){
        RealmResults<ItemList> results = realm.where(ItemList.class).findAll();

        for(ItemList data : results){
            if(data.getStorage() == 0) {
                container = (LinearLayout) findViewById(R.id.room_list);
            } else if(data.getStorage() == 1) {
                container = (LinearLayout) findViewById(R.id.refri_list);
            } else if(data.getStorage() == 2) {
                container = (LinearLayout) findViewById(R.id.freeze_list);
            } else {
                // 미분류
                container = (LinearLayout) findViewById(R.id.room_list);
            }

            final Button button = new Button(this);
            button.setText(data.toString());
            container.addView(button);
            final String id = data.getId();

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //삭제하시겠습니까? 메세지
                    //예 아니오 --> 예 클릭 시 해당 데이터베이스 삭제
                    // showMessage(id, button);
                    container.invalidate();
                }
            });
        }
    }

}