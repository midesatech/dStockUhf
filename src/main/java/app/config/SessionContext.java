package app.config;

import domain.model.User;

public class SessionContext {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    public static void clear() {
        currentUser = null;
    }
}

