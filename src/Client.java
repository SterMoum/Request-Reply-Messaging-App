import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
public class Client {
    // initialization: socket, input & output streams
    private Socket socket = null;
    private DataInputStream inputFromServer = null;
    private DataOutputStream outputToServer = null;
    // implementation of constructor
    public Client(String[] arguments) throws IOException {
        // do the connection
        try {
            String address = arguments[0];
            int port = Integer.parseInt(arguments[1]);
            socket = new Socket(address, port);
            inputFromServer = new DataInputStream(socket.getInputStream());
            outputToServer = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
        String username;
        String token;
        boolean isValid;
        //sending the function id to server in order to make the specific function
        int fn_id = Integer.parseInt(arguments[2]);
        outputToServer.write(fn_id);
        switch (fn_id){
            case 1: //Create account client side
                username = arguments[3];
                createAccount(username,inputFromServer,outputToServer);
                break;
            case 2: //Show Accounts client side
                try{
                    //send token to server
                    token = arguments[3];
                    outputToServer.writeUTF(token);
                    isValid = inputFromServer.readBoolean();
                    if(isValid){
                        //valid token
                        showAccounts(inputFromServer,outputToServer);
                    }
                    else{
                        //token is invalid. Connection must be closed
                        String message = inputFromServer.readUTF();
                        System.out.println(message);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
            case 3: //Send Message client side
                try{
                    //send token to server to check if it is valid
                    token = arguments[3];
                    outputToServer.writeUTF(token);
                    isValid = inputFromServer.readBoolean();
                    if(isValid){
                        //token is valid
                        username = arguments[4];
                        String messageBody = arguments[5];
                        sendMessage(username,messageBody,inputFromServer,outputToServer);
                    }else{
                        //token is invalid. Connection must be closed
                        String message = inputFromServer.readUTF();
                        System.out.println(message);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
            case 4: //Show inbox client side
                try{
                    //send token to server to check if it is valid
                    token = arguments[3];
                    outputToServer.writeUTF(token);
                    isValid = inputFromServer.readBoolean();
                    if(isValid){
                        //valid token
                        showInbox(inputFromServer,outputToServer);
                    }else{
                        //token is invalid. Connection must be closed..
                        String message = inputFromServer.readUTF();
                        System.out.println(message);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
            case 5: //Read Message client side
                try{
                    token = arguments[3];
                    outputToServer.writeUTF(token);
                    boolean flag = inputFromServer.readBoolean();
                    if(flag){
                        //valid token
                        //send message id to server
                        String message_id = arguments[4];
                        readMessage(message_id,inputFromServer,outputToServer);
                    }else{
                        //token is invalid. Connection must be closed
                        String message = inputFromServer.readUTF();
                        System.out.println(message);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
            case 6: //Delete account client side
                try{
                    token = arguments[3];
                    outputToServer.writeUTF(token);
                    boolean flag = inputFromServer.readBoolean();
                    if(flag){
                        //valid token
                        //send message id to server
                        String message_id = arguments[4];
                        deleteMessage(message_id,inputFromServer,outputToServer);
                    }else{
                        //token is invalid. Connection must be closed
                        String message = inputFromServer.readUTF();
                        System.out.println(message);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
            //token is invalid. Connection must be close

        }
// terminate the connection
        try {
            inputFromServer.close();
            outputToServer.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void createAccount(String username,DataInputStream inputFromServer,DataOutputStream outputToServer){
        try{
            //send username to the server
            outputToServer.writeUTF(username);
            //take result from server
            String message = inputFromServer.readUTF();
            System.out.println(message);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void showAccounts(DataInputStream inputFromServer, DataOutputStream outputToServer){
        try{
            //take number of accounts form server
            int length = inputFromServer.read();
            String message = "";
            for(int i = 0; i < length; i++){
                message = inputFromServer.readUTF();
                System.out.println(message);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void sendMessage(String username,String messageBody,DataInputStream inputFromServer,DataOutputStream outputToServer) {
        try {
            outputToServer.writeUTF(username);
            outputToServer.writeUTF(messageBody);
            boolean usernameExists = inputFromServer.readBoolean();
            if (usernameExists) {
                System.out.println("OK");
            } else {
                System.out.println("User does not exist");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showInbox(DataInputStream inputFromServer, DataOutputStream outputToServer){
        try{
            boolean isEmpty = inputFromServer.readBoolean();
            if(isEmpty){
                //inbox is empty
                String message = inputFromServer.readUTF();
                System.out.println(message);
            }else{
                //inbox not empty
                int lengthOfInboxMessages = inputFromServer.read();
                for(int i = 0; i < lengthOfInboxMessages; i++){
                    String message = inputFromServer.readUTF();
                    System.out.println(message);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void readMessage(String message_id, DataInputStream inputFromServer, DataOutputStream outputToServer){
        try{
            outputToServer.writeUTF(message_id);
            //get the appropriate message from server and print it
            String message = inputFromServer.readUTF();
            System.out.println(message);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void deleteMessage(String message_id,DataInputStream inputFromServer,DataOutputStream outputToServer){
        try{
            outputToServer.writeUTF(message_id);
            //get the appropriate message from server and print it
            String message = inputFromServer.readUTF();
            System.out.println(message);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        Client client = new Client(args);
    }
}