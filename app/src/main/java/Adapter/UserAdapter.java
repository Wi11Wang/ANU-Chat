package Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import wang.bogong.anuchat.ChatDetailActivity;
import wang.bogong.anuchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import User.Gender;
import User.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.myViewHolder>
{
    ArrayList<User> list;
    Context context;

    public UserAdapter(ArrayList<User> list, Context context)
    {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_chat_list, parent, false);
        return new myViewHolder(view);
    }


    // If the avatarURL is empty, the avatar will be default male avatar when gender is male. For other
    // gender without an avatarURL, the avatar will be default female avatar.
    // For those who are offline, the avatar will be set according to its gender, which is similar to the above rules.
    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position)
    {
        User user = list.get(position);
        holder.name.setText(user.getNickName());

        // To set the last message
        FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(FirebaseAuth.getInstance().getUid() + user.getFirebaseID())
                .orderByChild("timestamp")
                .limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.hasChildren())
                        {
                            for(DataSnapshot d : snapshot.getChildren())
                            {
                                holder.description.setText(d.child("message").getValue().toString());

                                // Show the sending or receiving time of the last message.
                                Date date = new Date(d.child("timestamp").getValue(long.class));
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
                                String txt_date = simpleDateFormat.format(date);
                                holder.time.setText(txt_date);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }
                });

        if(!user.getAvatarURL().isEmpty())
        {
            if(user.gender == Gender.MALE)
            {
                Picasso.get().load(user.getAvatarURL()).placeholder(R.drawable.default_avatar_male).into(holder.avatar);
            }
            else
            {
                Picasso.get().load(user.getAvatarURL()).placeholder(R.drawable.default_avatar_female).into(holder.avatar);
            }
        }
        else
        {
            if(user.gender == Gender.MALE)
            {
                Picasso.get().load(R.drawable.default_avatar_male).into(holder.avatar);
            }
            else
            {
                Picasso.get().load(R.drawable.default_avatar_female).into(holder.avatar);
            }
        }

        holder.itemView.setOnClickListener(v ->
        {
            Intent intent = new Intent(context, ChatDetailActivity.class);
            intent.putExtra("USER_ID", user.getFirebaseID());
            intent.putExtra("AVATAR", user.getAvatarURL());
            intent.putExtra("NAME", user.getNickName());
            intent.putExtra("GENDER", user.getGender().toString());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    static class myViewHolder extends RecyclerView.ViewHolder
    {
        ImageView avatar;
        TextView name;
        TextView description;
        TextView time;

        public myViewHolder(@NonNull View itemView)
        {
            super(itemView);

            avatar = itemView.findViewById(R.id.contact_list_avatar);
            name = itemView.findViewById(R.id.contact_list_nick_name);
            description = itemView.findViewById(R.id.chat_list_description);
            time = itemView.findViewById(R.id.chat_list_time);
        }
    }
}
