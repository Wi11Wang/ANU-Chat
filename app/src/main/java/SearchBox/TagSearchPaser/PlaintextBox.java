package SearchBox.TagSearchPaser;

import java.util.ArrayList;

public class PlaintextBox {
    ArrayList<String> textList;

    public PlaintextBox(ArrayList<String> andList) {
        this.textList = andList;
    }

    public PlaintextBox() {}

    public ArrayList<String> getPlaintextBox() {
        return textList;
    }

    public void addPlain(String txt) {
        textList.add(txt);
    }

    public int size() {
        return textList.size();
    }

    public String textBoxToString() {
        return String.join("", textList);
    }
}
