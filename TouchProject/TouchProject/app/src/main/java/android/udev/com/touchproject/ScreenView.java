package android.udev.com.touchproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;


public class ScreenView extends View {

  private static final int CURSOR_FILL_COLOR = Color.parseColor("#8083995E");
  private static final int TARGET_FILL_COLOR = Color.parseColor("#D15A5A");
  private static final int CIRCLE_FILL_COLOR = Color.parseColor("#DFDCDC");
  private static final int BUBBLE_CIRCLE_FILL_COLOR = Color.parseColor("#4F6085");

  public static final float CURSOR_SIZE_SMALL = 35;
  public static final float CURSOR_SIZE_BIG = 55;

  private static final int CIRCLES_COUNT = 20;

  private Paint cursorFillPaint;
  private Paint circleFillPaint;

  private ArrayList<Circle> circles = new ArrayList<>();

  private float cursorX;
  private float cursorY;
  private float cursorRadius = 0;

  private float width;
  private float height;

  private int targetId = 0;
  private int targetIndex = 0;
  private int closestCircleIndex = -1;

  public ScreenView(Context context) {
    super(context);
    init();
  }

  public ScreenView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public ScreenView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {

    cursorFillPaint = new Paint();
    cursorFillPaint.setColor(CURSOR_FILL_COLOR);
    cursorFillPaint.setStyle(Paint.Style.FILL);
    cursorFillPaint.setAntiAlias(true);


    circleFillPaint = new Paint();
    circleFillPaint.setColor(CIRCLE_FILL_COLOR);
    circleFillPaint.setStyle(Paint.Style.FILL);
    circleFillPaint.setAntiAlias(true);

  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    for (int index = 0; index < circles.size(); index++) {
      Circle circle = circles.get(index);
      if (index == closestCircleIndex) {
        circleFillPaint.setColor(BUBBLE_CIRCLE_FILL_COLOR);
        canvas.drawCircle(circle.x, circle.y, circle.radius, circleFillPaint);
        circleFillPaint.setColor(CIRCLE_FILL_COLOR);
      } else if (index == targetIndex) {
        circleFillPaint.setColor(TARGET_FILL_COLOR);
        canvas.drawCircle(circle.x, circle.y, circle.radius, circleFillPaint);
        circleFillPaint.setColor(CIRCLE_FILL_COLOR);
      } else {
        canvas.drawCircle(circle.x, circle.y, circle.radius, circleFillPaint);
      }
    }

    canvas.drawCircle(cursorX, cursorY, cursorRadius, cursorFillPaint);

  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    width = w;
    height = h;
    cursorX = width / 2;
    cursorY = height / 2;
  }


  public void changeTarget() {
    chooseTarget();
    invalidate();
  }

  public void clean() {
    circles.clear();
    cursorX = width / 2;
    cursorY = height / 2;
    cursorRadius = 0;
    invalidate();
  }

  public void setCursor(float cx, float cy) {
    this.cursorX = cx;
    this.cursorY = cy;
  }

  public ArrayList<Integer> getCirclesBehindCursor() {
    ArrayList<Integer> circlesBehindCursor = new ArrayList<>();
    for (int i = 0; i < circles.size(); i++) {
      Circle circle = circles.get(i);
      if (getDistanceDiff(cursorX, cursorY, circle.x, circle.y) <= (cursorRadius + circle.radius)) {
        circlesBehindCursor.add(i);
      }
    }
    return circlesBehindCursor;
  }

  public int getDistanceDiff(float x1, float y1, float x2, float y2) {
    int x = (int) Math.abs(x1 - x2);
    int y = (int) Math.abs(y1 - y2);
    int distance = (int) Math.sqrt((x * x) + (y * y));
    return distance;
  }

  public int getClosestCircleIndex(ArrayList<Integer> array) {
    float closestDistance = getDistanceDiff(cursorX, cursorY, circles.get(array.get(0)).x, circles.get(array.get(0)).y);
    int index = array.get(0);
    for (int i = 1; i < array.size(); i++) {
      float distance = getDistanceDiff(cursorX, cursorY, circles.get(array.get(i)).x, circles.get(array.get(i)).y);
      if (distance < closestDistance) {
        closestDistance = distance;
        index = array.get(i);
      }
    }
    return index;
  }

  public void increaseCursorRadius() {
    cursorRadius += 2;
  }

  public void decreaseCursorRadius() {
    cursorRadius -= 2;
  }

  public void setCursorSize(float cursorSize) {
    cursorRadius = cursorSize;
  }

  public Circle getCircle(int index) {
    return circles.get(index);
  }

  public void draw(boolean changeClosestCircleColor) {
    createCircles();
    if (changeClosestCircleColor) {
      ArrayList<Integer> circlesBehindCursor = getCirclesBehindCursor();
      if (circlesBehindCursor.size() != 0) {
        closestCircleIndex = getClosestCircleIndex(circlesBehindCursor);
      }
    } else {
      closestCircleIndex = -1;
    }
    invalidate();
  }

  public boolean areTargetAndClosetCircleTheSame() {
    return targetIndex == closestCircleIndex;
  }

  public float getTargetRadius() {
    return circles.get(targetIndex).radius;
  }


  private void createCircles() {
    circles.clear();
    int counter = 0;
    loop:
    while (counter < CIRCLES_COUNT) {
      float radius = (float) ((Math.random() * (cursorRadius * 2)) + (cursorRadius + 5));
      float x = (float) (Math.random() * ((width - (2 * radius)) + 1) + radius);
      float y = (int) ((Math.random() * ((height - (2 * radius))) + 1) + radius);

      for (Circle c : circles) {
        if (getDistanceDiff(c.x, c.y, x, y) < c.radius + radius) {
          continue loop;
        }
      }

      Circle circle = new Circle();
      circle.x = x;
      circle.y = y;
      circle.radius = radius;
      circles.add(circle);
      counter++;
    }
  }

  private void chooseTarget() {
    int target = (int) (Math.random() * circles.size());
    if (target == targetIndex) {
      chooseTarget();
      return;
    }
    targetIndex = target;
    targetId++;
  }

  public float getCursorX() {
    return cursorX;
  }

  public float getCursorY() {
    return cursorY;
  }

  public float getCursorRadius() {
    return cursorRadius;
  }

  public int getTargetId() {
    return targetId;
  }

  public int getTargetIndex() {
    return targetIndex;
  }

  public void setClosestCircleIndex(int bubbleCircleIndex) {
    this.closestCircleIndex = bubbleCircleIndex;
  }

}
