import java.util.ArrayList;
import java.util.List;

public class Account {

    private String username;
    private int authToken;
    private List<Message> messageBox;

    public Account(){
        username = "";
        authToken = -1;
        messageBox = new ArrayList<>();
    }
    public Account(String username, int authToken){
        this.username = username;
        this.authToken = authToken;
        this.messageBox = new ArrayList<>();
    }
    public Account(String username, int authToken, List<Message> messageBox){
        this.username = username;
        this.authToken = authToken;
        this.messageBox = messageBox;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAuthToken() {
        return authToken;
    }

    public void setAuthToken(int authToken) {
        this.authToken = authToken;
    }

    public List<Message> getMessageBox() {
        return messageBox;
    }

    public void setMessageBox(List<Message> messageBox) {
        this.messageBox = messageBox;
    }

    public void addMessageToMessageBox(Message message){
        messageBox.add(message);
    }

}