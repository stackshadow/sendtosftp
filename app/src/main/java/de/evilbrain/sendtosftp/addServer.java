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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;


public class addServer extends Activity {

    // Widgets
    EditText serverName;
    EditText serverHost;
    EditText userName;
    EditText userPassword;
    EditText keyFile;
    CheckBox defaultServer;

    Button buttonOK;
    Button buttonCancel;

    String action;

// Server
    JSONObject jsonServer = null;
    JSONObject jsonSettings = null;

    @Override
    protected void          onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_server);



    // Get widgets
        serverName=(EditText) findViewById(R.id.serverName);
        serverHost=(EditText) findViewById(R.id.serverHost);
        userName=(EditText) findViewById(R.id.userName);
        userPassword=(EditText) findViewById(R.id.userPassword);
        keyFile=(EditText) findViewById(R.id.keyFile);
        defaultServer=(CheckBox)findViewById(R.id.defaultServer);
        buttonOK=(Button) findViewById(R.id.buttonOK);
        buttonCancel=(Button) findViewById(R.id.buttonCancel);

    // Get jsons from intent
        jsonServer = config.getServerFromIntent(getIntent());
        jsonSettings = config.getSettingsFromIntent( getIntent() );

    // Set present values
        String serverNameString = config.getServerName(jsonServer);
        serverName.setText( serverNameString );
        serverHost.setText(config.getServerHostname(jsonServer));
        userName.setText( config.getServerUsername(jsonServer) );
        keyFile.setText( config.getServerKeyfile(jsonServer) );

    // Compare if this is the default server
        String defaultServerName = config.getDefaultServer(jsonSettings);
        if( defaultServerName.equals(serverNameString) ){
            defaultServer.setChecked( true );
        } else {
            defaultServer.setChecked( false );
        }


    // Actions
        buttonOK.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {

                    // read data to jsons
                        collectData();

                    // Create and fill intent
                        Intent returnData = new Intent();
                        config.putServerToIntent( returnData, jsonServer );
                        config.putSettingsToIntent( returnData, jsonSettings );

                    // return
                        setResult(RESULT_OK,returnData);
                        finish();
                    }
                });

        buttonCancel.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        setResult(RESULT_OK,null);
                        finish();
                    }
                });

    }

    private void              collectData(){
        config.setServer( jsonServer,
                serverName.getText().toString(),
                serverHost.getText().toString(),
                userName.getText().toString(),
                userPassword.getText().toString(),
                keyFile.getText().toString() );

        if( defaultServer.isChecked() ){
            config.setDefaultServer( jsonSettings, serverName.getText().toString() );
        }

        return;
    }



}
