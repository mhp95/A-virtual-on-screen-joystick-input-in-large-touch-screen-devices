package android.udev.com.touchproject;

public interface Listener {

  void onCorrectClick(int correctClickNum, int index, float distanceDiff, float targetRadius);

  void onWrongClick(int wrongClickNum, int index, float distanceDiff);

  void onReachTarget(long diffTime);

  void onRoundFinished(int correctClickNum, int wrongClickNum);

}
