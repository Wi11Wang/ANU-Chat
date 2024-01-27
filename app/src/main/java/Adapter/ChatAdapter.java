package Adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import wang.bogong.anuchat.Model.MessageModel;
import wang.bogong.anuchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatAdapter extends RecyclerView.Adapter
{
    ArrayList<MessageModel> messageModels;
    Context context;
    String receiverID;

    int OUTGOING_VIEW_TYPE = 1;  // Send the message. Sample_Sender
    int INCOMING_VIEW_TYPE = 2;  // Receive the message. Sample_Receiver

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context)
    {
        this.messageModels = messageModels;
        this.context = context;
    }

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context, String receiverID)
    {
        this.messageModels = messageModels;
        this.context = context;
        this.receiverID = receiverID;
    }

    @Override
    public int getItemViewType(int position)
    {
        if(messageModels.get(position).getUserID().equals(FirebaseAuth.getInstance().getUid()))
        {
            return OUTGOING_VIEW_TYPE;
        }
        else
        {
            return INCOMING_VIEW_TYPE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if(viewType == OUTGOING_VIEW_TYPE)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        MessageModel messageModel = messageModels.get(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                new AlertDialog.Builder(context).setTitle("Delete")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                               String senderRoom = FirebaseAuth.getInstance().getUid() + receiverID;
                                FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom)
                                        .child(messageModel.getMessageID()).setValue(null);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                }).show();
                return false;
            }
        });

        if(holder.getClass() == SenderViewHolder.class)
        {
            ((SenderViewHolder) holder).outgoingMsg.setText(messageModel.getMessage());

            // Set the format of the sending time
            Date date = new Date(messageModel.getTimestamp());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
            String txt_date = simpleDateFormat.format(date);

            TextView textView = ((SenderViewHolder) holder).outgoingTime;
            textView.setText(txt_date);
        }
        else
        {
            ((ReceiverViewHolder) holder).incomingMsg.setText(messageModel.getMessage());

            // Set the format of the sending time
            Date date = new Date(messageModel.getTimestamp());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
            String txt_date = simpleDateFormat.format(date);

            TextView textView = ((ReceiverViewHolder) holder).incomingTime;
            textView.setText(txt_date);
        }
    }

    @Override
    public int getItemCount()
    {
        return messageModels.size();
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder
    {
        TextView incomingMsg;
        TextView incomingTime;

        public ReceiverViewHolder(@NonNull View itemView)
        {
            super(itemView);
            incomingMsg = itemView.findViewById(R.id.incoming_msg);
            incomingTime = itemView.findViewById(R.id.incoming_time);
        }
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder
    {
        TextView outgoingMsg;
        TextView outgoingTime;

        public SenderViewHolder(@NonNull View itemView)
        {
            super(itemView);
            outgoingMsg = itemView.findViewById(R.id.outgoing_msg);
            outgoingTime = itemView.findViewById(R.id.outgoing_time);
        }
    }
}
