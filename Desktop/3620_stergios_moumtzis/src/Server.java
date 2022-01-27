import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    // initialization of lists and socket
    private static ArrayList<String> usernames; //list of registered usernames
    private static ArrayList<Account> accounts; //list of registered accounts
    private static ArrayList<Message> messages; //list of delivered messages
    private static ArrayList<Integer> tokens; //list of registered tokens
    private static int counter_id = 0; //counter for message id.Each time a message is sent it's increased by 1
    public Server(int port) {
        try {
            // start server and wait for a connection
            accounts = new ArrayList<>();
            usernames = new ArrayList<>();
            messages = new ArrayList<>();
            tokens = new ArrayList<>();
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started!");
            System.out.println("Waiting for a client ......");
            int resultFromClient = 0;
            Socket socket = null;
            try {
                while (true) {
                    socket = serverSocket.accept();
                    System.out.println("Client Accepted");
                    //create a new thread object
                    ClientHandler clientSock = new ClientHandler(socket);
                    //this thread will handle the client separately
                    new Thread(clientSock).start();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            // close connection
            if (socket != null) {
                socket.close();
            }
            System.out.println("Connection terminated.");
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    private static class ClientHandler implements Runnable{
        private final Socket socket;
        public ClientHandler(Socket socket){
            this.socket = socket;
        }
        @Override
        public void run() {
            try {
                DataInputStream inFromClient = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
                //new Thread((Runnable) socket).start();
                String tempToken;
                int givenToken;
                boolean flag;
                int resultFromClient = inFromClient.read();
                switch(resultFromClient){
                    case 1: //Create Account server side
                        createAccount(inFromClient, outToClient);
                        break;
                    case 2: //Show Accounts server side
                        //take token from client
                        tempToken = inFromClient.readUTF();
                        givenToken = Integer.parseInt(tempToken);
                        flag = isTokenValid(givenToken);
                        outToClient.writeBoolean(flag);
                        if(flag){
                            //token valid
                            showAccounts(inFromClient, outToClient);
                        }else{
                            //invalid token
                            outToClient.writeUTF("Invalid Auth Token");
                        }
                        break;
                    case 3: //Send Message server side
                        //checking if token's valid
                        try{
                            tempToken = inFromClient.readUTF();
                            givenToken = Integer.parseInt(tempToken);
                            flag = isTokenValid(givenToken);
                            outToClient.writeBoolean(flag);
                            if(flag){
                                //valid token
                                sendMessage(givenToken, inFromClient, outToClient);
                            }else{
                                //invalid token
                                outToClient.writeUTF("Invalid Auth Token");
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        break;
                    case 4: //Show inbox server side
                        try{
                            //checking if token is valid
                            tempToken = inFromClient.readUTF();
                            givenToken = Integer.parseInt(tempToken);
                            flag = isTokenValid(givenToken);
                            outToClient.writeBoolean(flag);
                            if (flag){
                                //valid token
                                showInbox(givenToken, inFromClient, outToClient);
                            }else{
                                //invalid token
                                outToClient.writeUTF("Invalid Auth Token");
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        break;
                    case 5: //Read message server side
                        try{
                            //checking if token is valid
                            tempToken = inFromClient.readUTF();
                            givenToken = Integer.parseInt(tempToken);
                            flag = isTokenValid(givenToken);
                            outToClient.writeBoolean(flag);
                            if (flag){
                                //valid token
                                readMessage(givenToken, inFromClient, outToClient);
                            }else{
                                //invalid token
                                outToClient.writeUTF("Invalid Auth Token");
                            }

                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        break;
                    case 6: //Delete Account server side
                        try{
                            //checking if token is valid
                            tempToken = inFromClient.readUTF();
                            givenToken = Integer.parseInt(tempToken);
                            flag = isTokenValid(givenToken);
                            outToClient.writeBoolean(flag);
                            if (flag){
                                //valid token
                                deleteMessage(givenToken, inFromClient, outToClient);
                            }else{
                                //invalid token
                                outToClient.writeUTF("Invalid Auth Token");
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        break;
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    private static void createAccount(DataInputStream inFromClient,DataOutputStream outToClient){
        try{
            //Take username from client. Store it in var 'username'
            String username = inFromClient.readUTF();
            int token = generateToken(username);
            String message;
            if (token == -2) {
                //send the appropriate string to client
                message = "Sorry, the user already exists";
            } else if (token == -1) {
                message = "Invalid Username";
            } else {
                message = Integer.toString(token);
            }
            outToClient.writeUTF(message);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private static void showAccounts(DataInputStream inFromClient,DataOutputStream outToClient){
        //print current registered accounts if token is valid
        try {
            int i = 1;
            outToClient.write(accounts.size());
            for (Account account : accounts) {
                outToClient.writeUTF(i++ + ". " + account.getUsername());
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private static void sendMessage(int givenToken, DataInputStream inFromClient,DataOutputStream outToClient){
        try{
            //get receiver's username from client
            String receiverUsername = inFromClient.readUTF();
            //get message body from client
            String messageBody = inFromClient.readUTF();
            outToClient.writeBoolean(usernames.contains(receiverUsername));
            if(usernames.contains(receiverUsername)){
                //send true to the client if receiver exists
                //find sender's username from the given token
                String senderUsername = "";
                int positionSender = -1;
                for(int i = 0; i < accounts.size() && positionSender == -1; i++){
                    if(accounts.get(i).getAuthToken() == givenToken){
                        senderUsername = accounts.get(i).getUsername();
                        positionSender = i;
                    }
                }
                Message messageSent = new Message(false,senderUsername,receiverUsername,counter_id++,messageBody);
                messages.add(messageSent);
                //find receiver's index in accounts array list
                int positionReceiver = -1;
                for(int i = 0; i < accounts.size() && positionReceiver == -1; i++){
                    if(accounts.get(i).getUsername().equals(receiverUsername)){
                        positionReceiver = i;
                    }
                }
                accounts.get(positionReceiver).addMessageToMessageBox(messageSent);
            }else{
                //send false to the client if there is no user with the given username
                outToClient.writeBoolean(false);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    private static void showInbox(int givenToken,DataInputStream inFromClient, DataOutputStream outToClient){
        try{
            //find user's position index in array list using token
            int positionUser = -1;
            for(int i = 0; i < accounts.size() && positionUser == -1; i++){
                if(accounts.get(i).getAuthToken() == givenToken){
                    positionUser = i;
                }
            }
            StringBuilder message = new StringBuilder();
            //checking if inbox of the user is empty
            outToClient.writeBoolean(accounts.get(positionUser).getMessageBox().isEmpty());
            if(accounts.get(positionUser).getMessageBox().isEmpty()){
                message.append("Inbox is empty");
                outToClient.writeUTF(message.toString());
            }else{
                //send to client the number of messages that exist in inbox
                outToClient.write(accounts.get(positionUser).getMessageBox().size());
                for(int i = 0; i < accounts.get(positionUser).getMessageBox().size(); i++){
                    //get id of the delivered message
                    int id = accounts.get(positionUser).getMessageBox().get(i).getId();
                    //get username of sender of the current message
                    String sender = accounts.get(positionUser).getMessageBox().get(i).getSender();
                    //isRead -> true if the message has been read by the receiver
                    boolean isRead = accounts.get(positionUser).getMessageBox().get(i).isRead();
                    if(isRead){
                        message.append(id).append(". from: ").append(sender);
                    }else{
                        message.append(id).append(". from: ").append(sender).append("*");
                    }
                    outToClient.writeUTF(message.toString());
                    //clear message
                    message.setLength(0);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private static void readMessage(int givenToken, DataInputStream inFromClient,DataOutputStream outToClient){
        try{
            String tempMessage_id = inFromClient.readUTF();
            int message_id = Integer.parseInt(tempMessage_id);
            //find if the message id exists
            boolean existsMessageId = false;
            for(int i = 0; (i < messages.size()) && !existsMessageId; i++){
                if(messages.get(i).getId() == message_id){
                    existsMessageId = true;
                }
            }
            if(existsMessageId){
                //message id exists
                //find user's position index in array list using token
                int positionUser = -1;
                for(int i = 0; i < accounts.size() && positionUser == -1; i++){
                    if(accounts.get(i).getAuthToken() == givenToken){
                        positionUser = i;
                    }
                }
                //find messageId's index in the specific account
                int positionMessageId = -1;
                for(int i = 0; i < accounts.get(positionUser).getMessageBox().size() && positionMessageId == -1;i++){
                    if(accounts.get(positionUser).getMessageBox().get(i).getId() == message_id){
                        positionMessageId = i;
                    }
                }
                String sender = accounts.get(positionUser).getMessageBox().get(positionMessageId).getSender();
                String messageBody = accounts.get(positionUser).getMessageBox().get(positionMessageId).getBody();

                //change isRead to true
                accounts.get(positionUser).getMessageBox().get(positionMessageId).setRead(true);

                String message = "(" + sender + ") " + messageBody;

                outToClient.writeUTF(message);

            }else{
                //message id does not exist
                String message = "Message " + message_id + " does not exist";
                outToClient.writeUTF(message);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    private static void deleteMessage(int givenToken, DataInputStream inFromClient,DataOutputStream outToClient){
        try{
            String tempMessage_id = inFromClient.readUTF();
            int message_id = Integer.parseInt(tempMessage_id);
            //find if the message id exists
            boolean existsMessageId = false;
            for(int i = 0; (i < messages.size()) && !existsMessageId; i++){
                if(messages.get(i).getId() == message_id){
                    existsMessageId = true;
                }
            }
            if(existsMessageId){
                //message id exists
                //find user's position index in array list using token
                int positionUser = -1;
                for(int i = 0; i < accounts.size() && positionUser == -1; i++){
                    if(accounts.get(i).getAuthToken() == givenToken){
                        positionUser = i;
                    }
                }
                //find messageId's index in the specific account
                int positionMessageId = -1;
                Message messageToDelete = null;
                System.out.println("SIZE OF INBOX " + accounts.get(positionUser).getMessageBox().size());
                for(int i = 0; i < accounts.get(positionUser).getMessageBox().size() && positionMessageId == -1;i++){
                    if(accounts.get(positionUser).getMessageBox().get(i).getId() == message_id){
                        positionMessageId = i;
                        messageToDelete = accounts.get(positionUser).getMessageBox().get(i);
                    }
                }
                //delete the message with given id
                accounts.get(positionUser).getMessageBox().remove(messageToDelete);
                messages.remove(messageToDelete);
                String message = "OK";
                outToClient.writeUTF(message);

            }else{
                //message id does not exist
                String message = "Message " + message_id + " does not exist";
                outToClient.writeUTF(message);
            }

        }catch (IOException e){
            e.printStackTrace();
        }

    }
    /**
     *
     * @param string
     * @return false if string cantains any special character except '_'
     */
    private static boolean containsSpecialCharacter(String string){
        Pattern p = Pattern.compile("[^a-z0-9_]", Pattern.CASE_INSENSITIVE);
        //Pattern allowedCharacter = Pattern.compile("[_]");
        Matcher m = p.matcher(string);
        //Matcher n = allowedCharacter.matcher(string);
        boolean flag = m.find();
        //boolean c = n.find();
        if (flag){
            return false;
        }
        return true;
    }
    private static int generateToken(String username){
        //create an account
        int token = -1;
        if (containsSpecialCharacter(username)){
            //if username is valid(ex. ster_moumtzis) check if already exists
            if (usernames.contains(username)){
                return -2;
                //error code for already existed username
            }
            //account.setUsername(username);
            usernames.add(username);
            //account.setMessageBox(null);
            Random rand = new Random(); //instance of random class
            int upperbound = 10000;
            //generate random values from 0-10000
            token = rand.nextInt(upperbound);
            //in case token already exists we need to regenerate a new token
            if(tokens.contains(token)){
                while(tokens.contains(token))
                    token = rand.nextInt(upperbound);
            }
            tokens.add(token);
            Account account = new Account(username,token);
            accounts.add(account);
        }
        return token;

    }
    //returns true if the token belongs to a user
    private static boolean isTokenValid(int givenToken) {
        boolean flag = false;
        //check if given token exists
        for(Account account : accounts) {
            if (account.getAuthToken() == givenToken) {
                flag = true;
                break;
            }
        }
        return flag;
    }
    public static void main(String[] args) {
        Server server = new Server(Integer.parseInt(args[0]));
    }

}

