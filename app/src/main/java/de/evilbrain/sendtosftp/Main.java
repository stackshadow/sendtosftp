/* SendToSFTP
	Copyright (C) 2014 by Martin Langlotz

	This file is part of the SendToSFTP app.

	SendToSFTP is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, version 3 of the License

	SendToSFTP is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with SendToSFTP.  If not, see <http://www.gnu.org/licenses/>.
*/

package de.evilbrain.sendtosftp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Properties;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.json.JSONException;
import org.json.JSONObject;


public class Main extends Activity {


    ProgressDialog mProgressDialog;

// Configuration
    config          conf;

// Widgets
    TextView        fileToSend;
    ImageView       imagePreview;
    RadioGroup      serverList;
    Button          buttonAddServer;
    Button          buttonTestConnection;

// File stuff
    AssetManager    assetManager;
    File            fileDir;

// Notifications
    Intent          notificationIntent = null;


    @Override
    protected void          onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    // Get Widgets
        fileToSend=(TextView) findViewById(R.id.serverNameText);
        imagePreview=(ImageView) findViewById(R.id.imagePreview);
        serverList=(RadioGroup) findViewById(R.id.serverList);
        buttonAddServer=(Button) findViewById(R.id.buttonAddServer);
        buttonTestConnection=(Button) findViewById(R.id.buttonTestConnection);


    // Other global vars
        assetManager = getAssets();
        fileDir = getFilesDir();

    // Setup actions
        buttonAddServer.setOnClickListener(
        new View.OnClickListener() {
            public void onClick(View view) {
                openAddServerIntent();
            }
        });

        buttonTestConnection.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        new DownloadFilesTask().execute("host", "username", null, "keyfile");
                    }
                });


    // Create config and read it
        conf = new config( getFilesDir(), "config.json" );

        conf.createTest();
        conf.configSave();

        conf.configRead();
        serverListFill();

    // This Activity
        notificationInit();
        handlePushFromApp();






    }

// Activity Handling
    private void            handlePushFromApp(){
        // Push to this app
        Intent intent = getIntent();
        String type = intent.getType();

        if ( type != null ) {

            // Text file
            fileToSend.setText( intent.getClipData().getItemAt(0).getUri().toString() );

            // Preview Image
            if( type.contains("image")  ){
                imagePreview.setImageURI((Uri) intent.getExtras().get(Intent.EXTRA_STREAM));
            }
        }
    }
    private void            notificationInit() {
        notificationIntent = new Intent(this, Main.class);
    }
    private void            notificationSend( String Title, String Text ){
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // build notification
        // the addAction re-use the same intent to keep the example short
        Notification n = new Notification.Builder(this)
                .setContentTitle( Title )
                .setContentText( Text )
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true).build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, n);
    }

    public void             openAddServerIntent(){
        Intent intent = new Intent();
        intent.setClass( Main.this, addServer.class );
        intent.putExtra("action","add");
        startActivityForResult(intent, 1);
    }
    @Override
    protected void          onActivityResult(int requestCode, int resultCode, Intent data) {

        if( data != null ) {
            Bundle extras = data.getExtras();
            String intentAction = extras != null ? extras.getString("action") : "nothing";
            String intentServerName = extras != null ? extras.getString("serverName") : "";

        }


    }



    // Widget Handling
    public void             serverListFill(){

        JSONObject jsonServer = null;


        serverList.removeAllViews();

        try {

            int index = 0;

            jsonServer = conf.getServer( index );
            while( jsonServer != null ){
                RadioButton RadioButton1  = new RadioButton(this);
                RadioButton1.setText(jsonServer.getString("name"));
                RadioButton1.setId(index);
                serverList.addView(RadioButton1); //the RadioButtons are added to the radioGroup instead of the layout

                index++;
                jsonServer = conf.getServer( index );
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    protected void          onPreExecute() {
        // Create a progressdialog
        mProgressDialog = new ProgressDialog(this);
        // Set progressdialog title
        mProgressDialog.setTitle("Android JSON Parse Tutorial");
        // Set progressdialog message
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setIndeterminate(false);
        // Show progressdialog
        mProgressDialog.show();
    }




    private class           DownloadFilesTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground( String... strings ) {
            notificationSend( "Connection", "Connetion OK");


            return "";
        }

    }


}
