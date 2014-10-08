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
import android.widget.EditText;


public class addServer extends Activity {

    // Widgets
    EditText serverName;
    EditText userName;
    EditText keyFile;

    Button buttonOK;
    Button buttonCancel;

    String action;



    @Override
    protected void          onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_server);

        final Bundle extras = getIntent().getExtras();
        action = extras != null ? extras.getString("action") : "nothing";

    // Get widgets
        serverName=(EditText) findViewById(R.id.serverName);
        userName=(EditText) findViewById(R.id.userName);
        keyFile=(EditText) findViewById(R.id.keyFile);
        buttonOK=(Button) findViewById(R.id.buttonOK);
        buttonCancel=(Button) findViewById(R.id.buttonCancel);



    // Actions
        buttonOK.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent returnData = collectData();
                        setResult(RESULT_OK,returnData);
                        finish();
                    }
                });


        ;
    }

    private Intent          collectData(){
        Intent newIntent = new Intent();
        newIntent.putExtra( "action", action );
        newIntent.putExtra( "serverName", serverName.getText().toString() );
        newIntent.putExtra( "userName", userName.getText().toString() );
        newIntent.putExtra( "keyFile", keyFile.getText().toString() );

        return newIntent;
    }



}
