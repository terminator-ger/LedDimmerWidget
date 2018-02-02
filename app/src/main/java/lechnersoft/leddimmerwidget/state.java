package lechnersoft.leddimmerwidget;

/**
 * Created by Michael on 29 Jan 2018.
 */

public class state{
    private state(){};
    private static state s;
    public  static state instance() {
        if (s == null) {
            s = new state();
        }
        return s;
    }

    public boolean ON = false;
}