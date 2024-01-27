package Adapter;

import static Moment.MomentViewModel.displaySenderSpecificComponents;
import static Moment.MomentViewModel.setAvatarClickActions;
import static Moment.MomentViewModel.setHashTagClickActions;
import static Moment.MomentViewModel.setLikeButtonActions;
import static Moment.MomentViewModel.setLinkClickActions;
import static Moment.MomentViewModel.setLocationDisplay;
import static Moment.MomentViewModel.setMomentImageDisplay;
import static Moment.MomentViewModel.setNumberOfComments;
import static Moment.MomentViewModel.setTextContentDisplay;
import static Moment.MomentViewModel.setTimeDisplayAndActions;
import static Moment.MomentViewModel.setNumberOfLikes;
import static Moment.MomentModel.deleteMoment;
import static User.Gender.FEMALE;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import wang.bogong.anuchat.MomentsActivities.MomentDetailActivity;
import wang.bogong.anuchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialTextView;

import java.text.ParseException;
import java.util.List;

import Moment.Content.Moment;
import User.User;

/**
 * @author Bogong Wang
 * @project Android Project
 * @created 29 / 09 / 2021 - 08:41
 */
public class MomentAdapter extends RecyclerView.Adapter<MomentAdapter.ViewHolder> {

    private Context context;
    private List<Moment> moments;

    private FirebaseUser user;

    public MomentAdapter(Context context, List<Moment> moments) {
        this.context = context;
        this.moments = moments;

        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.moment_cardview, parent, false);
        return new MomentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Moment moment = moments.get(position);
        holder.setIsRecyclable(false);

        // Display user name and avatar
        FirebaseDatabase.getInstance().getReference().child("User").child(moment.getSenderID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user == null) {
                    holder.userNameTextView.setText("Deleted Account");
                    holder.avatarImageView.setImageResource(R.drawable.ic_delete);
                } else {
                    holder.userNameTextView.setText(user.getNickName());
                    if (!user.getAvatarURL().equals("")) {
                        Glide.with(context).load(user.getAvatarURL()).into(holder.avatarImageView);
                    } else {
                        if (user.gender.equals(FEMALE)) {
                            holder.avatarImageView.setImageResource(R.drawable.default_avatar_female);
                        } else {
                            holder.avatarImageView.setImageResource(R.drawable.default_avatar_male);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Display time information.
        try {
            setTimeDisplayAndActions(holder.timeAgoTextView, moment);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Display location information.
        setLocationDisplay(holder.locationTextView, holder.locationIconImageView, moment);

        // Get post image.
        setMomentImageDisplay(context, holder.postImageImageView, moment);

        // Display text content.
        setTextContentDisplay(holder.contentTextView, moment);

        // Display sender exclusive message.
        if (moment.getSenderID().equals(user.getUid())) {
            displaySenderSpecificComponents(holder.moreImageButton,
                    holder.visibilityImageView,
                    holder.scheduleImageView,
                    holder.scheduleTextView,
                    moment);
            setMenuClickActions(holder.moreImageButton, moment);
        }

        setLikeButtonActions(holder.likeImageButton, moment);
        setNumberOfLikes(holder.likeCountTextView, moment);

        setCommentButtonActions(moment, holder.commentImageButton, holder.baseCardView);
        setNumberOfComments(holder.commentCountTextView, moment);

        setHashTagClickActions(context, holder.contentTextView);

        setAvatarClickActions(context, holder.avatarImageView, moment.getSenderID());
        setLinkClickActions(context, holder.contentTextView);
        setCardClickActions(holder.baseCardView, moment);
    }


    /**
     * This method shows menu when click more button.
     *
     * @param moreImageButton "more" button.
     * @param moment Current moment.
     */
    private void setMenuClickActions(ImageView moreImageButton,
                                            Moment moment) {
        moreImageButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.moment_more_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.moment_menu_delete) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle("Delete this moment?");
                    alertDialogBuilder.setMessage("This process cannot withdraw.");
                    alertDialogBuilder.setPositiveButton("Delete", (dialog, which) -> {
                        deleteMoment(moment);
                    });
                    alertDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {});
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.alert_red));
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.anu_logo_yellow));
                }
                return true;
            });
            popupMenu.show();
        });
    }

    private void setCommentButtonActions(Moment moment, ImageButton imageButton, CardView cardView) {
        String momentID = moment.getMomentID();
        imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, MomentDetailActivity.class);
            intent.putExtra("momentID", momentID);
            intent.putExtra("source", "comment");
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) context, cardView, "moment_cardview_baseCard").toBundle();
            context.startActivity(intent, bundle);
        });
    }

    private void setCardClickActions(CardView cardView, Moment moment) {
        String momentID = moment.getMomentID();
        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MomentDetailActivity.class);
            intent.putExtra("momentID", momentID);
            intent.putExtra("source", "moment");
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) context, cardView, "moment_cardview_baseCard").toBundle();
            context.startActivity(intent, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return moments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView baseCardView;

        public ImageView avatarImageView;
        public ImageView visibilityImageView;
        public ImageView scheduleImageView;
        public ImageView locationIconImageView;
        public ImageView postImageImageView;
        public ImageButton likeImageButton;
        public ImageButton commentImageButton;
        public ImageView repostImageView;
        public ImageButton moreImageButton;

        public TextView userNameTextView;
        public TextView scheduleTextView;
        public TextView timeAgoTextView;
        public TextView locationTextView;
        public TextView likeCountTextView;
        public TextView commentCountTextView;

        public SocialTextView contentTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            baseCardView = itemView.findViewById(R.id.moment_cardview_baseCard);

            avatarImageView = itemView.findViewById(R.id.moment_cardview_avatarShapeableImageView);
            visibilityImageView = itemView.findViewById(R.id.moment_cardview_visibilityImageView);
            scheduleImageView = itemView.findViewById(R.id.moment_cardview_scheduleImageView);
            locationIconImageView = itemView.findViewById(R.id.moment_cardview_locationIconImageView);
            postImageImageView = itemView.findViewById(R.id.moment_cardview_postImageShapeableImageView);
            likeImageButton = itemView.findViewById(R.id.moment_cardview_likeImageView);
            commentImageButton = itemView.findViewById(R.id.moment_cardview_commentImageView);
            repostImageView = itemView.findViewById(R.id.moment_cardview_repostImageView);
            moreImageButton = itemView.findViewById(R.id.moment_cardview_moreImageView);

            userNameTextView = itemView.findViewById(R.id.moment_cardview_userNameTextView);
            scheduleTextView = itemView.findViewById(R.id.moment_cardview_scheduleTextView);
            timeAgoTextView = itemView.findViewById(R.id.moment_cardview_timeAgoTextView);
            locationTextView = itemView.findViewById(R.id.moment_cardview_atPositionTextView);
            likeCountTextView = itemView.findViewById(R.id.moment_cardview_likeCountTextView);
            commentCountTextView = itemView.findViewById(R.id.moment_cardview_commentCountTextView);

            contentTextView = itemView.findViewById(R.id.moment_cardview_postContentTextView);
        }
    }
}
