package android.udev.com.touchproject;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ajts.androidmads.library.SQLiteToExcel;

public class MainActivity extends AppCompatActivity {

  private static final int REQUEST_WRITE_STORAGE = 1;
  private TouchPadView touchPadView;
  private ScreenView screenView;
  private int userCode;
  private int userId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    touchPadView = findViewById(R.id.touch_view);
    screenView = findViewById(R.id.cursor_view);

    screenView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        boolean granted = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (granted) {
          copyDatabase();
        } else {
          ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
        return true;
      }
    });

    touchPadView.attachCursor(screenView);
    touchPadView.setListener(new Listener() {

      @Override
      public void onCorrectClick(int correctClickNum, int index, float distanceDiff, float targetRadius) {
        G.database.updateCorrectClickNumColumn(userId, correctClickNum);
        G.database.addToClickDistancesColumn(userId, index, (int) distanceDiff);
        G.database.updateTargetsRadius(userId, correctClickNum, (int) targetRadius);
      }

      @Override
      public void onWrongClick(int wrongClickNum, int index, float distanceDiff) {
        G.database.updateWrongClickNumColumn(userId, wrongClickNum);
        G.database.addToClickDistancesColumn(userId, index, (int) distanceDiff);
      }

      @Override
      public void onReachTarget(long diffTime) {
        G.database.addToTimesToReachTargetColumn(userId, diffTime);
      }

      @Override
      public void onRoundFinished(int correctClickNum, int wrongClickNum) {
        showDialogRoundIsOver(correctClickNum, wrongClickNum);
      }

    });

    showDialogUserCodeInput();
  }

  private void showDialogRoundIsOver(int correctClick, int wrongClick) {
    final Dialog dialog = new Dialog(this);
    final View roundIsOverView = getLayoutInflater().inflate(R.layout.dialog_round_is_over, (ViewGroup) findViewById(R.id.root), false);
    ((TextView) roundIsOverView.findViewById(R.id.tv_correct_click)).setText(correctClick + "");
    ((TextView) roundIsOverView.findViewById(R.id.tv_wrong_click)).setText(wrongClick + "");
    roundIsOverView.findViewById(R.id.btn_play_again).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showDialogOptions();
        dialog.dismiss();
      }
    });
    dialog.setContentView(roundIsOverView);
    dialog.setCancelable(false);
    dialog.show();
    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_WRITE_STORAGE) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        copyDatabase();
      } else {
        Toast.makeText(MainActivity.this, "database copy failed", Toast.LENGTH_SHORT).show();
      }
    }
  }

  private void showDialogUserCodeInput() {
    final Dialog dialog = new Dialog(this);
    final View userCodeView = getLayoutInflater().inflate(R.layout.dialog_user_code_input, (ViewGroup) findViewById(R.id.root), false);
    userCodeView.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String str = ((EditText) userCodeView.findViewById(R.id.et_id)).getText().toString();
        if (str == null || str.isEmpty()) {
          Toast.makeText(MainActivity.this, "Please enter your code", Toast.LENGTH_SHORT).show();
          return;
        }
        userCode = Integer.parseInt(str);
        showDialogOptions();
        dialog.dismiss();
      }
    });
    dialog.setContentView(userCodeView);
    dialog.setCancelable(false);
    dialog.show();
    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
  }

  private void showDialogOptions() {
    final Dialog dialog = new Dialog(this);
    final View optionsDialog = getLayoutInflater().inflate(R.layout.dialog_conditions, (ViewGroup) findViewById(R.id.root), false);
    optionsDialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        int selectedCondition = ((RadioGroup) optionsDialog.findViewById(R.id.rg_conditions)).getCheckedRadioButtonId();
        int conditionNumber = -1;

        if (selectedCondition == -1) {
          Toast.makeText(MainActivity.this, "Please select a condition", Toast.LENGTH_SHORT).show();
          return;
        }

        switch (selectedCondition) {
          case R.id.rb_condition_1:
            conditionNumber = 1;
            screenView.setCursorSize(ScreenView.CURSOR_SIZE_SMALL);
            touchPadView.setScreenCursorMode(TouchPadView.SCREEN_CURSOR_MODE_NORMAL);
            break;

          case R.id.rb_condition_2:
            conditionNumber = 2;
            screenView.setCursorSize(ScreenView.CURSOR_SIZE_BIG);
            touchPadView.setScreenCursorMode(TouchPadView.SCREEN_CURSOR_MODE_NORMAL);
            break;

          case R.id.rb_condition_3:
            conditionNumber = 3;
            screenView.setCursorSize(ScreenView.CURSOR_SIZE_SMALL);
            touchPadView.setScreenCursorMode(TouchPadView.SCREEN_CURSOR_MODE_BUBBLE);
            break;
        }

        G.database.insert(userCode, conditionNumber);
        userId = G.database.getLastId();

        screenView.draw(conditionNumber == 3 ? true : false);
        dialog.dismiss();
      }
    });
    dialog.setContentView(optionsDialog);
    dialog.setCancelable(false);
    dialog.show();
    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
  }

  private void copyDatabase() {
    SQLiteToExcel sqLiteToExcel = new SQLiteToExcel(this, DatabaseHandler.DATABASE_NAME, Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
    sqLiteToExcel.exportSingleTable(DatabaseHandler.TABLE_NAME, "database.xls", new SQLiteToExcel.ExportListener() {
      @Override
      public void onStart() {
        //nothing to do
      }

      @Override
      public void onCompleted(String filePath) {
        Toast.makeText(MainActivity.this, "database copied to sdcard", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onError(Exception e) {
        Toast.makeText(MainActivity.this, "database copy failed", Toast.LENGTH_SHORT).show();
      }
    });
  }

}
