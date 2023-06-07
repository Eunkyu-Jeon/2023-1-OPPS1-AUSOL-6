package com.example.keepfresh;

import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.keepfresh.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private Realm realm;
    private Realm exp_realm;
    SimpleDateFormat idFormat = new SimpleDateFormat("yyyyMMddHHmmss");


    private ActivityMainBinding binding;

    private LinearLayout container;

    private TextView roomTitleText;
    private TextView refriTitleText;
    private TextView freezeTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 네비게이션 관련 기본생성 코드
        setContentView(R.layout.activity_main);
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

        roomTitleText = (TextView) findViewById(R.id.roomTitleText);
        refriTitleText = (TextView) findViewById(R.id.refriTitleText);
        freezeTitleText = (TextView) findViewById(R.id.freezeTitleText);

        RealmConfiguration expConfig = new RealmConfiguration.Builder().allowWritesOnUiThread(true).build();
        exp_realm = Realm.getInstance(expConfig);
        realm = Realm.getDefaultInstance();


        /*************************************************
        * TODO 모델로 인식할 클래스에 대한 addFood 작업 필요   *
        ************************************************/
        if(!MyApplication.initExp){
            // .json파일의 정보를 읽어서 ExpList 테이블 생성
            parsingItemInfo();

            // 테스트 튜플 추가 테스트
            createTuple("사과", 0);
            createTuple("바나나", 1);
            createTuple("귤", 0);

            MyApplication.initExp = true;
            
            // Test 실행시마다 튜플 추가하기 때문에 지워주기
            //clearData();

        }
        Log.i("result", "start result");

        showResult();

        Log.i("result", "endResult");
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

        String name;
        int recommendStore;
        String[] storageInfoArray = new String[3];
        int[] expInfoArray = new int[3];

        AssetManager assetManager = getAssets();
        try {
            InputStream filePath = assetManager.open("itemInfo.json");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(filePath))) {
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

                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // DB에 정보 추가할 튜플 생성
    // 모델 이용한 유통기한 추가
    public void createTuple(final String name, final int storage){

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ItemList itemList = realm.createObject(ItemList.class);

                /*******input_date 설정*******/
                itemList.setInputDate(new Date());
                System.out.println(itemList.getInputDate());

                /*******name 설정*******/
                itemList.setName(name);

                /*******id 설정*******/
                String id = idFormat.format(itemList.getInputDate());
                itemList.setId(id);

                //나머지 data는 expList table을 참고해서 설정함
                ExpList expList = exp_realm.where(ExpList.class).equalTo("name", name).findAll().first();

                /*******storage 설정*******/
                itemList.setStorage(storage);

                /*******expire_date 설정*******/
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(itemList.getInputDate());
                calendar.add(Calendar.DATE, expList.getExp_info(1));
                itemList.setExpireDate(calendar.getTime());
            }
        });
    }

    // DB에 정보 추가할 튜플 생성
    // 유통기한 직접 입력시
    public void createTuple(final String name, final int storage, final Date expireDate){
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

                /*******storage 설정*******/
                itemList.setStorage(storage);

                /*******expire_date 설정*******/
                itemList.setExpireDate(expireDate);
            }
        });
    }

    // ItemList에 저장된 식품정보 불러오기
    private void showResult(){
        RealmResults<ItemList> results = realm.where(ItemList.class).findAll();

        for(ItemList data : results){
            Log.i("result", data.toString());
            if(data.getStorage() == 0) {
                container = (LinearLayout) findViewById(R.id.room_list);
                roomTitleText.setVisibility(View.VISIBLE);
            } else if(data.getStorage() == 1) {
                container = (LinearLayout) findViewById(R.id.refri_list);
                refriTitleText.setVisibility(View.VISIBLE);
            } else if(data.getStorage() == 2) {
                container = (LinearLayout) findViewById(R.id.freeze_list);
                freezeTitleText.setVisibility(View.VISIBLE);
            } else {
                // 미분류
                container = (LinearLayout) findViewById(R.id.freeze_list);
            }

            final Button button = new Button(this);
            button.setText(data.toString());
            container.addView(button);
            final String id = data.getId();

            Log.i("result", container.toString());

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //삭제하시겠습니까? 메세지
                    //예 아니오 --> 예 클릭 시 해당 데이터베이스 삭제
                    showMessage(id, button);
                    container.invalidate();
                }
            });
        }
    }

    private void showMessage(final String id, final Button button) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("품목 삭제");
        builder.setMessage("삭제하시겠습니까?");

        //예 클릭 시
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //해당 id를 갖는 tuple 삭제
                final RealmResults<ItemList> results = realm.where(ItemList.class).equalTo("id", id).findAll();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        results.deleteFirstFromRealm();
                        container.removeView(button);
                    }
                });
            }
        });
    }

    public static void clearData() {
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm realm = Realm.getInstance(config);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });

        realm.close();
    }
}