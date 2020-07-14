package android.udev.com.touchproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class TouchPadView extends View {

  public static final int SCREEN_CURSOR_MODE_NORMAL = 0;
  public static final int SCREEN_CURSOR_MODE_BUBBLE = 1;

  private static final int BACKGROUND_COLOR = Color.parseColor("#26000000");
  private static final int NORMAL_FILL_COLOR = Color.parseColor("#D5E7E8");
  private static final int NORMAL_STROKE_COLOR = Color.parseColor("#CFCFCF");
  private static final int FOCUSED_FILL_COLOR = Color.parseColor("#ffffff");
  private static final int FOCUSED_STROKE_COLOR = Color.parseColor("#A09898");

  private static final int ROUND_LENGTH = 20;

  private static final float SHOCKER_RADIUS_NORMAL = 60;
  private static final float SHOCKER_RADIUS_FOCUSED = 70;

  private ScreenView screenView;
  private Listener listener;

  private Paint fillPaint;
  private Paint strokePaint;

  private float shockerX;
  private float shockerY;
  private float shockerRadius = SHOCKER_RADIUS_NORMAL;
  private float xRate;
  private float yRate;

  private int correctClickNum = 0;
  private int wrongClickNum = 0;

  private float lastClickX;
  private float lastClickY;

  private long timeAtTouchDown;
  private long timeAtTouchUp;

  private float touchDownPosX;
  private float touchDownPosY;
  private boolean isDragging;

  private float touchDownCursorPosX;
  private float touchDownCursorPosY;


  private int checkedTargetId = -1;
  private int closestCircleIndex = -1;

  private boolean bubbleCursor;

  public TouchPadView(Context context) {
    super(context);
    init();
  }

  public TouchPadView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public TouchPadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    this.setBackgroundColor(BACKGROUND_COLOR);

    fillPaint = new Paint();
    fillPaint.setColor(NORMAL_FILL_COLOR);
    fillPaint.setStyle(Paint.Style.FILL);
    fillPaint.setAntiAlias(true);

    strokePaint = new Paint();
    strokePaint.setColor(NORMAL_STROKE_COLOR);
    strokePaint.setStyle(Paint.Style.STROKE);
    strokePaint.setStrokeWidth(10);
    strokePaint.setAntiAlias(true);


  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvas.drawCircle(shockerX, shockerY, shockerRadius, fillPaint);
    canvas.drawCircle(shockerX, shockerY, shockerRadius, strokePaint);

  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    float width = screenView.getWidth() / 2;
    float height = screenView.getHeight() / 3;
    xRate = screenView.getWidth() / width;
    yRate = screenView.getHeight() / height;
    setMeasuredDimension((int) width, (int) height);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    shockerX = w / 2;
    shockerY = h / 2;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {


    int action = event.getActionMasked();
    switch (action) {

      case MotionEvent.ACTION_DOWN: {
        fillPaint.setColor(FOCUSED_FILL_COLOR);
        strokePaint.setColor(FOCUSED_STROKE_COLOR);
        shockerRadius = SHOCKER_RADIUS_FOCUSED;
        timeAtTouchDown = System.currentTimeMillis();
        touchDownPosX = event.getX();
        touchDownPosY = event.getY();
        touchDownCursorPosX = screenView.getCursorX();
        touchDownCursorPosY = screenView.getCursorY();
        invalidate();
      }
      break;

      case MotionEvent.ACTION_MOVE: {
        if (!isDragging && Math.abs(event.getX() - touchDownPosX) < 15 && Math.abs(event.getY() - touchDownPosY) < 15) {
          break;
        }
        if (!isDragging) {
          isDragging = true;
        }

        shockerX = (int) event.getX();
        shockerY = (int) event.getY();

        screenView.setCursor(((shockerX - touchDownPosX) * xRate) + touchDownCursorPosX, ((shockerY - touchDownPosY) * yRate) + touchDownCursorPosY);

        ArrayList<Integer> circlesBehindCursor = screenView.getCirclesBehindCursor();

        if (bubbleCursor) {
          if (circlesBehindCursor.size() != 0) {
          int closestCircleIndex = screenView.getClosestCircleIndex(circlesBehindCursor);

            if (closestCircleIndex != this.closestCircleIndex) {
              this.closestCircleIndex = closestCircleIndex;
              screenView.setClosestCircleIndex(closestCircleIndex);
            }

            Circle closestCircle = screenView.getCircle(this.closestCircleIndex);
            if (screenView.getCursorRadius() < closestCircle.radius) {
              screenView.increaseCursorRadius();
            } else if (screenView.getCursorRadius() > closestCircle.radius) {
              screenView.decreaseCursorRadius();
            }

          } else {
            closestCircleIndex = -1;
            screenView.increaseCursorRadius();
            screenView.setClosestCircleIndex(-1);
          }
        }

        if (circlesBehindCursor.contains(screenView.getTargetIndex()) && checkedTargetId != screenView.getTargetId()) {
          checkedTargetId = screenView.getTargetId();
          listener.onReachTarget(System.currentTimeMillis() - timeAtTouchDown);
        }

        invalidate();
        screenView.invalidate();

      }
      break;

      case MotionEvent.ACTION_UP: {
        long currentTime = System.currentTimeMillis();
        if (!isDragging && currentTime - timeAtTouchUp <= 500) {
          float x = Math.abs(screenView.getCursorX() - lastClickX);
          float y = Math.abs(screenView.getCursorY() - lastClickY);
          float clickDistanceDiff = (float) Math.sqrt((x * x) + (y * y));

          ArrayList<Integer> circlesBehindCursor = screenView.getCirclesBehindCursor();
          if ((circlesBehindCursor.contains(screenView.getTargetIndex()) && screenView.areTargetAndClosetCircleTheSame()) || (circlesBehindCursor.size() == 1 && circlesBehindCursor.get(0) == screenView.getTargetIndex())) {
            correctClickNum++;
            listener.onCorrectClick(correctClickNum, (correctClickNum + wrongClickNum), clickDistanceDiff, screenView.getTargetRadius());
            screenView.changeTarget();
          } else {
            wrongClickNum++;
            listener.onWrongClick(wrongClickNum, (correctClickNum + wrongClickNum), clickDistanceDiff);
          }

          lastClickX = screenView.getCursorX();
          lastClickY = screenView.getCursorY();

          timeAtTouchUp = 0;
        } else {

          if (isDragging) {
            timeAtTouchUp = 0;
          } else {
            timeAtTouchUp = System.currentTimeMillis();
          }
        }


        shockerX = getWidth() / 2;
        shockerY = getHeight() / 2;
        fillPaint.setColor(NORMAL_FILL_COLOR);
        strokePaint.setColor(NORMAL_STROKE_COLOR);
        shockerRadius = SHOCKER_RADIUS_NORMAL;
        isDragging = false;
        invalidate();

        if (correctClickNum == ROUND_LENGTH) {
          listener.onRoundFinished(correctClickNum, wrongClickNum);
          reset();
        }
      }
      break;
    }

    return true;
  }

  public void attachCursor(ScreenView cursor) {
    this.screenView = cursor;
  }

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  public void setScreenCursorMode(int cursorMode) {
    if (cursorMode == SCREEN_CURSOR_MODE_NORMAL) {
      bubbleCursor = false;
    } else if (cursorMode == SCREEN_CURSOR_MODE_BUBBLE) {
      bubbleCursor = true;
    }
  }

  public void reset() {
    bubbleCursor = false;
    checkedTargetId = -1;
    closestCircleIndex = -1;
    touchDownPosX = 0;
    touchDownPosY = 0;
    isDragging = false;
    lastClickX = 0;
    lastClickY = 0;
    correctClickNum = 0;
    wrongClickNum = 0;
    timeAtTouchUp = 0;
    timeAtTouchDown = 0;
    screenView.clean();
  }


}
