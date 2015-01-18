package pl.polsl.java.lab4.tomasz.domaracki.JavaBillsCollectionServerModel;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
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
    /**
     * Socket connected to server
     */
    private final Socket socket;
    /**
     * Buffer to send data to client
     */
    private final PrintWriter out;
    /**
     * Buffer to get data from client
     */
    private final BufferedInputStream in;
    /**
     * Variables informing about transmission status
     */
    private boolean transmission;
    /**
     * Stores information about last processed data
     */
    private String lastDataName;
    
    /**
     * The initiating constructor of the class ConnectionHandler
     * 
     * @param socket socket from client
     * @throws IOException 
     */
    public ConnectionHandler(Socket socket) throws IOException {
        this.socket = socket;
        out = new PrintWriter(
                new BufferedWriter(
                    new OutputStreamWriter(
                        socket.getOutputStream())), true);
        
        in = new BufferedInputStream(socket.getInputStream());
        
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
    
    /**
     * Sends information to client that an error occurred during transmission
     * and close connection with it
     */
    private void errorDuringTransmission(){
        out.println("Error during transmission. Can not continue, connection closed.");
        transmission = false;
    }
    
    /**
     * Checks if received data is expected one
     * 
     * @param dataName received data name
     * @return data status
     */
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
    
    /**
     * Waits for new data in buffer and returns it when it appears
     * 
     * @return data from stream
     * @throws IOException 
     */
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
    
    /**
     * Saves image file on server
     * 
     * @param buffer data from server
     * @param size size of data
     * @throws IOException 
     */
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
    
    /**
     * Saves database file on server
     * 
     * @param buffer data from server
     * @param size size of data
     * @throws IOException 
     */
    private void saveDatabaseFile(byte[] buffer, int size) throws IOException{
        FileOutputStream dbFileBackup = new FileOutputStream("dbBackup.billdb");
        dbFileBackup.write(buffer, 0, size);
        dbFileBackup.flush();
        dbFileBackup.close();
        out.println("Database file saved on server.");
    }            
    
    /**
     * Sends information that everything is saved and close connection with client
     */
    private void endOfTransmission(){
        out.println("Transmission successful. Connection closed.");
        transmission = false;
    }
}
