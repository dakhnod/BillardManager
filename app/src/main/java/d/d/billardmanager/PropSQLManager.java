package d.d.billardmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class PropSQLManager extends SQLiteOpenHelper {
    Context context;
    SQLiteDatabase database;

    public PropSQLManager(Context context) {
        super(context, "Props.sqlite", null, 1);
        this.context = context;
        database = getWritableDatabase();
        database.execSQL("CREATE TABLE IF NOT EXISTS Props (id INTEGER NOT NULL PRIMARY KEY, type INT, number INTEGER, position_x INTEGER, position_y INTEGER, rotation INTEGER, status INTEGER, time_start LONG);");
    }


    public Prop[] getProps() {
        Cursor c = database.rawQuery("SELECT * FROM Props;", null);
        int count = c.getCount();
        Prop[] props = new Prop[count];
        if (count > 0) {
            c.moveToFirst();
            for (int i = 0; i < count; i++) {
                int type = c.getInt(c.getColumnIndex("type"));
                if(type == PropType.TYPE_POOLTABLE.getId()){
                    //PoolTable table = (PoolTable) inflater.inflate(R.layout.pooltable, null);
                    PoolTable table = new PoolTable(context);
                    table.id = c.getInt(c.getColumnIndex("id"));
                    table.setX(c.getInt(c.getColumnIndex("position_x")));
                    table.setY(c.getInt(c.getColumnIndex("position_y")));
                    table.setOccupied(c.getInt(c.getColumnIndex("status")) == 1);
                    table.start = c.getLong(c.getColumnIndex("time_start"));
                    table.setRotation(c.getInt(c.getColumnIndex("rotation")));
                    
                    props[i] = table;
                    c.moveToNext();
                }
            }
        }
        c.close();
        return props;
    }

    @Override
    public void close(){
        database.close();
        super.close();
    }

    public void insertProp(Prop p){
        ContentValues values = new ContentValues();
        values.put("type", p.propType.getId());
        values.put("status", p.status);
        values.put("position_x", p.getX());
        values.put("position_y", p.getY());
        values.put("number", p.number);
        values.put("rotation", p.getRotation());
        if(p.status != 0){
            values.put("time_start", p.start);
        }
        if(p.id == -1){
            p.id = database.insert("Props", null, values);
        }else{
            database.update("Props", values, "id = " + p.id, null);
        }
    }

    public void deleteProp(Prop p){
        database.delete("Props", "id=" + p.id, null);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
