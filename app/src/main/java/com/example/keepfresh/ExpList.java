package com.example.keepfresh;

import io.realm.RealmObject;

// 유통기한 저장 DB(모델에서 인식할 클래스만)
public class ExpList extends RealmObject {
    // 식품명
    String name;

    int recommend_storage;

    // 보관방법 (0:상온, 1:냉장, 2:냉동, 3:미정)
    String storage_info0;
    String storage_info1;
    String storage_info2;
    int exp_info0;
    int exp_info1;
    int exp_info2;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

   public int getRecommend_storage() {
        return recommend_storage;
   }

   public void setRecommend_storage(int recommend_storage) {
        this.recommend_storage = recommend_storage;
   }

   public String getStorage_info(int index) {
        switch(index) {
            case 0: return storage_info0;
            case 1: return storage_info1;
            case 2: return storage_info2;
            default : return storage_info0;
        }
   }

   public void setStorage_info(String info, int index) {
       switch(index) {
           case 0: this.storage_info0 = info;
           case 1: this.storage_info1 = info;
           case 2: this.storage_info2 = info;
       }
   }

   public int getExp_info(int index) {
       switch(index) {
           case 0: return exp_info0;
           case 1: return exp_info1;
           case 2: return exp_info2;
           default : return exp_info0;
       }
   }

   public void setExp_info(int exp, int index) {
       switch(index) {
           case 0: this.exp_info0 = exp;
           case 1: this.exp_info1 = exp;
           case 2: this.exp_info2 = exp;
       }
   }
}
