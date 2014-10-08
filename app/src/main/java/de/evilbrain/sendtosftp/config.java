package de.evilbrain.sendtosftp;

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



    public void             createTest(){
        createEmpty();
        configAddServer( "evilbrain.de");
        configAddServer( "testserver.de" );
    }

    private boolean         createEmpty(){

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

    public boolean          configRead(){

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

    public JSONObject       configAddServer( String name ){
    // Basic check
        if( jsonServers == null ) return null;

        JSONObject jsonServer = null;

        try {
            jsonServer = new JSONObject();
            jsonServer.put( "name", name );
            jsonServers.put(jsonServer);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonServer;
    }



    public JSONObject       getServer( int index ){
    // Basic check
        if( jsonServers == null ) return null;

    // Vars
        JSONObject jsonServer = null;

    // Get Server
        if( index < jsonServers.length() ) {
            try {
                jsonServer = jsonServers.getJSONObject(index);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    // Return
        return jsonServer;
    }



    public boolean          configSave() {

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


}
