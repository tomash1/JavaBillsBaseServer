package pl.polsl.java.lab4.tomasz.domaracki.JavaBillsCollectionServerModel;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Class designed to become a model in MVC architecture
 * Handles connection from client
 *
 * @author tomaszdomaracki
 * @version 1.0.0
 */
public class ConnectionHandler extends Thread{

    private final Socket socket;
    
    private final PrintWriter out;
    
    private final BufferedInputStream in;
    
    private final InputStream inByte;
    
    private boolean transmission;
    
    private String lastDataName;
    
    public ConnectionHandler(Socket socket) throws IOException {
        this.socket = socket;
        out = new PrintWriter(
                new BufferedWriter(
                    new OutputStreamWriter(
                        socket.getOutputStream())), true);
        
        in = new BufferedInputStream(socket.getInputStream());
        
        inByte = socket.getInputStream();
        transmission = true;
        lastDataName = "";
        start();
    }
    
    @Override
    public void run(){
        out.println("Connection with server established.");
        byte[] buffer = null;
        while(true){
            String lineFromStream = "";
            int size = 0;
            try {
                 buffer = waitForData();
                 size = buffer.length;
                 lineFromStream = new String(buffer);
                 out.println("Server received bytes.");
                 switch(lineFromStream){
                     case "EOT": 
                         if (checkIfDataOrderIsAppropriate("EOT")){
                            endOfTransmission();
                         }
                         else{
                            errorDuringTransmission();
                         }
                         break;
                     case "DATABASE": 
                         if (checkIfDataOrderIsAppropriate("DATABASE")){
                            buffer = waitForData();
                            size = buffer.length;
                            saveDatabaseFile(buffer, size);
                         }
                         else{
                            errorDuringTransmission();
                         }
                         break;
                     case "IMAGE":
                         if (checkIfDataOrderIsAppropriate("IMAGE")){
                            buffer = waitForData();
                            size = buffer.length;
                            out.println("Server received image name.");
                            saveImageFile(buffer, size);
                         }
                         else{
                             errorDuringTransmission();
                         }
                         break;
                     default:
                         errorDuringTransmission();
                 }
                 
                 if (!transmission){
                     break;
                 }
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
                return;
            }
        }
        try {
            socket.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    private void errorDuringTransmission(){
        out.println("Error during transmission. Can not continue, connection closed.");
        transmission = false;
    }
    
    private boolean checkIfDataOrderIsAppropriate(String dataName){
        switch(dataName){
            case "DATABASE":
                if (!lastDataName.equals("")){
                    return false;
                }
                lastDataName = dataName;
                break;
            case "IMAGE":
                if (!lastDataName.equals("DATABASE")){
                    if(!lastDataName.equals("IMAGE")){
                        return false;
                    }
                }
                lastDataName = dataName;
                break;
            case "EOT":
                if (!lastDataName.equals("IMAGE")){
                    return false;
                }
                lastDataName = dataName;
                break;
            default: return false;                
        }
        return true;
    }
    
    private byte[] waitForData() throws IOException{
        int size = 0;
        while (size == 0){
            size = in.available();
             try {
                 sleep(500);
             } catch (InterruptedException ex) {
                 System.err.println(ex.getMessage());
             }
        }

        byte[] buffer = new byte[size];
        in.read(buffer, 0, size);
        return buffer;
    }
    
    private void saveImageFile(byte[] buffer, int size) throws IOException{
        String fileName = new String(buffer);
        buffer = waitForData();
        size = buffer.length;
        FileOutputStream imageFileBackup = new FileOutputStream(fileName);
        imageFileBackup.write(buffer, 0, size);
        imageFileBackup.flush();
        imageFileBackup.close();
        out.println("Image file " + fileName + " saved on server.");
    }
    
    private void saveDatabaseFile(byte[] buffer, int size) throws IOException{
        FileOutputStream dbFileBackup = new FileOutputStream("dbBackup.billdb");
        dbFileBackup.write(buffer, 0, size);
        dbFileBackup.flush();
        dbFileBackup.close();
        out.println("Database file saved on server.");
    }            
    
    private void endOfTransmission(){
        out.println("Transmission successful. Connection closed.");
        transmission = false;
    }
}
