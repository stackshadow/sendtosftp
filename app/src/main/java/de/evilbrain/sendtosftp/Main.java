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

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class Main extends Activity {


    private ProgressDialog mProgressDialog;

// Configuration
    private config          conf;

// Widgets
    private RadioGroup      serverList;
    private TextView        fileNameText;
    private TextView        fileName;
    private ImageView       imagePreview;
    private Button          buttonSend;
    private Switch          connectionState;
    private Button          buttonAddServer;
    private Button          buttonEditServer;
    private Button          buttonDeleteServer;


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
        serverList=(RadioGroup) findViewById(R.id.serverList);
        fileNameText=(TextView) findViewById(R.id.fileNameText);
        fileName=(TextView) findViewById(R.id.fileName);
        imagePreview=(ImageView) findViewById(R.id.imagePreview);
        buttonSend=(Button) findViewById(R.id.buttonSend);
        connectionState=(Switch) findViewById(R.id.connectionState);
        buttonAddServer=(Button) findViewById(R.id.buttonAddServer);
        buttonEditServer=(Button) findViewById(R.id.buttonEditServer);
        buttonDeleteServer=(Button) findViewById(R.id.buttonDeleteServer);


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
        conf.configRead();

    // Fill the serverlist
        serverListFill();

    // This Activity
        handlePushFromApp();


    // Setup actions
        buttonAddServer.setOnClickListener( new View.OnClickListener() {
            public void onClick(View view) {
                serverAdd();
            }
        });
        buttonEditServer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                serverEdit();
            }
        });
        buttonDeleteServer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                serverDelete();
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
    @SuppressLint("NewApi")
    private void            handlePushFromApp(){
        // Push to this app
        Intent intent = getIntent();
        String type = intent.getType();

        if ( type != null ) {

        // Set all visible
            fileNameText.setVisibility(View.VISIBLE );
            fileName.setVisibility(View.VISIBLE);
            imagePreview.setVisibility(View.VISIBLE);
            buttonSend.setVisibility(View.VISIBLE);

        // get Content
            String contentString = "";
            Object content = null;

            if( android.os.Build.VERSION.SDK_INT > 16 ){
                content = intent.getClipData().getItemAt(0).getUri();
                contentString = content.toString();
            } else {
                content = intent.getExtras().get(Intent.EXTRA_STREAM);
                contentString = content.toString();
            }

        // set filename
            fileName.setText( contentString );

        // Preview Image
            if( type.contains("image")  ){
                imagePreview.setImageURI((Uri) content );
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

    public void             serverAdd(){

        Intent intent = new Intent();
        intent.setClass( Main.this, addServer.class );
        conf.putSettingsToIntent( intent );

        startActivityForResult(intent, 1);
    }
    public void             serverEdit(){

    // Get selected server
        String server = serverListGetActive();
        if( server != null ) {

        // Set Intent with json-string
            Intent intent = new Intent();
            intent.setClass(Main.this, addServer.class);
            conf.putServerToIntent( intent, server );
            conf.putSettingsToIntent( intent );

        // Start activity
            startActivityForResult(intent, 1);

        }
    }
    public void             serverDelete(){
        // Get selected server
        String server = serverListGetActive();
        if( server != null ) {
            conf.deleteServer(server);
        }

        serverListFill();
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

            // Get and convert json from intent
                JSONObject jsonServer = config.getServerFromIntent(data);
                JSONObject jsonSettings = config.getSettingsFromIntent( data );

            // Add the server ( will be updated if already exist )
                conf.addServer( jsonServer );
                conf.setSettings( jsonSettings );

        }

        serverListFill();
    }



// Widget Handling
    public void             serverListFill(){

    // Vars
        JSONObject jsonServer = null;
        int index = 0;
        String actualServer = null;

    // clean list
        serverList.removeAllViews();

    // get the default server and select it
        String defaultServer = conf.getDefaultServer();

    // iterate through the server
        jsonServer = conf.getServer( index );
        while( jsonServer != null ){

            actualServer = config.getServerName(jsonServer);

        // Create radio button
            RadioButton RadioButton1  = new RadioButton(this);
            RadioButton1.setText( actualServer );
            RadioButton1.setId(index);
        // This is the default server
            if( defaultServer.equals(actualServer) ){
                RadioButton1.setChecked( true );
            }

        // Add it
            serverList.addView(RadioButton1); //the RadioButtons are added to the radioGroup instead of the layout

            index++;
            jsonServer = conf.getServer( index );
        }

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
        if( sshConnection != null ) {
            sshConnection.disconnect();
        }

        super.onDestroy();
    }

}
