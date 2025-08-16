package app.session;

import domain.model.User;

public class UserSession {
    private static User current;
    public static void setCurrent(User u){ current = u; }
    public static User getCurrent(){ return current; }
    public static void clear(){ current = null; }
}