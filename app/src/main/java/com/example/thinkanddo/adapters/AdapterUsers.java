package com.example.thinkanddo.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
<<<<<<< Updated upstream
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
=======
>>>>>>> Stashed changes
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.thinkanddo.ChatActivity;
import com.example.thinkanddo.R;
import com.example.thinkanddo.TheirProfileActivity;
import com.example.thinkanddo.models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    // For getting current user uId
    FirebaseAuth firebaseAuth;
    String myUid;

    Context context;
    List<ModelUsers> userList;


    public AdapterUsers(Context context, List<ModelUsers> userList) {
        this.context = context;
        this.userList = userList;

        firebaseAuth= FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        // inflate layout(row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, final int i) {

        final String hisUID = userList.get(i).getUid();

        // fetching data.
        final String userImage = userList.get(i).getImage();
        String userName = userList.get(i).getName();
        final String userEmail = userList.get(i).getEmail();

        // set data.
        myHolder.mnameTv.setText(userName);
        myHolder.mEmailTv.setText(userEmail);
        try{
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_default_img_foreground)
                    .into(myHolder.mAvatarIv);
        }
        catch (Exception e){


        }
        myHolder.blockIv.setImageResource(R.drawable.ic_unblocked_green);

        //check of each user if is blocked or not

        CheckIsBlocked(hisUID,myHolder,i);

        // handle item click

        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which ==0){
                            Intent intent = new Intent(context , TheirProfileActivity.class);
                            intent.putExtra("uId",hisUID);
                            context.startActivity(intent);
                        }
                        if(which == 1){

                              /*Click user to start chatting
                  Start activity by putting UID of receiver
                  we will use the uid to identify the user we are gonna chat
                 */

                              imBlockedORNot(hisUID);
                        }
                    }
                });
                builder.create().show();
            }

        });

        // Click to block unblock user
        myHolder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userList.get(i).isBlocked()){

                    unBlockUser(hisUID);
                }

                else{
                    blockUser(hisUID);
                }
            }
        });
    }

    private void imBlockedORNot(final String hisUID){
        /* first check if sender is blocked by reciver or not
            if uid of sender exists in "BlockedUsers" of receiver then sender is blocked
            if blocked then just display a message e.g you are blocked by user, can't send message
        * */

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            if(ds.exists()){
                                Toast.makeText(context, "You are blocked by user, can't send message",Toast.LENGTH_SHORT).show();
                                //backed, dont proceed further
                                return;
                            }
                        }
                        // not blocked , start activity
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid",hisUID);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
    private void CheckIsBlocked(String hisUID, final MyHolder myHolder, final int i) {
        // Check each user, if blocking or not..

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    if(ds.exists()){
                        myHolder.blockIv.setImageResource(R.drawable.ic_blocked_red);
                        userList.get(i).setBlocked(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void blockUser(String hisUID) {
        // Block the user, by adding uid to the current user's "BlockedUsers" node

        // put values in hashmap tp put in db

        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // blocked successfully

                Toast.makeText(context, "Blocked Successfully...",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // failed to block
                Toast.makeText(context, "Failed: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unBlockUser(String hisUID) {
        // unblock user
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            if(ds.exists()){

                                ds.getRef().removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // unblock succesfully
                                        Toast.makeText(context, "Unblocked Successfully...",Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Failed: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{


        ImageView mAvatarIv,blockIv;
        TextView mnameTv, mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            blockIv = itemView.findViewById(R.id.blockIv);
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            mnameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTv);
        }
    }

}
