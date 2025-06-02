package movies;

import jakarta.servlet.http.HttpSession;

public class SessionAttribute<T> {
    private final Class<T> clazz;
    private final String name;

    SessionAttribute(Class<T> clazz, String name) {
        this.name = name;
        this.clazz = clazz;
    }

    T get(HttpSession session) {
        return clazz.cast(session.getAttribute(name));
    }

    void set(HttpSession session, T value) {
        session.setAttribute(name, value);
    }
}