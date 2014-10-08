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
    private boolean     serverIsConnected = false;
    public String       errorMessage;

// JSch
    JSch                jsch = null;
    Session             jschSession = null;

    public void         setHost( String hostName ){
        serverHostName = hostName;
    }
    public void         setUsername( String userName ){
        serverUserName = userName;
    }
    public void         setPassword( String password ){
        serverPassword = password;
    }


    public boolean      connect(){

        // TODO check if already connected
        serverIsConnected = false;

        try {
            jsch = new JSch();
            jschSession = jsch.getSession( serverUserName, serverHostName, 22);
            jschSession.setPassword( serverPassword );

        // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            jschSession.setConfig(prop);

        // Connect
            jschSession.connect();

            serverIsConnected = true;

        } catch (JSchException e) {
            e.printStackTrace();
            errorMessage = e.getLocalizedMessage();
        }

        return serverIsConnected;
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
