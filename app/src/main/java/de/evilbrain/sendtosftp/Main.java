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
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
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
    Switch          connectionState;
    Button          buttonAddServer;
    Button          bittonEditServer;


// File stuff
    AssetManager    assetManager;
    File            fileDir;

// Notifications
    Intent          notificationIntent = null;
    int             notificationID = 0;

// SSH Connection
    sshConnect      sshConnection;


    @Override
    protected void          onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    // Get Widgets
        fileToSend=(TextView) findViewById(R.id.serverNameText);
        imagePreview=(ImageView) findViewById(R.id.imagePreview);
        serverList=(RadioGroup) findViewById(R.id.serverList);
        connectionState=(Switch) findViewById(R.id.connectionState);
        buttonAddServer=(Button) findViewById(R.id.buttonAddServer);
        bittonEditServer=(Button) findViewById(R.id.bittonEditServer);


    // Other global vars
        assetManager = getAssets();
        fileDir = getFilesDir();

    // Init notification
        notificationInit();

    // Create config and read it
        conf = new config( getFilesDir(), "config.json" );

    // This is just a test
        //conf.createTest();
        //conf.configSave();

    // Read configuration
        if( ! conf.configRead() ) {
            conf.configSave();
            if( ! conf.configRead() ) {
                notificationSend( "Error", conf.errorMessage );
            }
        }

    // Fill the serverlist
        serverListFill();

    // This Activity
        handlePushFromApp();


    // Setup actions
        buttonAddServer.setOnClickListener( new View.OnClickListener() {
            public void onClick(View view) {
                openServerAddIntent();
            }
        });
        bittonEditServer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                openServerEditIntent();
            }
        });
        connectionState.setOnClickListener( new View.OnClickListener() {
            public void onClick(View view) {

                if( connectionState.isChecked() ){
                    connect();
                } else {
                    disconnect();
                }

            }
        });


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
        notificationID = 0;
    }
    private void            notificationSend( String Title, String Text ){
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder notificationbuilder = new NotificationCompat.Builder(this);
        Notification notification = notificationbuilder.setContentIntent(pIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker(Title)
                .setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(Title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(Text))
                .setContentText(Text).build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, notification);
        notificationID++;
    }

    public void             openServerAddIntent(){

        Intent intent = new Intent();
        intent.setClass( Main.this, addServer.class );

        startActivityForResult(intent, 1);
    }
    public void             openServerEditIntent(){

    // Get selected server
        String server = serverListGetActive();
        if( server != null ) {

        // Set Intent with json-string
            Intent intentServer = new Intent();
            intentServer.setClass(Main.this, addServer.class);
            conf.toIntent( intentServer, server );

        // Start activity
            startActivityForResult(intentServer, 1);

        }
    }
    private void            connect(){
        String server = serverListGetActive();
        if( server != null ) {
            processDialog("Test Connection", "Please wait while testing the SSH Connection");
            new connectTask().execute(  conf.getServerString( server ) );
        }
    }
    private class           connectTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            JSONObject jsonServer = null;

            // Build json-string
            try {
                jsonServer = new JSONObject( strings[0] );
            } catch (JSONException e) {
                e.printStackTrace();
                jsonServer = new JSONObject();
            }

            if( sshConnection == null ){
                sshConnection = new sshConnect();
            }


            sshConnection.setHost( config.getServerHostname(jsonServer) );
            sshConnection.setUsername( config.getServerUsername(jsonServer) );
            sshConnection.setPassword( config.getServerPassword(jsonServer) );
            sshConnection.connect();

            processDialogFinish();
            if( sshConnection.isConnected() ) {
                notificationSend("Connection Success", "Connetion to " + config.getServerHostname(jsonServer) + " success" );
            } else {
                notificationSend("Connection Failed", "Connection to " + config.getServerHostname(jsonServer) + " failed because: " + sshConnection.errorMessage );
            }

            return null;
        }


        protected void onPostExecute (String result){
            if( sshConnection.isConnected() ) {
                connectionState.setChecked(true);
            } else {
                connectionState.setChecked(false);
            }
        }


    }
    private void            disconnect(){
        sshConnection.disconnect();
        if( sshConnection.isConnected() ) {
            connectionState.setChecked(true);
        } else {
            connectionState.setChecked(false);
        }
    }

    @Override
    protected void          onActivityResult(int requestCode, int resultCode, Intent data) {

        if( data != null ) {

        // Get jsonServer from intent
            final Bundle extras = data.getExtras();
            if( extras != null ) {

                try {

                // Get and convert json from intent
                    String jsonString = extras.getString("json");
                    JSONObject jsonServer = new JSONObject(jsonString);

                // Add the server ( will be updated if already exist )
                    conf.addServer(jsonServer);

                // Save config file
                    conf.configSave();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        serverListFill();
    }



// Widget Handling
    public void             serverListFill(){

        JSONObject jsonServer = null;


        serverList.removeAllViews();
        int index = 0;

        jsonServer = conf.getServer( index );
        while( jsonServer != null ){
            RadioButton RadioButton1  = new RadioButton(this);
            RadioButton1.setText( config.getServerName(jsonServer) );
            RadioButton1.setId(index);
            serverList.addView(RadioButton1); //the RadioButtons are added to the radioGroup instead of the layout

            index++;
            jsonServer = conf.getServer( index );
        }

        serverList.clearCheck();
        serverList.bringToFront();




    }
    public String           serverListGetActive(){

    // Get selected Radio Button
        int selectedId = serverList.getCheckedRadioButtonId();

    // Get text from radio-button
        RadioButton actualradioButton = (RadioButton) findViewById(selectedId);
        if( actualradioButton == null ){
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setTitle("Select an Server");
            dlgAlert.setMessage("Please select an server from the list");
            dlgAlert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();

            return null;
        }

        return actualradioButton.getText().toString();
    }

// Addidional stuff
    protected void          processDialog( String Title, String Message ) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(Title);
        mProgressDialog.setMessage(Message);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.show();
    }
    protected void          processDialogFinish(){
        mProgressDialog.cancel();
    }


    @Override
    protected void          onDestroy(){

    // Disconnect from ssh
        sshConnection.disconnect();
    }

}
