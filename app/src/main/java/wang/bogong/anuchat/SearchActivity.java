package wang.bogong.anuchat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import wang.bogong.anuchat.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Adapter.MomentAdapter;
import SearchBox.TagSearchPaser.SearchBoxes;

public class SearchActivity extends AppCompatActivity {
    MaterialToolbar toolbar;
    SearchView searchView;
    RecyclerView recyclerView;
    SearchBoxes searchBoxes;

    LinearLayoutManager linearLayoutManager;
    List<Moment.Content.Moment> resultMoments = new ArrayList<>();
    MomentAdapter momentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Enable transition animation.
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));
        setContentView(R.layout.activity_search);
        super.onCreate(savedInstanceState);

        searchBoxes = new SearchBoxes();

        // Set up animation.
        animationSetUp();
        linkUIComponents();
        setToolbar();
        setQuerySubmitActions();
        displaySearchResults();

        try {
            String searchContent;
            searchContent = getIntent().getStringExtra("search content");
            if (!searchContent.equals("")) {
                searchView.setQuery(searchContent, false);
                searchView.requestFocus();
            }
        } catch (Exception ignored) { }
    }

    /**
     * Setup search button animation with search activity.
     */
    private void animationSetUp() {
        findViewById(android.R.id.content).setTransitionName("fragment_moments_toolbar");
        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());

        MaterialContainerTransform transform = new MaterialContainerTransform();
        transform.addTarget(android.R.id.content);
        transform.setDuration(400);

        getWindow().setSharedElementEnterTransition(transform);
        getWindow().setSharedElementReturnTransition(transform);
    }

    /**
     * Link UI components.
     */
    private void linkUIComponents() {
        toolbar = findViewById(R.id.activity_search_toolbar);
        searchView = findViewById(R.id.activity_search_searchView);
        recyclerView = findViewById(R.id.activity_search_resultRecyclerView);
    }

    private void setToolbar() {
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void displaySearchResults() {
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        momentAdapter = new MomentAdapter(this, resultMoments);
        recyclerView.setAdapter(momentAdapter);
    }

    private void setQuerySubmitActions() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search action here
                // add search result to "resultMoments"
                // TODO: Changes made here
                try {
                    searchBoxes.updateSearchContent(query.toLowerCase());
                    ArrayList<String> result = searchBoxes.returnSearchResult(); // Xinyi - it returns searching result
                    updateResultMoments(result);
                    momentAdapter.notifyDataSetChanged();
                } catch (Exception ignored) {
                    Toast.makeText(SearchActivity.this, "No result for " + query, Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateResultMoments(List<String> momentIDs) {
        if (momentIDs == null) {
            Toast.makeText(SearchActivity.this,"Input not Valid! Please enter in a proper way!",Toast.LENGTH_SHORT).show();
        }
        else {
            resultMoments.clear();
            for (String momentID : momentIDs) {
                FirebaseDatabase.getInstance().getReference().child("Moments").child(momentID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        resultMoments.add(snapshot.getValue(Moment.Content.Moment.class));
                        momentAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        }
    }
}