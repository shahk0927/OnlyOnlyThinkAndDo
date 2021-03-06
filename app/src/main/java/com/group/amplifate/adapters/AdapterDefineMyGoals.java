package com.group.amplifate.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.group.amplifate.DefineStepActivity;
import com.group.amplifate.FinishedGoalActivity;
import com.group.amplifate.GoalDescriptionActivity;
import com.group.amplifate.R;
import com.group.amplifate.models.ModelGoalDescription;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterDefineMyGoals extends RecyclerView.Adapter<AdapterDefineMyGoals.MyHolder> {


    Context context;
    List<ModelGoalDescription> modelGoalDescriptionList;

    String myUid;
    Uri uri;

    private DatabaseReference Goal_Description;

    //Boolean mProcessLike = false;

    public AdapterDefineMyGoals(Context context, List<ModelGoalDescription> modelGoalDescriptionList) {
        this.context = context;
        this.modelGoalDescriptionList = modelGoalDescriptionList;

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Goal_Description = FirebaseDatabase.getInstance().getReference().child("Goal_Description");

    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_mygoal, parent, false);
        return new MyHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, int i) {

        //get data

        final String uId = modelGoalDescriptionList.get(i).getUid();
        final String uEmail = modelGoalDescriptionList.get(i).getuEmail();
        final String uName = modelGoalDescriptionList.get(i).getuName();
        String uDp = modelGoalDescriptionList.get(i).getuDp();
        final String gId = modelGoalDescriptionList.get(i).getgId();
        final String gTitle = modelGoalDescriptionList.get(i).getgTitle();
        final String gDescription = modelGoalDescriptionList.get(i).getgDescr();
        String gTimeStamp = modelGoalDescriptionList.get(i).getgTime();


        // convert timestamp to dd/mm/yyyy hh:mm: am/pm

        Calendar calender = Calendar.getInstance(Locale.getDefault());
        calender.setTimeInMillis(Long.parseLong(gTimeStamp));
        String pTime = DateFormat.format("dd MMM yyyy\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\thh:mm aa", calender).toString();

        // set data


        myHolder.gTimeTv.setText(pTime);
        myHolder.gTitleTv.setText(gTitle);
        myHolder.gDescriptionTv.setText(gDescription);

        // set post image
        // if there is no image i.e. pImage.equals("noImage" )then hide imageview
        // handle "more" button click
        myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showMoreOptions(myHolder.moreBtn, uId, myUid, gId, gTitle, gDescription, uName, uEmail);
                //Toast.makeText(context, "More", Toast.LENGTH_SHORT).show();
            }
        });

        myHolder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DefineStepActivity.class);
                intent.putExtra("uId", uId);
                intent.putExtra("gId",gId);
                intent.putExtra("gTitle", gTitle);
                intent.putExtra("gDescr", gDescription);
                intent.putExtra("uName", uName);
                intent.putExtra("uEmail", uEmail);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return modelGoalDescriptionList.size();
    }

    private void showMoreOptions(ImageButton moreBtn, final String uid, String myUid, final String gId, final String gTitle, final String gDescription, final String uName, final String uEmail) {

        // Creating popup menu currently having option Delete

        final PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        // show delete option in only post of currently signed in user.
        if (uid.equals(myUid)) {
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE, 2, 0, "Finish");
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }



        // item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    // delete is clicked
                    beginDelete(gId, gTitle);
                } else if (id == 1) {
                    // edit is clicked
                    // start AddPostActivity with key "editpost" and the id of the post clicked

                    Intent intent = new Intent(context, GoalDescriptionActivity.class);
                    intent.putExtra("key", "editGoal");
                    intent.putExtra("editGoalDescriptionId", gId);
                    context.startActivity(intent);
                } else if (id == 2) {

                    Intent intent = new Intent(context, FinishedGoalActivity.class);
                    intent.putExtra("finishGoalId", gId);
                    intent.putExtra("GoalTitle", gTitle);
                    intent.putExtra("GoalDescr", gDescription);
                    intent.putExtra("UserName", uName);
                    intent.putExtra("Useremail", uEmail);
                    intent.putExtra("Uid", uid);

                    context.startActivity(intent);
                    uploadData(gTitle, gDescription, gId, uid, uName, uEmail);
                    beginDelete(gId, gTitle);
                    //popupMenu.getMenu().removeItem(2);


                }

                return false;
            }
        });

        // show menu
        popupMenu.show();

    }

    private void uploadData(final String title, final String description, final String gid, final String uid, final String name, final String email) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Finishing...");
        pd.show();

        final String timeStamp = String.valueOf(System.currentTimeMillis());
        //String filePathAndName="Goal Description/" + "goalDes_" + timeStamp;

        //post without image
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("gId", gid);
        hashMap.put("gTitle", title);
        hashMap.put("gDescr", description);
        hashMap.put("gTime", timeStamp);


        //path to store post data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Goal_Finished");
        //
        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                pd.dismiss();
                Toast.makeText(context, "Goal Achieved", Toast.LENGTH_SHORT).show();

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void beginDelete(String gId, String gTitle) {

        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting... ");
        // image deleted, now delete database
        Query fQuery = FirebaseDatabase.getInstance().getReference("Goal_Description").orderByChild("gId").equalTo(gId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    ds.getRef().removeValue();  // remove values from firebase where pid matches
                }
                // deleted
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


// view holder class

    class MyHolder extends RecyclerView.ViewHolder {


        TextView gTimeTv, gTitleTv, gDescriptionTv;

        ImageButton moreBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);


            gTimeTv = itemView.findViewById(R.id.gTimeTv);
            gTitleTv = itemView.findViewById(R.id.goal_title_Tv);
            gDescriptionTv = itemView.findViewById(R.id.goal_description_Tv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            profileLayout = itemView.findViewById(R.id.goal_profileLayout);
        }
    }
}