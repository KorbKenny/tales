package com.draw.tales;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.draw.tales.classes.Constants;

public class MultiverseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiverse);

        //Get all pages as a list
        TextView v = (TextView)findViewById(R.id.drawtalestext);
        Typeface typeface = Typeface.createFromAsset(getAssets(), Constants.FONT);
        v.setTypeface(typeface);
        TextView t = (TextView)findViewById(R.id.titletitle);
        t.setTypeface(typeface);

        //Start on page one

        //Get LEFT and RIGHT page id

        //Get the pages that correspond to the LEFT and RIGHT ids of current page

        //Each of these pages is now page 2

        //Recurse through this code to get pages 3, 4, 5... n

        //Now that you have the max pages, you can set the distance between branches

        //Decrease the angle between brances reverse-exponentially

        //Go back to the first page

        //Now run through everything again and draw a line between every node that continues

        //Draw a dot at every page.

        //Match the id's of the users for each page to your id to choose
    }
}
