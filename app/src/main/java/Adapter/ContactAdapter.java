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

import wang.bogong.anuchat.InformationActivity;
import wang.bogong.anuchat.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import User.User;
import User.Gender;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.myViewHolder>
{
    ArrayList<User> list;
    Context context;

    public ContactAdapter(ArrayList<User> list, Context context)
    {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_contact_list, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position)
    {
        User user = list.get(position);
        holder.name.setText(user.getNickName());

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

        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context, InformationActivity.class);
                intent.putExtra("USER_FROM_CONTACT", user.getFirebaseID());
                context.startActivity(intent);
            }
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

        public myViewHolder(@NonNull View itemView)
        {
            super(itemView);
            avatar = itemView.findViewById(R.id.contact_list_avatar);
            name = itemView.findViewById(R.id.contact_list_nick_name);
        }
    }
}
