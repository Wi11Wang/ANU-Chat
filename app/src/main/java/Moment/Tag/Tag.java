package Moment.Tag;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Tag implements Comparable<Tag> {

    private final String tag;
    private ArrayList<String> momentIds = new ArrayList<>();

//    public Tag() {}

    public Tag(String tag) {
        this.tag = tag;
    }

    public Tag(String tag, ArrayList<String> momentIds) {
        this.tag = tag;
        this.momentIds = momentIds;
    }

    public String getTag() {
        return tag;
    }

    public ArrayList<String> getMomentIds() {
        return momentIds;
    }

    @Override
    public int compareTo(Tag tag) {
        String thisTag = this.getTag();
        String tagTocomp = tag.getTag();
        return thisTag.compareTo(tagTocomp);
    }

    @Override
    public String toString() {
        return "Tag{" +
                "tag='" + tag + '\'' +
                ", momentIds=" + momentIds +
                '}';
    }
}
