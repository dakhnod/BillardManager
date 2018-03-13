package d.d.billardmanager;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;

public class PoolTable extends Prop {
    //public boolean occupied = false;
    public final int identifier = 0;

    ImageView tableImage, deleteView;
    TextView tableTime, reservationTime;
    LinearLayout textLayout;

    long nextReservation = -1;

    String reservationTimeString;

    //public static final int shadowResource = R.drawable.pool_table_top_shadow_only;

    public void setTimeText(String text){
        tableTime.setText(text);
    }

    public boolean isOccupied(){
        return status == 1;
    }

    public void setOccupied(boolean occupied){
        status = occupied ? 1 : 0;

        if (occupied) {
            start = System.currentTimeMillis();
            tableTime.setVisibility(View.VISIBLE);
            tableTime.setText("00:00:00");
        } else {
            start = 0;
        }
        setImageResource(occupied ? R.drawable.pool_table_top_occ : R.drawable.pool_table_top2);
    }

    public void handleReservation(int hours, int mins){
        reservationTimeString = (hours < 10 ? "0" : "") + hours + (mins < 10 ? ":0" : ":") + mins;
        reservationTime.setText(reservationTimeString);
        reservationTime.setVisibility(VISIBLE);

        nextReservation = hours * 60 + mins;
    }


    @Override
    public void setRotation(float r){
        super.setRotation(r);
        deleteView.setRotation(-r);
    }

    @Override
    public void handleTimeEvent(Handler mainHandler){
        if(nextReservation != -1) {
            Calendar c = Calendar.getInstance();
            long time = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
            long dif = nextReservation - time;
            if (time - nextReservation > 15) {
                mainHandler.post(() -> {
                        cancelReservation();
                        tableImage.setImageResource(R.drawable.pool_table_top2);
                });
            } else if (dif <= 60) {
                mainHandler.post(() -> {
                        tableImage.setImageResource(R.drawable.pool_table_top_pending);
                        reservationTime.setText(reservationTimeString + " -" + dif);
                });
            }
        }

        if (!isOccupied() || start == 0) {
            return;
        }
        long dif = (System.currentTimeMillis() - start) / 1000;
        long secs = dif % 60;
        long mins = (dif / 60) % 60;
        long hours = dif / 3600;
        final String timeString = (hours < 10 ? "0" : "") + hours + (mins < 10 ? ":0" : ":") + mins + (secs < 10 ? ":0" : ":") + secs;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                setTimeText(timeString);
            }
        });
    }

    void cancelReservation(){
        nextReservation = -1;
        reservationTime.setVisibility(GONE);
    }

    void initViews(Context context){
        //tableImage = findViewById(R.id.tableView);
        //tableTime = findViewById(R.id.tableTimeText);

        Drawable drawable = getResources().getDrawable(R.drawable.pool_table_top2);

        final float scale = getContext().getResources().getDisplayMetrics().density;
        //this.width = (int) (100 * scale + 0.5f);
        this.width = 300;
        this.height = (int)((float)width * ((float) drawable.getMinimumHeight() / (float) drawable.getMinimumWidth()));
        RelativeLayout.LayoutParams params = new LayoutParams(width, height);
        params.addRule(CENTER_IN_PARENT, TRUE);

        tableImage = new ImageView(context);
        tableImage.setImageDrawable(drawable);
        tableImage.setLayoutParams(params);
        addView(tableImage);

        textLayout = new LinearLayout(context);
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        textParams.addRule(CENTER_IN_PARENT, TRUE);
        textLayout.setLayoutParams(textParams);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        //textLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        addView(textLayout);

        tableTime = new TextView(context);
        tableTime.setTextSize(20);
        tableTime.setTextColor(Color.BLACK);
        //tableTime.setVisibility(GONE);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //params2.addRule(CENTER_IN_PARENT, TRUE);
        params2.gravity = Gravity.CENTER_HORIZONTAL;
        tableTime.setLayoutParams(params2);
        textLayout.addView(tableTime);

        reservationTime = new TextView(context);
        reservationTime.setTextSize(16);
        reservationTime.setTextColor(Color.BLACK);
        reservationTime.setVisibility(GONE);
        LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params4.gravity = Gravity.CENTER_HORIZONTAL;
        //params4.addRule(CENTER_HORIZONTAL, TRUE);
        //params4.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        reservationTime.setLayoutParams(params4);
        textLayout.addView(reservationTime);

        deleteView = new ImageView(context);
        deleteView.setImageResource(R.drawable.delete);
        RelativeLayout.LayoutParams params3 = new LayoutParams(80, 80);
        params3.addRule(CENTER_IN_PARENT, TRUE);
        deleteView.setLayoutParams(params3);
        deleteView.setVisibility(INVISIBLE);
        addView(deleteView);
    }

    public void setImageResource(int r){
        tableImage.setImageResource(r);
    }

    public PoolTable(Context context) {
        super(context);
        initViews(context);
        this.propType = PropType.TYPE_POOLTABLE;
        this.shadowResource = R.drawable.pool_table_top_shadow_only;
    }

    public PoolTable(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
        this.propType = PropType.TYPE_POOLTABLE;
    }

    public PoolTable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
        this.propType = PropType.TYPE_POOLTABLE;
    }
}
