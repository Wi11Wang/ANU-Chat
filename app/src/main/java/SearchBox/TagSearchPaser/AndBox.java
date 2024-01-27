package SearchBox.TagSearchPaser;

import java.util.ArrayList;

public class AndBox {
    ArrayList<String> andList;

    public AndBox(ArrayList<String> andList) {
        this.andList = andList;
    }

    public AndBox() {}

    public ArrayList<String> getAndList() {
        return andList;
    }

    public void addAnd(String txt) {
        andList.add(txt);
    }

    public int size() {
        return andList.size();
    }

    public String andBoxToString() {
        return String.join("", andList);
    }
}
