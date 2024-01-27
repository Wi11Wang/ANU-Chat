package wang.bogong.anuchat.Fragments;

import static Moment.Builder.MomentBuilder.FRIENDS;
import static Moment.Builder.MomentBuilder.PRIVATE;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import wang.bogong.anuchat.MomentsActivities.WriteMomentActivity;
import wang.bogong.anuchat.R;
import wang.bogong.anuchat.SearchActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Adapter.MomentAdapter;
import Moment.Content.Moment;

public class MomentsFragment extends Fragment {
    private FloatingActionButton floatingActionButton;
    private Toolbar toolbar;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView momentsRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private MomentAdapter momentAdapter;
    private List<Moment> momentsList;

    private HashSet<String> friendsSet;

    private final String EVERYONE_MOMENT = "Everyone";
    private final String FRIENDS_MOMENT = "Friends";
    private final String PRIVATE_MOMENT = "Private";
    boolean isFromSearch = false;

    private String timelineStatus = EVERYONE_MOMENT;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_moments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        animationSetup();
        linkUIComponents();

        setFloatingActionButtonActions();
        setToolbarMenuActionButtonActions();
        setToolbarLongPressActions();

        setSwipeRefreshLayoutActions();
        initializeMomentsTimeline();
        refreshMomentsTimeline();

