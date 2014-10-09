package de.evilbrain.sendtosftp;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

/**
 * Created by stackshadow on 07.10.14.
 */
public class sshConnect {

// Infos
    private String      serverHostName = null;
    private String      serverUserName = null;
    private String      serverPassword = null;
    private String      serverKeyFile = null;
    private boolean     serverIsConnected = false;
    public String       errorMessage;

// JSch
    private JSch        jsch = null;
    private Session     jschSession = null;

    public void         setHost( String hostName ){
        serverHostName = hostName;
    }
    public void         setUsername( String userName ){
        serverUserName = userName;
    }
    public void         setPassword( String password ){
        serverPassword = password;
    }
    public void         setKeyFile( String keyFIle ){
        serverKeyFile = keyFIle;
    }


    public boolean      connect(){

    // Connect
        try {

        // Create if needed
            if( jsch == null ) jsch = new JSch();
            if( jschSession == null ) jschSession = jsch.getSession( serverUserName, serverHostName, 22);

        // Check if already connected
            if( jschSession.isConnected() ){
                serverIsConnected = true;
                return true;
            } else {
                serverIsConnected = false;
            }

        // password authentification
            jschSession.setHost( serverHostName );
            jschSession.setPort(22);
            jschSession.setPassword( serverPassword );

        // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            jschSession.setConfig(prop);

        // Connect
            jschSession.connect();

        // Check if connect
            serverIsConnected = jschSession.isConnected();

        } catch (JSchException e) {
            e.printStackTrace();
            errorMessage = e.getLocalizedMessage();

            disconnect();
        }



        return serverIsConnected;
    }

    public boolean      disconnect(){
    // Basic check
        if( jschSession == null ) return true;

        if( jschSession.isConnected() ) {
            jschSession.disconnect();
        }

        serverIsConnected = false;
        jschSession = null;

        return true;
    }

    public boolean      connectTest(){
        try {
            // SSH Channel
            ChannelExec channelssh = (ChannelExec)
            jschSession.openChannel("exec");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channelssh.setOutputStream(baos);

            // Execute command
            channelssh.setCommand("ls");
            channelssh.connect();
            channelssh.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
            errorMessage = e.getLocalizedMessage();
            return false;
        }

        return true;
    }



    public boolean      isConnected(){
        return serverIsConnected;
    }

}
