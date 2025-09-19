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


## Environment variables

```bash
 export PROFILE=dev
 export DB_HOST=localhost
 export DB_PORT=3306
 export DB_NAME=inventario
 export DB_USER=root
 export DB_PASS=root
```


```bash
 export PROFILE=prod
 export JDBC_URL="jdbc:mariadb://db:3306/inventario"
 export DB_USER=mdt_user
 export DB_PASS=mdt_pass
```