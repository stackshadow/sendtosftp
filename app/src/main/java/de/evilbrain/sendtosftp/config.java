package de.evilbrain.sendtosftp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by stackshadow on 07.10.14.
 */
public class config {

    // Core
    public String           errorMessage;
    private File            fileDir;

    // Configuration
    private String          jsonConfigFile = null;
    private JSONObject      jsonConfig = null;
    private JSONArray       jsonServers = null;
    private JSONObject      jsonSettings = null;



                                config( File baseDirectory, String configFile ) {
        fileDir = baseDirectory;
        jsonConfigFile = configFile;

        createEmpty();
    }



    public void                 createTest(){
        createEmpty();
        addServer("localhost", "localhost", "testuser", "password", "/keyfile");
        addServer("testserver", "github.com", "testuser", "password", "/keyfile");
    }

    private boolean             createEmpty(){

        try {
            jsonConfig = new JSONObject();
            jsonServers = new JSONArray();
            jsonSettings = new JSONObject();

            jsonConfig.put("servers", jsonServers);
            jsonConfig.put("settings", jsonSettings);
        } catch (JSONException e) {
            e.printStackTrace();
            errorMessage = e.getLocalizedMessage();
            return false;
        }


        return true;
    }

// ############################### save / read ###############################

    public boolean              configRead(){

        StringBuilder total = new StringBuilder();

    // To load text file
        try {
            File configFile = new File( fileDir + "/", jsonConfigFile );
            InputStream configFileStream = new BufferedInputStream(new FileInputStream(configFile));
            BufferedReader fileBufferedReader = new BufferedReader(new InputStreamReader(configFileStream));

            String line;
            while ((line = fileBufferedReader.readLine()) != null) {
                total.append(line);
            }
            fileBufferedReader.close();
            configFileStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getLocalizedMessage();
            return false;
        }

    // Create new JSON-Object
        try {

        // if jsonConfig is present, destroy it
            if( jsonConfig != null ){
                jsonConfig = null;
            }

        // Create new JSON Object
            jsonConfig = new JSONObject( total.toString() );
            jsonServers = jsonConfig.getJSONArray("servers");
            jsonSettings = jsonConfig.getJSONObject("settings");

        } catch (JSONException e) {
            e.printStackTrace();
            errorMessage = e.getLocalizedMessage();
            return false;
        }


        return true;
    }

