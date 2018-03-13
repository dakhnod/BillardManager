package d.d.billardmanager;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends Activity implements View.OnTouchListener, View.OnClickListener, View.OnLongClickListener {
    int deltaX, deltaY;
    float x2 = -1, y2 = -1, x, y;
    boolean editMode = false;
    volatile boolean countTime;
    Thread timeThread;
    ImageView shadow = null;

    long tapTime;

    ArrayList<Prop> tables;

    PropSQLManager sql;

    RelativeLayout root;

    Prop editProp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        sql = new PropSQLManager(this);

        Prop[] props = sql.getProps();
        tables = new ArrayList<>(props.length);

        root = findViewById(R.id.root);
        for (Prop p : props) {
            tables.add(p);
            root.addView(p);
            p.setOnClickListener(this);
            p.setOnLongClickListener(this);
        }
        findViewById(R.id.rotate).bringToFront();
        findViewById(R.id.wrehcn).bringToFront();
        findViewById(R.id.plus).bringToFront();
        findViewById(R.id.clock).bringToFront();

        findViewById(R.id.wrehcn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMode = !editMode;
                AlphaAnimation alpha = new AlphaAnimation(editMode ? 0 : 1, editMode ? 1 : 0);
                alpha.setDuration(300);
                alpha.setFillAfter(true);
                for (final Prop t : tables) {
                    t.setOnClickListener(editMode ? null : MainActivity.this);
                    t.setOnLongClickListener(editMode ? null : MainActivity.this);
                    t.setOnTouchListener(editMode ? MainActivity.this : null);

                    if (t instanceof PoolTable) {
                        ((PoolTable) t).deleteView.setVisibility(View.VISIBLE);
                        ((PoolTable) t).deleteView.setAnimation(alpha);
                        ((PoolTable) t).textLayout.setVisibility(editMode ? View.GONE : View.VISIBLE);
                    }
                }
                ((ImageView) view).setImageResource(editMode ? R.drawable.tick : R.drawable.wrench);
                if (editMode) {
                    findViewById(R.id.gridView).setVisibility(View.VISIBLE);
                    findViewById(R.id.gridView).startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in_grid));
                    findViewById(R.id.plus).setVisibility(View.VISIBLE);
                    findViewById(R.id.rotate).setVisibility(View.VISIBLE);
                    findViewById(R.id.plus).startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in_plus));
                    findViewById(R.id.rotate).startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in_rotate));
                    if (shadow == null) {
                        shadow = new ImageView(MainActivity.this);
                    }
                    root.addView(shadow);
                } else {
                    Animation fade_out_rotate = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out_rotate);
                    Animation fade_out_plus = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out_plus);
                    Animation fade_out_grid = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out_grid);
                    fade_out_plus.setFillAfter(true);
                    fade_out_rotate.setFillAfter(true);
                    fade_out_grid.setFillAfter(true);

                    findViewById(R.id.plus).startAnimation(fade_out_plus);
                    findViewById(R.id.rotate).startAnimation(fade_out_rotate);
                    findViewById(R.id.gridView).startAnimation(fade_out_grid);
                    root.removeView(shadow);
                    shadow = null;
                    editProp = null;
                }
            }
        });

        findViewById(R.id.rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.this.editProp != null){
                    editProp.setRotation(editProp.getRotation() == 90 ? 0 : 90);
                }
            }
        });

        findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PoolTable t = new PoolTable(MainActivity.this);
                if(editProp != null){
                    t.setRotation(editProp.getRotation());
                }
                if(editMode){
                    t.deleteView.setVisibility(View.VISIBLE);
                }
                tables.add(t);
                root.addView(t);
                editProp = t;
                if (x2 != -1) {
                    float tY = y;
                    float tX = x;
                    x = x + (x - x2);
                    y = y + (y - y2);
                    y2 = tY;
                    x2 = tX;
                    t.setX(x);
                    t.setY(y);
                } else {
                    x2 = x;
                    y2 = y;
                }
                checkPosition(t);
                x = t.getX();
                y = t.getY();
                findViewById(R.id.wrehcn).bringToFront();
                findViewById(R.id.plus).bringToFront();
                t.setOnTouchListener(MainActivity.this);
             }
        });
    }


    void checkPosition(Prop p) {
        int maxX = root.getWidth() - p.width;
        int maxY = root.getHeight() - p.height;
        if (p.getX() > maxX) {
            p.setX(maxX);
        } else if (p.getX() < 0) {
            p.setX(0);
        }
        if (p.getY() > maxY) {
            p.setY(maxY);
        } else if (p.getY() < 0) {
            p.setY(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Prop p : tables) {
            sql.insertProp(p);
        }
        sql.close();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view == shadow) {
            return false;
        }
        int action = motionEvent.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            tapTime = System.currentTimeMillis();
            deltaY = (int) (motionEvent.getRawY() - view.getY());
            deltaX = (int) (motionEvent.getRawX() - view.getX());
            int x = (int) (motionEvent.getRawX() - deltaX);
            int y = (int) (motionEvent.getRawY() - deltaY);
            if (view instanceof Prop) {
                editProp = (Prop)view;
                if (((Prop) view).shadowResource != -1) {
                    shadow.setImageResource(((Prop) view).shadowResource);
                    shadow.setLayoutParams(new RelativeLayout.LayoutParams(view.getWidth(), view.getHeight()));
                    shadow.setVisibility(View.VISIBLE);
                    shadow.setRotation(view.getRotation());
                    shadow.setX(x + (x - 960 + shadow.getWidth() / 2) / 15);
                    shadow.setY(y + (y - 540 + shadow.getHeight() / 2) / 15);
                    shadow.setAlpha(0.7f);
                    shadow.bringToFront();
                }
            }
            view.bringToFront();
        } else if (action == MotionEvent.ACTION_UP) {
            shadow.setVisibility(View.GONE);
            if (System.currentTimeMillis() - tapTime < 200) {
                tables.remove(view);
                root.removeView(view);
                if (view instanceof Prop) {
                    sql.deleteProp((Prop) view);
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            x = (int) (motionEvent.getRawX() - deltaX);
            y = (int) (motionEvent.getRawY() - deltaY);
            x -= x % (960 / 30);
            y -= y % (540 / 30);
            view.setX(x);
            view.setY(y);
            if (view instanceof Prop) {
                checkPosition((Prop) view);
            }
            x = view.getX();
            y = view.getY();
            shadow.setX(x + (x - 960 + shadow.getWidth() / 2) / 15);
            shadow.setY(y + (y - 540 + shadow.getHeight() / 2) / 15);
        }
        return true;
    }

    Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            countTime = true;
            Handler mainHandler = new Handler(getMainLooper());
            while (countTime) {
                for (final Prop t : tables) {
                    t.handleTimeEvent(mainHandler);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (timeThread != null) {
            //timeThread.stop();
            countTime = false;
            super.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        (timeThread = new Thread(timeRunnable)).start();
    }

    @Override
    public void onClick(View view) {
        if (view instanceof Prop) {
            if (((Prop) view).propType == PropType.TYPE_POOLTABLE) {
                PoolTable table = (PoolTable) view;
                table.setOccupied(!table.isOccupied());
            }
        }
    }

    @Override
    public boolean onLongClick(final View view) {
        if (view instanceof PoolTable) {
            PopupMenu menu = new PopupMenu(this, view);
            menu.getMenu().add("reservieren");
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getTitle().equals("reservieren")) {
                        showReservationDialog((Prop) view);
                    }
                    return false;
                }
            });
            menu.show();
        }
        return true;
    }

    void showReservationDialog(final Prop p) {
        Calendar c = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                if (p instanceof PoolTable) {
                    ((PoolTable) p).handleReservation(i, i1);
                }
            }
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        dialog.show();
    }
}
