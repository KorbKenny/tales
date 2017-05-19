package com.draw.tales.groups;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.draw.tales.classes.Bookmark;
import com.draw.tales.classes.GroupLite;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.CheckedOutputStream;

/**
 * Created by KorbBookProReturns on 3/18/17.
 */

public class DBSQLiteHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "drawtales.db";

    //==========================================
    //               Groups
    //==========================================
    public static final String TABLE_GROUPS = "GROUPS";

    public static final String G_COL_ID = "ID";
    public static final String G_COL_NAME = "NAME";
    public static final String G_COL_TYPE = "TYPE";
    public static final String G_COL_MEMBERS = "MEMBERS";
    public static final String G_COL_UPDATED = "UPDATED";

    public static final String[] ALL_COLUMNS_GROUPS = {G_COL_ID,G_COL_NAME,G_COL_TYPE,G_COL_MEMBERS,G_COL_UPDATED};

    private static final String CREATE_TABLE_GROUPS =
            "CREATE TABLE " + TABLE_GROUPS + "(" +
                    G_COL_ID + " TEXT PRIMARY KEY, " +
                    G_COL_NAME + " TEXT, " +
                    G_COL_TYPE + " INTEGER, " +
                    G_COL_MEMBERS + " TEXT, " +
                    G_COL_UPDATED + " INTEGER)";


    //==========================================
    //               Bookmarks
    //==========================================
    public static final String TABLE_BOOKMARKS = "BOOKMARKS";

    public static final String B_COL_NUMBER = "NUMBER";
    public static final String B_COL_ID = "PAGEID";
    public static final String B_COL_IMAGE = "PAGEIMAGE";

    public static final String[] ALL_COLUMNS_BOOKMARKS = {B_COL_NUMBER,B_COL_ID,B_COL_IMAGE};

    private static final String CREATE_TABLE_BOOKMARKS =
            "CREATE TABLE " + TABLE_BOOKMARKS + "(" +
                    B_COL_NUMBER + " INTEGER PRIMARY KEY, " +
                    B_COL_ID + " TEXT, " +
                    B_COL_IMAGE + " TEXT)";


    //==========================================
    //            Constructor/Singleton
    //==========================================
    private static DBSQLiteHelper mInstance;

    public static DBSQLiteHelper getInstance(Context context){
        if(mInstance==null){
            mInstance = new DBSQLiteHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    private DBSQLiteHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_GROUPS);
        db.execSQL(CREATE_TABLE_BOOKMARKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
        this.onCreate(db);
    }

    //==============================================================
    //==============================================================
    //            GROUP METHODS
    //==============================================================
    //==============================================================


    //==========================================
    //            Get All Groups
    //==========================================
    public List<GroupLite> getAllGroups(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_GROUPS,
                ALL_COLUMNS_GROUPS,null,null,null,null,null);

        List<GroupLite> gList = new ArrayList<>();

        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String id = cursor.getString(cursor.getColumnIndex(G_COL_ID));
                String name = cursor.getString(cursor.getColumnIndex(G_COL_NAME));
                int type = cursor.getInt(cursor.getColumnIndex(G_COL_TYPE));
                String members = cursor.getString(cursor.getColumnIndex(G_COL_MEMBERS));
                int updated = cursor.getInt(cursor.getColumnIndex(G_COL_UPDATED));

                GroupLite g = new GroupLite(id,name,type,members,updated);
                gList.add(g);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return gList;
    }

    //==========================================
    //            Update Group
    //==========================================
    public void updateGroup(GroupLite g){
        SQLiteDatabase db = getWritableDatabase();
        String selection = G_COL_ID + " = ?";
        String[] selectionArgs = new String[]{g.getId()};
        ContentValues values = new ContentValues();
        values.put(G_COL_NAME,g.getName());
        values.put(G_COL_MEMBERS,g.getMembers());
        values.put(G_COL_UPDATED,g.getUpdated());
        db.update(TABLE_GROUPS,values,selection,selectionArgs);
        db.close();
    }

    //==========================================
    //            Update Group Name Only
    //==========================================
    public void updateGroupName(String s,String newName){
        SQLiteDatabase db = getWritableDatabase();
        String selection = G_COL_ID + " = ?";
        String[] selectionArgs = new String[]{s};
        ContentValues values = new ContentValues();
        values.put(G_COL_NAME,newName);
        db.update(TABLE_GROUPS,values,selection,selectionArgs);
        db.close();
    }


    //==========================================
    //            Add New Group
    //==========================================
    public void addNewGroup(GroupLite g){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(G_COL_ID,g.getId());
        values.put(G_COL_NAME,g.getName());
        values.put(G_COL_TYPE,g.getType());
        values.put(G_COL_MEMBERS,g.getMembers());
        values.put(G_COL_UPDATED,g.getUpdated());
        db.insert(TABLE_GROUPS,null,values);
        db.close();
    }

    //==========================================
    //            Delete Group
    //==========================================
    public void deleteGroup(GroupLite g){
        SQLiteDatabase db = getWritableDatabase();
        String selection = G_COL_ID + " = ?";
        String[] selectionArgs = {g.getId()};
        db.delete(TABLE_GROUPS,selection,selectionArgs);
        db.close();
    }

    //==========================================
    //            Clear All Groups
    //==========================================
    public void clearAllGroups(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_GROUPS,null,null);
        db.close();
    }

    //==============================================================
    //==============================================================
    //            BOOKMARK METHODS
    //==============================================================
    //==============================================================

    //==========================================
    //            Get all Bookmarks
    //==========================================
    public List<Bookmark> getAllBookmarks(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKMARKS,
                ALL_COLUMNS_BOOKMARKS,null,null,null,null,null);

        List<Bookmark> bList = new ArrayList<>();

        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                Integer number = cursor.getInt(cursor.getColumnIndex(B_COL_NUMBER));
                String id = cursor.getString(cursor.getColumnIndex(B_COL_ID));
                String image = cursor.getString(cursor.getColumnIndex(B_COL_IMAGE));

                Bookmark b = new Bookmark(number,id,image);
                bList.add(b);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return bList;
    }


    //==========================================
    //            Set a Bookmark
    //==========================================
    public void setBookmark(int number, String id, String image){
        SQLiteDatabase db = getWritableDatabase();
        String selection = B_COL_NUMBER + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(number)};
        ContentValues values = new ContentValues();
        values.put(B_COL_ID,id);
        values.put(B_COL_IMAGE,image);
        db.update(TABLE_BOOKMARKS,values,selection,selectionArgs);
        db.close();
    }

    //==========================================
    //            Clear All Bookmarks
    //==========================================
    public void clearAllBookmarks(){
        SQLiteDatabase db = getWritableDatabase();
        for (int i = 1; i < 5; i++) {
            String selection = B_COL_NUMBER + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(i)};
            ContentValues values = new ContentValues();
            values.put(B_COL_ID,"");
            values.put(B_COL_IMAGE,"");
            db.update(TABLE_BOOKMARKS,values,selection,selectionArgs);
        }
        db.close();
    }
}

