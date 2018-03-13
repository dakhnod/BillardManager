package d.d.billardmanager;

/**
 * Created by Sound on 09.01.2018.
 */

public enum PropType {
    TYPE_POOLTABLE(0);

    final private int id;

    PropType(int id){
        this.id = id;
    }

    public int getId(){
        return this.id;
    }
}
