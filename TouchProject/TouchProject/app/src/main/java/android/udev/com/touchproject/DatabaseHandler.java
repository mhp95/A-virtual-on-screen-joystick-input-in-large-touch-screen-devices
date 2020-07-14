package android.udev.com.touchproject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

  public static final String DATABASE_NAME = "database.db";
  public static final int DATABASE_VERSION = 1;

  public static final String TABLE_NAME = "User";
  public static final String COLUMN_ID = "id";
  public static final String COLUMN_USER_CODE = "user_code";
  public static final String COLUMN_CORRECT_CLICK_NUM = "correct_click_num";
  public static final String COLUMN_WRONG_CLICK_NUM = "wrong_click_num";
  public static final String COLUMN_CLICK_DISTANCES = "click_distances";
  public static final String COLUMN_TIMES_TO_REACH_TARGET = "times_to_reach_target";
  public static final String COLUMN_CONDITION = "condition";
  public static final String COLUMN_TARGETS_RADIUS = "targets_radius";

  public DatabaseHandler(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("create table if not exists " + TABLE_NAME + "(" + COLUMN_ID + " integer primary key autoincrement , " + COLUMN_USER_CODE + " int not null," + COLUMN_CONDITION + " integer not null, " + COLUMN_CORRECT_CLICK_NUM + " integer, " + COLUMN_WRONG_CLICK_NUM + " integer, " + COLUMN_CLICK_DISTANCES + " text, " + COLUMN_TIMES_TO_REACH_TARGET + " text, " + COLUMN_TARGETS_RADIUS + " text" + ")");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("drop table if exists " + TABLE_NAME);
    onCreate(db);
  }

  public void deleteAllRows() {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL("delete from " + TABLE_NAME);
    db.close();
  }

  public void deleteTable() {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL("drop table if exists " + TABLE_NAME);
    db.close();
  }

  public void insert(int userCode, int condition) {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL("insert into " + TABLE_NAME + " (" + COLUMN_USER_CODE + "," + COLUMN_CONDITION + ") values (" + userCode + "," + condition + ")");
    db.close();
  }

  public void updateCorrectClickNumColumn(int id, int value) {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL("update " + TABLE_NAME + " set " + COLUMN_CORRECT_CLICK_NUM + "=" + value + " where " + COLUMN_ID + "=" + id);
    db.close();
  }

  public void updateWrongClickNumColumn(int id, int value) {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL("update " + TABLE_NAME + " set " + COLUMN_WRONG_CLICK_NUM + "=" + value + " where " + COLUMN_ID + "=" + id);
    db.close();
  }

  public void addToClickDistancesColumn(int id, int index, int value) {
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery("select " + COLUMN_CLICK_DISTANCES + " from " + TABLE_NAME, null);
    String lastValue = null;
    while (cursor.moveToNext()) {
      lastValue = cursor.getString(0);
    }
    db.execSQL("update " + TABLE_NAME + " set " + COLUMN_CLICK_DISTANCES + "=" + ("\"" + (lastValue == null ? "[" + index + "] " + value : lastValue + " , " + "[" + index + "] " + value) + "\"") + " where " + COLUMN_ID + "=" + id);
    db.close();
  }

  public void addToTimesToReachTargetColumn(int id, long value) {
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery("select " + COLUMN_TIMES_TO_REACH_TARGET + " from " + TABLE_NAME, null);
    String lastValue = null;
    while (cursor.moveToNext()) {
      lastValue = cursor.getString(0);
    }
    db.execSQL("update " + TABLE_NAME + " set " + COLUMN_TIMES_TO_REACH_TARGET + "=" + ("\"" + (lastValue == null ? value : lastValue + "," + value) + "\"") + " where " + COLUMN_ID + "=" + id);
    db.close();
  }

  public void updateTargetsRadius(int id, int index, int radius) {
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery("select " + COLUMN_TARGETS_RADIUS + " from " + TABLE_NAME, null);
    String lastValue = null;
    while (cursor.moveToNext()) {
      lastValue = cursor.getString(0);
    }
    db.execSQL("update " + TABLE_NAME + " set " + COLUMN_TARGETS_RADIUS + "=" + ("\"" + (lastValue == null ? "[" + index + "] " + radius : lastValue + ", " + "[" + index + "] " + radius) + "\"") + " where " + COLUMN_ID + "=" + id);
    db.close();
  }

  public int getLastId() {
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery("select max (" + COLUMN_ID + ")" + " from " + TABLE_NAME, null);
    int lastId = 0;
    while (cursor.moveToNext()) {
      lastId = cursor.getInt(0);
    }
    db.close();
    return lastId;
  }


}
