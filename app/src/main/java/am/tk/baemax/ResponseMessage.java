package am.tk.baemax;

public class ResponseMessage {
    String message;
    boolean isUser;

    public ResponseMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }
}
