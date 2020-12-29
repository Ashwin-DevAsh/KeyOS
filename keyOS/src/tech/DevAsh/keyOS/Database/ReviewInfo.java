package tech.DevAsh.keyOS.Database;

import io.realm.Realm;
import io.realm.RealmObject;

public class ReviewInfo extends RealmObject {
   public int launchedCount = 0;
   public boolean isReviewed = false;

   public ReviewInfo(){}

   public ReviewInfo(int launchedCount,boolean isReviewed){
       this.launchedCount = launchedCount;
       this.isReviewed = isReviewed;
   }

   static public void init(int launchedCount,boolean isReviewed){
       Realm.getDefaultInstance().executeTransactionAsync(
               realm -> {
                   realm.delete(ReviewInfo.class);
                   realm.insert(new ReviewInfo(launchedCount,isReviewed));
               }
       );
   }
}
