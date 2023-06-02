package com.example.keepfresh;

import io.realm.RealmObject;

// 유통기한 저장 DB(모델에서 인식할 클래스만)
public class ExpList extends RealmObject {
    String name;
    int storage;
    int expire_date;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }

    public int getExpireDate() {
        return expire_date;
    }

    public void setExpireDate(int expire_date) {
        this.expire_date = expire_date;
    }

}
