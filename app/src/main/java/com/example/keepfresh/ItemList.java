package com.example.keepfresh;
import java.text.SimpleDateFormat;
import java.util.Date;
import io.realm.RealmObject;

// 사용자 식품 리스트 DB
public class ItemList extends RealmObject {
    // 식품 식별 id
    String id;
    // 식품명
    String item_name;
    // 보관방법 (0:상온, 1:냉장, 2:냉동, 3:미정)
    int storage;
    // 입력 일자
    Date input_date;
    // 유통기한 만료일
    Date expire_date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return item_name;
    }

    public void setName(String item_name) {
        this.item_name = item_name;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }

    public Date getInputDate() {
        return input_date;
    }

    public void setInputDate(Date input_date) {
        this.input_date = input_date;
    }

    public Date getExpireDate() {
        return expire_date;
    }

    public void setExpireDate(Date expire_date) {
        this.expire_date = expire_date;
    }

    @Override
    public String toString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return "품목명 : " + item_name +
                "\n보관 방법 : " + storage +
                "\n구매 일자 : " + dateFormat.format(input_date) +
                "\n유통 기한 : " + dateFormat.format(expire_date);
    }

}
