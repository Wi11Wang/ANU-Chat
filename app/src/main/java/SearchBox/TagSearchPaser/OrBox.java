package SearchBox.TagSearchPaser;

import java.util.ArrayList;

public class OrBox {
    ArrayList<String> orList;

    public OrBox(ArrayList<String> andList) {
        this.orList = andList;
    }

    public OrBox() {}

    public ArrayList<String> getOrList() {
        return orList;
    }

    public void addOr(String txt) {
        orList.add(txt);
    }

    public int size() {
        return orList.size();
    }

    public String orBoxToString() {
        return String.join("", orList);
    }

}
