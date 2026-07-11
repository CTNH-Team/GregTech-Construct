package slimeknights.tconstruct.library.tools.helper;

public class Marker {
    protected int count;

    public Marker(){ count = 0; }

    public void mark(){
        count++;
    }

    public boolean free(){
        if (count > 0){
            count--;
            return true;
        } else return false;
    }

    public boolean marked(){
        return count > 0;
    }

    public int count(){
        return count;
    }

    public int clean(){
        int result = count;
        count = 0;
        return result;
    }
}
