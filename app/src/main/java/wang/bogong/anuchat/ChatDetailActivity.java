package wang.bogong.anuchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import wang.bogong.anuchat.Model.MessageModel;

import wang.bogong.anuchat.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import Adapter.ChatAdapter;

public class ChatDetailActivity extends AppCompatActivity
{
    ImageView chatBack, avatar, chatMenu;
    TextView username;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));
        setContentView(R.layout.activity_chat_detail);

        Intent intent = getIntent();
        String senderID = mAuth.getUid();  // People who's using this phone
        String receiverID = intent.getStringExtra("USER_ID");  // People who's talking to the owner of this phone
        String userName = intent.getStringExtra("NAME");
        String profilePic = intent.getStringExtra("AVATAR");
        String gender = intent.getStringExtra("GENDER");

        username = findViewById(R.id.chat_username);
        username.setText(userName);
        avatar = findViewById(R.id.chat_avatar);
        if(!profilePic.isEmpty())
        {
            if(gender.equals("MALE"))
            {
                Picasso.get().load(profilePic).placeholder(R.drawable.default_avatar_male).into(avatar);
            }
            else
            {
                Picasso.get().load(profilePic).placeholder(R.drawable.default_avatar_female).into(avatar);
            }
        }
        else
        {
            if(gender.equals("MALE"))
            {
                Picasso.get().load(R.drawable.default_avatar_male).into(avatar);
            }
            else
            {
                Picasso.get().load(R.drawable.default_avatar_female).into(avatar);
            }
        }

        // Set the recycler view
        ArrayList<MessageModel> messageModels = new ArrayList<>();
        ChatAdapter chatAdapter = new ChatAdapter(messageModels, this, receiverID);
        RecyclerView recyclerView = findViewById(R.id.dialog_view);
        recyclerView.setAdapter(chatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        String senderRoom = senderID + receiverID;
        String receiverRoom = receiverID + senderID;
        ImageView senderIcon = findViewById(R.id.send_icon);
        EditText inputMsg = findViewById(R.id.input_message);
        senderIcon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(inputMsg.getText().toString().isEmpty())
                {
                    Toast.makeText(ChatDetailActivity.this, "Empty message!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    MessageModel msgModel = new MessageModel(senderID, inputMsg.getText().toString());
                    msgModel.setTimestamp(new Date().getTime());
                    inputMsg.setText("");  // After click on the send button, set the input field empty.

                    firebaseDatabase.getReference().child("Chats").child(senderRoom) // Store message data in the branch of a sender.
                            .push().setValue(msgModel).addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(@NonNull Void unused)
                        {
                            firebaseDatabase.getReference().child("Chats").child(receiverRoom)
                                    .push().setValue(msgModel).addOnSuccessListener(new OnSuccessListener<Void>()
                            {
                                @Override
                                public void onSuccess(@NonNull Void unused)
                                {

                                }
                            });
                        }
                    });
                }
            }
        });

        // Show the message content stored in the realtime database
        firebaseDatabase.getReference().child("Chats").child(senderRoom).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                messageModels.clear();
                for(DataSnapshot d : snapshot.getChildren())
                {
                    MessageModel model = d.getValue(MessageModel.class);
                    model.setMessageID(d.getKey());   // d.getKey() will return the push Hash value.
                    messageModels.add(model);
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

        chatBack = findViewById(R.id.chat_back);
        chatBack.setOnClickListener(v -> finish());
    }

}