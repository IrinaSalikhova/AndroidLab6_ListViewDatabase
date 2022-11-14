package com.cst3104.androidlab5_listview;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase theDatabase;
    private  Cursor results;

    private int idIndex;
    private int  messageIndex;
    private int sOrRIndex;
    private int timeIndex;

    private EditText newMessage;
    private MyAdapter myAdapter;
    private ArrayList<ChatMessage> messages;
    static Context mycontext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mycontext = this;



        messages = new ArrayList<>();

        ListView messageView = findViewById(R.id.messageView);
        myAdapter = new MyAdapter(this, messages);
        messageView.setAdapter(myAdapter);

        MyOpenHelper myOpener = new MyOpenHelper(mycontext);
        theDatabase = myOpener.getWritableDatabase();
        loadFromDB(theDatabase, null);


        printCursor(theDatabase);

        newMessage = findViewById(R.id.messageField);


        ImageButton receiveButton = findViewById(R.id.receiveButton);

        receiveButton.setOnClickListener(click ->
        {
            String readMessage = newMessage.getText().toString();
            if (!readMessage.isEmpty()) {
                int id = addToDB(readMessage, true);
                ChatMessage thisMessage = new ChatMessage(readMessage, true, id);
                messages.add(thisMessage);
                myAdapter.notifyDataSetChanged();
                newMessage.setText("");
                Log.e("CheckingIT", thisMessage.toString());

            }
        });

        ImageButton sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(click ->
        {
            String readMessage = newMessage.getText().toString();
            if (!readMessage.isEmpty()) {
                int id = addToDB(readMessage, false);
                ChatMessage thisMessage = new ChatMessage(readMessage, false, id);
                messages.add(thisMessage);
                myAdapter.notifyDataSetChanged();
                newMessage.setText("");
                Log.e("CheckingIT", thisMessage.toString());

            }
        });

        messageView.setOnItemLongClickListener((parent, view, position, id) -> {

            ChatMessage whatWasClicked = messages.get(position);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mycontext);
            alertDialogBuilder.setTitle(getString(R.string.alert_question))

                    //What is the message:
                    .setMessage(getString(R.string.alert_inf1) + (position + 1) + "\n" + getString(R.string.alert_inf2) + whatWasClicked.getId())

                    //what the Yes button does:
                    .setPositiveButton(R.string.positive_alert, (click, arg) -> {
                        messages.remove(position);
                        myAdapter.notifyDataSetChanged();
                        //delete from database:, returns number of rows deleted
                        theDatabase.delete(MyOpenHelper.TABLE_NAME,
                                MyOpenHelper.COL_ID +" = ?", new String[] { Long.toString( whatWasClicked.getId() )  });
                    })
                    //What the No button does:
                    .setNegativeButton(R.string.negative_alert, (click, arg) -> {
                    })

                    //Show the dialog
                    .create().show();
            return true;
        });
    }

    private void printCursor(SQLiteDatabase theDatabase) {
            openDB(theDatabase, null);
       int version = theDatabase.getVersion();
       int columns = results.getColumnCount();
       int rows = results.getCount();

       String[] columnNames = results.getColumnNames();

       ArrayList<String> rowStrings = new ArrayList<>();

        while( results.moveToNext() ) {
            int id = results.getInt(idIndex);
            int statusInt = results.getInt(sOrRIndex);
            String message = results.getString(messageIndex);
            String time = results.getString(timeIndex);
            boolean status = false;
            String stringStatus = "sent";
            if (statusInt == 1) {
                status = true;
                stringStatus = "received";
            }
            String newRow = String.format("id: %d, Message: %s, SendOrReceive: %d (means boolean %s and \"%s\" status), TimeSent: %s (it wasn't saved at all on purpose)",
                    id, message, statusInt, status, stringStatus, time);
            rowStrings.add(newRow);
        }

       String firstToLog = "Version: " + version + " \n " +
               "Columns: " + columns + "\n" +
               "Rows: "+ rows;
        String secondToLog = TextUtils.join(System.getProperty("line.separator"),columnNames);
        String thirdToLog = TextUtils.join(System.getProperty("line.separator"),rowStrings);

        String TAG = "printCursor";
        Log.d(TAG, firstToLog);
        Log.d(TAG, "Column names: " + "\n" + secondToLog );
        Log.d(TAG, "Rows content: " + "\n" + thirdToLog );

        results.close();
    }

    private int addToDB(String readMessage, boolean b) {

        ContentValues newRow = new ContentValues();// like intent or Bundle

        //Message column:
        newRow.put( MyOpenHelper.COL_MESSAGE , readMessage  );

        //Send or receive column:
        newRow.put(MyOpenHelper.COL_SEND_RECEIVE, b);

        return (int) theDatabase.insert( MyOpenHelper.TABLE_NAME, null, newRow );
    }

    private void loadFromDB(SQLiteDatabase theDatabase, @Nullable String text) {
        openDB(theDatabase, text);

        //cursor is pointing to row -1
        while( results.moveToNext() ) //returns false if no more data
        { //pointing to row 2
            int id = results.getInt(idIndex);
            int statusInt = results.getInt(sOrRIndex);
            String message = results.getString( messageIndex );
            boolean status = statusInt == 1;

            //add to arrayList:
            messages.add( new ChatMessage(message,status, id));
        }

        results.close();
        myAdapter.notifyDataSetChanged();

    }

    private void openDB(SQLiteDatabase theDatabase, @Nullable String text) {
if (text == null) {
    //load from the database:
    results = theDatabase.rawQuery("Select * from " + MyOpenHelper.TABLE_NAME + ";", null);//no arguments to the query
}
else {
    messages.clear();
    results = theDatabase.rawQuery( "Select * from " + MyOpenHelper.TABLE_NAME + " WHERE " + MyOpenHelper.COL_MESSAGE + " like '%" + text + "%';", null );

}
        //Convert column names to indices:
        idIndex = results.getColumnIndex( MyOpenHelper.COL_ID );
        messageIndex = results.getColumnIndex( MyOpenHelper.COL_MESSAGE);
        sOrRIndex = results.getColumnIndex( MyOpenHelper.COL_SEND_RECEIVE);
        timeIndex = results.getColumnIndex( MyOpenHelper.COL_TIME_SENT );

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem search = menu.findItem(R.id.app_bar_search);
        SearchView searchView =  (SearchView) search.getActionView();

        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadFromDB( theDatabase, query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadFromDB( theDatabase, newText);
                return false;
            }
        } );

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

                printCursor(theDatabase);

            return super.onOptionsItemSelected(item);
        }

}



