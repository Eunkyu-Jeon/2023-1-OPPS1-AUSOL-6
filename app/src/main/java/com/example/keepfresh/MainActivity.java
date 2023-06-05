package com.example.keepfresh;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.keepfresh.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
/*
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
*/
        /*************************************************
        * TODO 모델로 인식할 클래스에 대한 addFood 작업 필요   *
        ************************************************/
        if(!MyApplication.initExp){
            //ExpList 테이블 생성
            /*
            storage -> 0:상온 1:냉장 2:냉동

            addFood("사과", 1, 21);
            addFood("바나나", 2, 21);
            addFood("오렌지", 1, 7);
            addFood("쌈채소", 1, 4); //상추, 깻잎
            addFood("뿌리채소", 1, 7); //감자, 고구마
            addFood("버섯류", 1, 3);
            */
            // .json파일의 정보를 읽어서 ExpList 테이블 생성
            parsingItemInfo();

            MyApplication.initExp = true;
        }

        realm = Realm.getDefaultInstance();
    }
    // expList에 정보 넣기 위한 포맷 설정(모델에서 인식할 클래스에 대한 유통기한)
    public void addExpList(String name, int recommend_storage, String[] storage_info, int[] exp_info){

        //이미 있으면 생성하지 않음
        if(exp_realm.where(ExpList.class).equalTo("name", name).findAll().size() != 0)
            return;

        exp_realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ExpList expList = exp_realm.createObject(ExpList.class);
                expList.setName(name);
                expList.setRecommend_storage(recommend_storage);
                for (int i = 0; i < 3; i++) {
                    expList.setStorage_info(storage_info[i], i);
                    expList.setExp_info(exp_info[i], i);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exp_realm.close();
        realm.close();
    }

    public void parsingItemInfo(){
        String filePath = "./assets/itemInfo.json";

        String name;
        int recommendStore;
        String[] storageInfoArray = new String[3];
        int[] expInfoArray = new int[3];

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            JSONArray jsonArray = new JSONArray(jsonContent.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                name = ((JSONObject) jsonObject).optString("item_name");
                recommendStore = jsonObject.optInt("recommend_store");
                //JSONArray storageInfoArray = jsonObject.getJSONArray("storage_info");
                storageInfoArray[0] = jsonObject.optString("storage_info_0");
                storageInfoArray[1] = jsonObject.optString("storage_info_1");
                storageInfoArray[2] = jsonObject.optString("storage_info_2");
                expInfoArray[0] = jsonObject.optInt("exp_info_0");
                expInfoArray[1] = jsonObject.optInt("exp_info_1");
                expInfoArray[2] = jsonObject.optInt("exp_info_2");

                addExpList(name, recommendStore, storageInfoArray, expInfoArray);
                System.out.println(name);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
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
                if(expList.getRecommend_storage() == 0)
                    itemList.setStorage(0);
                else if(expList.getRecommend_storage() == 1)
                    itemList.setStorage(1);
                else if(expList.getRecommend_storage() == 2)
                    itemList.setStorage(2);
                else
                    itemList.setStorage(3);

                /*******expire_date 설정*******/
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(itemList.getInputDate());
                calendar.add(Calendar.DATE, expList.getExp_info(expList.getRecommend_storage()));
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