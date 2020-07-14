package android.udev.com.touchproject;

import android.app.Application;

public class G extends Application {

  public static DatabaseHandler database;

  @Override
  public void onCreate() {
    super.onCreate();

    database = new DatabaseHandler(getApplicationContext());

  }


}
