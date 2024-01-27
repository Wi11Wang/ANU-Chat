package Adapter;

import static java.util.Calendar.DAY_OF_YEAR;
import static Moment.MomentViewModel.setLinkClickActions;
import static Moment.MomentViewModel.setTimeAgoTextViewActions;
import static User.Gender.FEMALE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import wang.bogong.anuchat.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialTextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import Moment.Content.Comment;
import Moment.Content.Moment;
import Moment.MomentViewModel;
import User.User;

/**
 * @author Bogong Wang
 * @project Android Project
 * @created 07 / 10 / 2021 - 17:37
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context context;
    private String momentID;
    private List<Comment> comments;
    private FirebaseUser firebaseUser;

    public CommentAdapter(Context context, String momentID, List<Comment> comments) {
        this.context = context;
        this.momentID = momentID;
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_view, parent, false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // Set holder not recyclable to prevent display error.
        holder.setIsRecyclable(false);

        // Get comment.
        Comment comment = comments.get(position);

        // Display user name and avatar
        FirebaseDatabase.getInstance().getReference().child("User").child(comment.getSenderID()).addValueEventListener(new ValueEventListener() {
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

        // Display comment content.
        holder.commentTextView.setText(comment.getTextContent());

        if (comment.getSenderID().equals(firebaseUser.getUid())) {
            holder.moreImageView.setVisibility(View.VISIBLE);
            setMenuClickActions(comment.getCommentID(), momentID, holder.moreImageView);
            Calendar schedule = comment.getSchedule();
            // If has schedule:
            if (schedule != null) {
                // If schedule is not finished.
                if (!comment.isScheduleFinished()) {
                    holder.scheduleImageView.setVisibility(View.VISIBLE);
                    holder.scheduleTextView.setVisibility(View.VISIBLE);
                    holder.scheduleImageView.setImageResource(R.drawable.ic_time);
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                    String scheduledTime = dateFormat.format(schedule.getTime());
                    if (Calendar.getInstance().get(DAY_OF_YEAR) == schedule.get(DAY_OF_YEAR)) {
                        holder.scheduleTextView.setText("Today " + scheduledTime);
                    } else {
                        holder.scheduleTextView.setText("Tomorrow " + scheduledTime);
                    }
                }
            }
        }

        MomentViewModel.setHashTagClickActions(context, holder.commentTextView);

        try {
            setTimeAgoTextViewActions(holder.timeAgoTextView, comment);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        MomentViewModel.setAvatarClickActions(context, holder.avatarImageView, comment.getSenderID());
        setLinkClickActions(context, holder.commentTextView);
    }

    private void setMenuClickActions(String commentID, String momentID, ImageView itemImageButton) {
        itemImageButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.comment_more_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.moment_menu_delete) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle("Delete this comment?");
                    alertDialogBuilder.setMessage("This process cannot withdraw.");
                    alertDialogBuilder.setPositiveButton("Delete", (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference().child("Comments").child(momentID).child(commentID).removeValue();
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

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ShapeableImageView avatarImageView;
        public TextView userNameTextView;
        public SocialTextView commentTextView;
        public TextView timeAgoTextView;

        public ImageView scheduleImageView;
        public TextView scheduleTextView;

        public ImageView moreImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            linkUIComponents();
        }

        private void linkUIComponents() {
            avatarImageView = itemView.findViewById(R.id.comment_view_avatarShapeableImageView);
            userNameTextView = itemView.findViewById(R.id.comment_view_userNameTextView);
            commentTextView = itemView.findViewById(R.id.comment_view_commentTextView);
            timeAgoTextView = itemView.findViewById(R.id.comment_view_timeagoTextView);

            scheduleImageView = itemView.findViewById(R.id.comment_view_schedule_scheduleImageView);
            scheduleTextView = itemView.findViewById(R.id.comment_view_scheduleTextView);

            moreImageView = itemView.findViewById(R.id.comment_view_moreImageView);
        }
    }
}