        super.onViewCreated(view, savedInstanceState);
    }

    //**************************************************************************
    //                            INITIALIZATION                               *
    //**************************************************************************
    /**
     * Setup floating action button animation with write moments activity.
     */
    private void animationSetup() {
        getActivity().setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        getActivity().setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        getActivity().getWindow().setSharedElementsUseOverlay(false);
    }

    /**
     * Link UI components.
     */
    private void linkUIComponents() {
        floatingActionButton = getActivity().findViewById(R.id.fragment_moments_floatingActionButton);
        toolbar = getActivity().findViewById(R.id.fragment_moments_toolbar);

        swipeRefreshLayout = getActivity().findViewById(R.id.fragment_moments_swipeRefreshLayout);
        momentsRecyclerView = getActivity().findViewById(R.id.fragment_moments_recyclerView);
    }

    //**************************************************************************
    //                         SET BUTTON ACTIONS                              *
    //**************************************************************************
    /**
     * Set actions of floating action button.
     */
    private void setFloatingActionButtonActions() {
        floatingActionButton.setElevation(5);
        floatingActionButton.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), WriteMomentActivity.class);
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), floatingActionButton, "fragment_moments_floatingActionButton").toBundle();
            startActivity(intent, bundle);
        });
    }

    /**
     * Set actions of toolbar menu.
     */
    private void setToolbarMenuActionButtonActions() {
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.fragment_moments_appbar_search) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("search content", "");
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), toolbar, "fragment_moments_toolbar").toBundle();
                startActivity(intent, bundle);
            }

            if (item.getItemId() == R.id.moment_select_menu_everyone) {
                if (!timelineStatus.equals(EVERYONE_MOMENT)) {
                    timelineStatus = EVERYONE_MOMENT;
                    refreshMomentsTimeline();
                }
            }
            if (item.getItemId() == R.id.moment_select_menu_friends) {
                if (!timelineStatus.equals(FRIENDS_MOMENT)) {
                    timelineStatus = FRIENDS_MOMENT;
                    refreshMomentsTimeline();
                }
            }
            if (item.getItemId() == R.id.moment_select_menu_private) {
                if (!timelineStatus.equals(PRIVATE_MOMENT)) {
                    timelineStatus = PRIVATE_MOMENT;
                    refreshMomentsTimeline();
                }
            }
            return true;
        });
    }

    /**
     * Set long press action to toolbar.
     */
    private void setToolbarLongPressActions() {
        final long[] LAST_CLICK_TIME = {0};
        final int mDoubleClickInterval = 400; // Milliseconds

        toolbar.setOnClickListener(v -> {
            long currentClickTime = System.currentTimeMillis();
            if (currentClickTime - LAST_CLICK_TIME[0] <= mDoubleClickInterval) {
                int itemCount = momentsRecyclerView.getAdapter().getItemCount();
                if (itemCount > 20) {
                    momentsRecyclerView.scrollToPosition(itemCount - 20);
                    momentsRecyclerView.smoothScrollToPosition(itemCount - 1);
                }
            }
            else {
                LAST_CLICK_TIME[0] = System.currentTimeMillis();
            }
        });
    }

    //**************************************************************************
    //                           TIME LINE RELEVANT                            *
    //**************************************************************************
    /**
     * Initialize time line in moment activity.
     */
    private void initializeMomentsTimeline() {
        momentsRecyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        momentsRecyclerView.setLayoutManager(linearLayoutManager);

        momentsList = new ArrayList<>();
        momentAdapter = new MomentAdapter(getContext(), momentsList);
        momentsRecyclerView.setAdapter(momentAdapter);

        friendsSet = new HashSet<>();
    }

    /**
     * Get following users.
     */
    private void updateFriendsSet() {
        Set<String> following = new HashSet<>();
        Set<String> follower = new HashSet<>();
        FirebaseDatabase.getInstance().getReference().child("Following").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Add following users to following set.
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    following.add(dataSnapshot.getKey());
                }
                FirebaseDatabase.getInstance().getReference().child("Follower").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Add followers to follower set.
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            follower.add(dataSnapshot.getKey());
                        }

                        friendsSet.clear();

                        // Intersect two sets, and update to friends set.
                        following.retainAll(follower);
                        friendsSet.addAll(following);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    /**
     * Refresh moments timeline.
     */
    private void refreshMomentsTimeline() {
        swipeRefreshLayout.setRefreshing(true);
        FirebaseDatabase.getInstance().getReference().child("Moments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                momentsList.clear();
                updateFriendsSet();
                ArrayList<Moment> scheduledCurrentUserMoments = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Moment moment = dataSnapshot.getValue(Moment.class);
                    String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    /* Logic of adding moments to timeline under status "EVERYONE_MOMENT"
                     * This option shows all moments.
                     *
                     * 1. If sender of moment is not current user:
                     *    1.1 Check visibility of a moment.
                     *        1.1.1 If visibility is null (everyone), go to 1.2.
                     *        1.1.2 If visibility is "FRIENDS", check if the sender of the moment is a
                     *                friend, if is, go to 1.2, else don't add this moment to timeline.
                     *        1.1.3 If visibility is "PRIVATE", don't add this moment to timeline.
                     *    1.2 Check if schedule of moment is finished.
                     *        1.2.1 If schedule is not finished, don't add this moment to timeline.
                     *        1.2.2 If schedule if finished, add this moment to scheduledCurrentUserMoments
                     *                for reorder the timeline later.
                     *    1.3 If moment is not classified as don't show in timeline: add to timeline
                     * 2. If sender is current user:
                     *    2.1 Check if schedule is finished.
                     *        2.1.1 Schedule is finished: add to timeline
                     *        2.1.2 Schedule is not finished: add to scheduledCurrentUserMoments
                     *              for reorder the timeline later.
                     * 3. combine moment list and scheduled list so that the timeline will display inorder.
                     */
                    if (timelineStatus.equals(EVERYONE_MOMENT)) {
                        if (!currentUserID.equals(moment.getSenderID())) {
                            boolean showInTimeline = true;

                            // Check visibility.
                            String visibility = moment.getVisibility();
                            if (visibility != null) {
                                if (visibility.equals(FRIENDS)) {
                                    // Check if friends.
                                    if (!friendsSet.contains(currentUserID)) {
                                        showInTimeline = false;
                                    }
                                } else if (visibility.equals(PRIVATE)) {
                                    showInTimeline = false;
                                }
                            }

                            // Check if schedule finished.
                            if (!moment.isScheduleFinished()) {
                                showInTimeline = false;
                            } else {
                                if (moment.getSchedule() != null) {
                                    scheduledCurrentUserMoments.add(moment);
                                }
                            }

                            if (showInTimeline) {
                                momentsList.add(moment);
                            }
                        } else {
                            if (moment.getSchedule() != null) {
                                scheduledCurrentUserMoments.add(moment);
                            } else {
                                momentsList.add(moment);
                            }
                        }
                    /* Logic of adding moments to timeline under status "FRIENDS_MOMENT"
                     * This option only shows friends moment.
                     *
                     * 1. Make sure this moment is not sent by current user and sender of the moment is a friend of current user.
                     *    1.1 Make sure visibility of moment is "PUBLIC" or "FRIENDS"
                     *        1.1.1 Make sure schedule is finished or no schedule.
                     */
                    } else if (timelineStatus.equals(FRIENDS_MOMENT)) {
                        if (!moment.getMomentID().equals(currentUserID) && friendsSet.contains(moment.getSenderID())) {
                            if (moment.getVisibility() == null || moment.getVisibility().equals(FRIENDS)) {
                                if (moment.getSchedule() != null) {
                                    if (moment.isScheduleFinished()) {
                                        scheduledCurrentUserMoments.add(moment);
                                    }
                                } else {
                                    momentsList.add(moment);
                                }
                            }
                        }
                    /* Logic of adding moments to timeline user status "PRIVATE_MOMENT"
                     * 1. Make sure sender of the moment is current user.
                     *    1.1 Make sure visibility of current moment is private.
                     *        1.1.1 Based on schedule info, add to momentList or scheduled list.
                     */
                    } else {
                        if (currentUserID.equals(moment.getSenderID())) {
                            if (moment.getVisibility() != null && moment.getVisibility().equals(PRIVATE)) {
                                if (moment.getSchedule() != null) {
                                    scheduledCurrentUserMoments.add(moment);
                                } else {
                                    momentsList.add(moment);
                                }
                            }
                        }
                    }
                }

                try {
                    updateMomentsList((ArrayList<Moment>) momentsList, scheduledCurrentUserMoments);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                swipeRefreshLayout.setRefreshing(false);
                momentAdapter.notifyDataSetChanged();
                linearLayoutManager.scrollToPosition(momentAdapter.getItemCount() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Adding scheduled moments to moments list.
     *
     * @param scheduledMoments Scheduled moments.
     * @throws ParseException Error when parsing time.
     */
    private void updateMomentsList(ArrayList<Moment> sortedMoments, ArrayList<Moment> scheduledMoments) throws ParseException {
        for (Moment scheduledMoment : scheduledMoments) {
            Date schedule = scheduledMoment.getSchedule().getTime();
            int left = 0;
            int right = sortedMoments.size();
            while (left < right) {
                int middle = (left + right) / 2;
                Date sortedMomentTimeStamp = sortedMoments.get(middle).getTimestamp().getTime();
                if (schedule.after(sortedMomentTimeStamp)) {
                    left = middle + 1;
                } else {
                    right = middle;
                }
            }
            sortedMoments.add(left, scheduledMoment);
        }
    }
    /**
     * Set swipe to refresh layout actions.
     *
     * Current actions: swipe to obtain data in firebase.
     */
    private void setSwipeRefreshLayoutActions() {
        int anu_logo_yellow = ContextCompat.getColor(getActivity(), R.color.anu_logo_yellow);
        int anu_original_dark = ContextCompat.getColor(getActivity(), R.color.anu_original_dark);
        swipeRefreshLayout.setColorSchemeColors(anu_logo_yellow, anu_original_dark);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshMomentsTimeline();
            swipeRefreshLayout.setRefreshing(false);
        });
    }
}