package com.example.moviesbook.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviesbook.Activity.ChatActivity;
import com.example.moviesbook.Chat;
import com.example.moviesbook.Interfaces.ClickListener;
import com.example.moviesbook.R;
import com.example.moviesbook.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    Context context;
    List<User> mUsers;
    private final ClickListener listener;
    private String last_msg;

    public UserAdapter(Context context, List<User> mUsers, ClickListener listener){
        this.context = context;
        this.mUsers = mUsers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_chat, parent, false);

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.name.setText(user.getUsername());
        holder.id = user.getId();
        String s = mUsers.get(position).getImage();
        String s2 = "https://i.stack.imgur.com/l60Hf.png";

        if(s == null){
            Picasso.get().load(s2).into(holder.imageView);

        }
        else {
            Picasso.get().load(s).into(holder.imageView);
        }
        showLastMessage(user.getId(),holder.msg);
    }


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView name;
        ImageView imageView;
        TextView msg;
        String id;
        private WeakReference<ClickListener> listenerRef;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            listenerRef = new WeakReference<>(listener);
            name = (TextView)itemView.findViewById(R.id.chat_name);
            imageView = (ImageView)itemView.findViewById(R.id.chat_img);
            msg = (TextView)itemView.findViewById(R.id.chat_msg);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("ID",ViewHolder.this.id);
            context.startActivity(intent);
        }
    }
    private void showLastMessage(final String userid, final TextView msg){
        last_msg = "Default";
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Chats").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(QueryDocumentSnapshot document : task.getResult()){
                    Chat chat = new Chat();
                    chat.sender = document.get("sender").toString();
                    chat.receiver = document.get("receiver").toString();
                    chat.message = document.get("message").toString();
                    if(fUser.getUid().equals(chat.receiver) && chat.sender.equals(userid)||
                            fUser.getUid().equals(chat.sender)&& chat.receiver.equals(userid)){
                        last_msg = chat.message;
                    }
                }
                switch (last_msg){
                    case "Default":
                        msg.setText("No messages.");
                        break;
                    default:
                        msg.setText(last_msg);
                        break;
                }
                last_msg = "Default";
            }
        });

    }
}
