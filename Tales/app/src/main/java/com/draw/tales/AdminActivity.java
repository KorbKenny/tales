package com.draw.tales;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.draw.tales.classes.Constants;
import com.draw.tales.main.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity {
    FirebaseDatabase db;
    int pageCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseDatabase.getInstance();
        final DatabaseReference firstStoryRef = db.getReference(Constants.GLOBAL).child(Constants.FIRST_STORY);
        firstStoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if(dataSnapshot!=null) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                String thisPageImage = ds.child(Constants.IMAGE_PATH).getValue(String.class);
                                if(thisPageImage!=null){
                                    if(!thisPageImage.equals(Constants.DB_NULL)){
                                        pageCount++;
                                    }
                                }
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        firstStoryRef.child(Constants.PAGE_COUNT).setValue(pageCount);
                        Toast.makeText(AdminActivity.this, "pages: " + pageCount, Toast.LENGTH_SHORT).show();
                    }
                }.execute();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AdminActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
