package User;

import android.content.Intent;
import android.widget.EditText;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Bio {


    private String fullName;
    private String nickName;
    private String gradYear;
    private College college;
    private Gender gender;
    private String age;
    private String id;

    //default constructor required for calls to DataSnapshot.getValue(Bio.class)
    public Bio(){};


    public Bio(String id, String age, String fullName, College college, String gradYear) {

        this.college = college;
        this.age = age;
        this.fullName = fullName;
        this.id=id;
        this.gradYear = gradYear;
    }

    public String getGradYear() {
        return gradYear;
    }
    public void setGradYear(String gradYear) {
        this.gradYear = gradYear;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public College getCollege() {
        return college;
    }

    public void setCollege() {
        this.college = college;
    }


}
