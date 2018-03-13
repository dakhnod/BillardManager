package d.d.billardmanager;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Sound on 09.01.2018.
 */

public abstract class Prop extends RelativeLayout {
    protected PropType propType;

    public long id = -1;

    public int number = 0;
    public long start = 0;

    public int width, height;

    public int shadowResource = -1;

    public int status;

    abstract void handleTimeEvent(Handler mainHandler);

    protected Prop(Context context) {
        super(context);
    }

    protected Prop(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected Prop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
