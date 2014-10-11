/**
 *
 */

/**
 * @author sandeep
 * @project Hackathon October 14
 * @team Joe said it ....
 */

package com.android.internal.app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.*;
import android.database.sqlite.*;
import android.provider.BaseColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.util.List;
import java.util.ArrayList;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.util.Comparator;
import java.util.PriorityQueue;
import android.content.*;

public class IntentLearner extends SQLiteOpenHelper
{

    private static abstract class Intent implements BaseColumns {
        public static final String TABLE_NAME = "IntentLearningTable";
        public static final String COLUMN_NAME_APPNAME = "appname";
        public static final String COLUMN_NAME_APPID = "appid";
        public static final String COLUMN_NAME_USE_COUNT = "usagecount";
        public static final String COLUMN_NAME_MIMETYPE = "mimetype";
        public static final String COLUMN_NAME_SOURCE_APP_NAME = "sourceapp";
        public static final String COLUMN_NAME_SOURCE_APP_ID = "sourceappid";
        public static final String COLUMN_NAME_START_TIME = "starttime";
    }

    private static final String DATABASE_NAME = "Intent2.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = " , ";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Intent.TABLE_NAME + " (" +
                    Intent._ID + " INTEGER PRIMARY KEY," +
                    Intent.COLUMN_NAME_APPNAME + TEXT_TYPE + COMMA_SEP +
                    Intent.COLUMN_NAME_APPID + TEXT_TYPE + COMMA_SEP +
                    Intent.COLUMN_NAME_USE_COUNT + TEXT_TYPE + COMMA_SEP +
                    Intent.COLUMN_NAME_MIMETYPE + TEXT_TYPE + COMMA_SEP +
                    Intent.COLUMN_NAME_SOURCE_APP_NAME + TEXT_TYPE + COMMA_SEP +
                    Intent.COLUMN_NAME_SOURCE_APP_ID + TEXT_TYPE + COMMA_SEP +
                    Intent.COLUMN_NAME_START_TIME + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Intent.TABLE_NAME;

  /*  private final Context context;

    public IntentLearner (Context ctx)
    {
        this.context = ctx;
    }
*/
    //private static class IntentLearnerInternal extends SQLiteOpenHelper {

        private static IntentLearner instance;
        private static Context mCtx;

        public static synchronized IntentLearner gethelper (Context context)
        {
            if (instance == null)
                instance = new IntentLearner(context);

            return instance;
        }
        /**/
        /*Constructor */
        /*public IntentLearner()
        {
            CreateDB();

        }*/
        public SQLiteDatabase db ;
        /*Constructor */
        public IntentLearner (Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mCtx = context;
            //onCreate(db);

        }
        @Override
        public void onCreate (SQLiteDatabase db)
        {
            db.execSQL(SQL_CREATE_ENTRIES);
            //super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onUpgrade (SQLiteDatabase db, int oldversion, int newversion)
        {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
        @Override
        public void onDowngrade (SQLiteDatabase db, int oldversion, int newversion)
        {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        /*List*/
        //public void GetListofApps(String mimetype, PriorityQueue<LearnedData> listofApps)
        public List<String> GetListofApps(String mimetype)
        {

            SQLiteDatabase db = this.getReadableDatabase();

            String[] projection = {
                    Intent._ID,
                    Intent.COLUMN_NAME_USE_COUNT,
                    Intent.COLUMN_NAME_APPNAME
            };

            //String selection = Intent.COLUMN_NAME_MIMETYPE + " LIKE ?";
            //Strings[] selectionArgs = {String.valueOf(rowId)};

            String sortOrder = Intent.COLUMN_NAME_USE_COUNT + " DESC";

            Cursor c = db.query(
                    Intent.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    sortOrder
            );

            //listofApps = new PriorityQueue<LearnedData>();
            List<String> list = new ArrayList<String>(50);
            c.moveToFirst();
            do {
                String appname = c.getString(c.getColumnIndex(Intent.COLUMN_NAME_APPNAME));
                int count = c.getInt(c.getColumnIndex(Intent.COLUMN_NAME_USE_COUNT));

                list.add(appname);

            }while(c.moveToNext());

            return list;
        }

        /*
        public class CountCompare extends Comparator <int>
        {
            @Override
            public int Compare (int a, int b)
            {
                if (a < b)
                    return -1;
                else if (a > b)
                    return 1;
                return 0;
            }

        }
        */

        public void UpdateAppUsage(String appname, String mimetype,String sourceapp )
        {
            SQLiteDatabase db;
            try
            {
                 db = this.getWritableDatabase();

                ContentValues cv = new ContentValues();

                String[] projections = {
                        Intent._ID,
                        Intent.COLUMN_NAME_USE_COUNT
                };

                String selection = Intent.COLUMN_NAME_MIMETYPE + " = '" + mimetype + "' AND " +
                        Intent.COLUMN_NAME_APPNAME + " = '" + appname +"'";

                String sortOrder = Intent.COLUMN_NAME_USE_COUNT + " DESC";

                Cursor c = db.query(
                        Intent.TABLE_NAME,
                        projections,
                        selection,
                        null,
                        null,
                        null,
                        sortOrder
                );

                int count = 0;
                if (c.getCount() != 0) {
                    int pos = c.getPosition();
                    c.moveToLast();
                    count = c.getInt(1);


                    count++;

                    cv.put(Intent.COLUMN_NAME_USE_COUNT, count);

                    int numofrows = db.update(Intent.TABLE_NAME,
                            cv,
                            selection,
                            null);

                    Log.i("HACKATHON","Number of Rows Updated "+numofrows);
                }
                else
                {
                    count++;

                    cv.put(Intent.COLUMN_NAME_USE_COUNT,count);
                    cv.put(Intent.COLUMN_NAME_APPNAME, appname);
                    cv.put(Intent.COLUMN_NAME_MIMETYPE,mimetype);

                    long rowid = db.insert(Intent.TABLE_NAME,
                                null,
                                cv);

                    Log.i("HACKATHON","Row Created "+rowid);
                }
            }
            catch (SQLiteException e)
            {
                Log.i("HACKATHON", "SQL exception in get writable database" + e.toString());
            }

        }

        //private SQLiteQueryBuilder queryHelper = new SQLiteQueryBuilder();

        //private SQLiteOpenHelper dbHelper = new SQLiteOpenHelper();

        //private SQLiteDatabase db = this.Open




        private void CreateDB ()
        {
            //db.execSQL(SQL_CREATE_ENTRIES);
    /*
            CREATE TABLE Learner (
                appname TEXT NOT NULL,
                appid  TEXT,
                usagecount INTEGER,
                mimetype TEXT,
                sourceapp TEXT,
                sourceappid TEXT,
                starttime INTEGER);
    */
        }





        /*private class databaseHelper extends SQLLiteOpenHelper
        {


        }

        protected class LearnedData implements Comparator<Integer>
        {
            int count;
            String appname;

            public LearnedData (int ncount, String name)
            {
                count = ncount;
                appname = name;
            }

            @Override
            public int compare (Integer a, Integer b)
            {
                return a.compareTo(b);
            }
        }

        */
    }

//}