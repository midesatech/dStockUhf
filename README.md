Inventario Desktop - Final (Gradle)
==================================

Contenido: JavaFX + Hexagonal + JPA (Hibernate+Hikari) + Login with BCrypt + dynamic menu + seeds + SQL scripts.

Run:
    ./gradlew run    # or gradlew.bat run on Windows

Default DB config (persistence.xml):
    JDBC URL: jdbc:mariadb://localhost:3306/inventario
    User: root
    Password: (empty)

To use DB on startup, in src/main/java/app/MainApp.java set AppBootstrap.init(true);