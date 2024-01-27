package User;

import java.util.Set;

public class User {

    public String id;
    public String firebaseID;
    public String avatarURL;
    public String nickName;
    public Bio bio;
    public Gender gender;
    public College college;
    public Set<Integer> follower;
    public Set<Integer> following;

    public User(){}

    public User(String id, String firebaseID, String avatarURL, String nickName, Gender gender) {
        this.id = id;
        this.firebaseID = firebaseID;
        this.avatarURL = avatarURL;
        this.nickName = nickName;
        this.gender = gender;
    }

    public College getCollege() {return college;}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirebaseID() {
        return firebaseID;
    }

    public void setFirebaseID(String firebaseID) {
        this.firebaseID = firebaseID;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Bio getBio() {
        return bio;
    }

    public void setBio(Bio bio) {
        this.bio = bio;
    }

    public Set<Integer> getFollower() {
        return follower;
    }

    public void setFollower(Set<Integer> follower) {
        this.follower = follower;
    }

    public Set<Integer> getFollowing() {
        return following;
    }

    public void setFollowing(Set<Integer> following) {
        this.following = following;
    }

    public Gender getGender()
    {
        return gender;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firebaseID='" + firebaseID + '\'' +
                ", avatarURL='" + avatarURL + '\'' +
                ", nickName='" + nickName + '\'' +
                ", bio=" + bio +
                ", gender=" + gender +
                ", follower=" + follower +
                ", following=" + following +
                '}';
    }
}
