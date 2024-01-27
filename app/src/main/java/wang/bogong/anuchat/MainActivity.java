package wang.bogong.anuchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.Window;

import wang.bogong.anuchat.Fragments.ChatsFragment;
import wang.bogong.anuchat.Fragments.ContactsFragment;
import wang.bogong.anuchat.Fragments.MeFragment;
import wang.bogong.anuchat.Fragments.MomentsFragment;

import wang.bogong.anuchat.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private final Fragment momentsFragment = new MomentsFragment();
    private final Fragment chatsFragment = new ChatsFragment();
    private final Fragment contactsFragment = new ContactsFragment();
    private final Fragment meFragment = new MeFragment();

    private Fragment activeFragment = momentsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable transition animation.
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();

        bottomNavigationView = findViewById(R.id.activity_main_bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_moments:
                    fragmentManager.beginTransaction().hide(activeFragment).show(momentsFragment).commit();
                    activeFragment = momentsFragment;
                    return true;
                case R.id.navigation_chats:
                    //After coming back from other fragment, the Chats fragment will be updated.
                    fragmentManager.beginTransaction().hide(activeFragment).show(chatsFragment).detach(chatsFragment).commitNow();
                    fragmentManager.beginTransaction().attach(chatsFragment).commitNow();
                    activeFragment = chatsFragment;
                    return true;
                case R.id.navigation_contacts:
                    fragmentManager.beginTransaction().hide(activeFragment).show(contactsFragment).commit();
                    activeFragment = contactsFragment;
                    return true;
                case R.id.navigation_me:
                    fragmentManager.beginTransaction().hide(activeFragment).show(meFragment).commit();
                    activeFragment = meFragment;
                    return true;
            }

            return false;
        });

        fragmentManager.beginTransaction().add(R.id.fragment_container, meFragment, "me").hide(meFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, contactsFragment, "contacts").hide(contactsFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, chatsFragment, "chats").hide(chatsFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, momentsFragment, "moments").commit();
    }
}