    private boolean             configSave() {

        String jsonString = jsonConfig.toString();

        try {
            File configFile = new File( fileDir + "/", jsonConfigFile );
            configFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(configFile);

            fos.write(jsonString.getBytes());
            fos.flush();
            fos.close();



        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getLocalizedMessage();
            return false;
        }

    /*
        File f = new File( fileDir + "/" );
        File file[] = f.listFiles();
        Log.d("Files", "Size: "+ file.length);
        for (int i=0; i < file.length; i++)
        {
            Log.d("Files", "FileName:" + file[i].getName());
        }
    */
        return true;

    }



// ############################### Get ###############################
    public JSONObject           getServer( int index ){
        // Basic check
        if( jsonServers == null ) return null;
        if( index < 0 ) return null;

        // Vars
        JSONObject jsonServer = null;

        // Get Server
        try {
            if (index < jsonServers.length()) {
                jsonServer = jsonServers.getJSONObject(index);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // Return
        return jsonServer;
    }
    private int                 getServerIndex( String name ){

        // Values
        int serverIndex = 0;
        JSONObject jsonServer = null;

        jsonServer = getServer(serverIndex);
        while( jsonServer != null ){

            try {
                if( serverIndex < jsonServers.length() ) {

                    jsonServer = jsonServers.getJSONObject(serverIndex);
                    String ServerName = this.getServerName(jsonServer);
                    if( ServerName.equals(name) ){
                        return serverIndex;
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            serverIndex++;
            jsonServer = getServer(serverIndex);
        }

        return -1;
    }
    private JSONObject          getServer( String name ){
        return getServer( getServerIndex(name) );
    }

    public String               getServerString( String servername ){

        // Try to find a server
        JSONObject jsonServer = getServer( servername );

        if( jsonServer == null ){
            jsonServer = new JSONObject();
            setServer( jsonServer, "", "", "", "", "" );
        }

        return jsonServer.toString();
    }
    public static String        getServerName( JSONObject jsonServer ){
        try {
            return jsonServer.getString("serverName");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }
    public static String        getServerHostname( JSONObject jsonServer ){
        try {
            return jsonServer.getString("serverHostname");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }
    public static String        getServerUsername( JSONObject jsonServer ){
        try {
            return jsonServer.getString("serverUsername");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }
    public static String        getServerPassword( JSONObject jsonServer ){
        try {
            return jsonServer.getString("serverPassword");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }
    public static String        getServerKeyfile( JSONObject jsonServer ){
        try {
            return jsonServer.getString("serverKeyfile");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

// Settings
    public String               getSettingsString(){
        return jsonSettings.toString();
    }
    public String               getDefaultServer(){
        return getDefaultServer( jsonSettings );
    }
    public static String        getDefaultServer( JSONObject jsonSettings ){
        try {
            return jsonSettings.getString("defaultServer");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }


// ############################### Set ###############################
// Every set option is saved into configuration file

    public static boolean       setServer( JSONObject jsonServer, String servername, String hostname, String username, String password, String keyfile ){
    // Basic check
        if( jsonServer == null ) return false;

        try {
            jsonServer.put( "serverName", servername );
            jsonServer.put( "serverHostname", hostname );
            jsonServer.put( "serverUsername", username );
            jsonServer.put( "serverPassword", password );
            jsonServer.put( "serverKeyfile", keyfile );

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean              addServer( JSONObject jsonServer ){

        return addServer(
                getServerName(jsonServer),
                getServerHostname(jsonServer),
                getServerUsername(jsonServer),
                getServerPassword(jsonServer),
                getServerKeyfile(jsonServer)
                );
    }
    public boolean              addServer( String servername, String hostname, String username, String password, String keyfile ){
    // Vars
        JSONObject jsonServer = null;
        JSONObject jsonServerFound = null;
        boolean result = false;

    // Find server
        jsonServerFound = getServer( servername );
        if( jsonServerFound == null ){

            // Not found add a new one
            jsonServer = new JSONObject();
            jsonServers.put( jsonServer );

        } else {
            jsonServer = jsonServerFound;
        }

    // Set values
        result = setServer( jsonServer, servername, hostname, username, password, keyfile );
        if( ! result ) return false;

    // save to configuration file
        result = configSave();
        if( ! result ) return false;

        return true;
    }

// Settings
    public boolean              setDefaultServer( String defaultServer ){
    // Vars
        boolean result = false;

        setDefaultServer( jsonSettings, defaultServer );

    // save to configuration file
        result = configSave();
        if( ! result ) return false;

    }
    public static boolean       setDefaultServer( JSONObject jsonSettings, String defaultServer ){
        try {
            jsonSettings.put( "defaultServer" , defaultServer );
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean              setSettings( JSONObject jsonSettings ){
        setDefaultServer( getDefaultServer(jsonSettings) );

        return true;
    }

// ############################### Delete ###############################
    public static JSONArray     removeJSONArray( JSONArray jsonArray ,int pos ) {
        if( pos > 0 ) return null;

        JSONArray jsonArrayNew = new JSONArray();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                if (i != pos)
                    jsonArrayNew.put(jsonArray.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArrayNew;
    }

    public boolean              deleteServer( String servername ){
    // Vars
        int jsonServerIndex = -1;

    // Find server
        jsonServerIndex = getServerIndex( servername );
        if( jsonServers != null ){
            jsonServers = removeJSONArray( jsonServers, jsonServerIndex );
            return true;
        }

        return false;
    }

// ############################### Intent ###############################
    public static void          putServerToIntent( Intent intent, JSONObject jsonServer ){
        intent.putExtra("jsonServer", jsonServer.toString() );
    }
    public void                 putServerToIntent( Intent intent, String servername ){
        intent.putExtra("jsonServer", getServerString(servername));
    }

    public static void          putSettingsToIntent( Intent intent, JSONObject jsonSettings ){
        intent.putExtra("jsonSettings", jsonSettings.toString() );
    }
    public void                 putSettingsToIntent( Intent intent ){
        intent.putExtra("jsonSettings", getSettingsString());
    }


    private static JSONObject   getJsonFromIntent( Intent intent, String intentKey ){
        // Vars
        JSONObject jsonObject = null;

        // get extras
        final Bundle extras = intent.getExtras();
        if (extras != null) {

            try {
                String jsonString = extras.getString(intentKey);

                if( jsonString == null ) jsonString = "{}";
                jsonObject = new JSONObject(jsonString);


            } catch (JSONException e) {
                e.printStackTrace();
                jsonObject = new JSONObject();
            }

        } else {
            jsonObject = new JSONObject();
        }

        return jsonObject;
    }
    public static JSONObject    getServerFromIntent( Intent intent ) {
        return getJsonFromIntent( intent, "jsonServer" );
    }
    public static JSONObject    getSettingsFromIntent( Intent intent ){
        return getJsonFromIntent( intent, "jsonSettings" );
    }



}
