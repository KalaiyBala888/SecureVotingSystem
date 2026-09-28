SmartEventOrganizer
====================

A simple Swing-based event organizer. The application stores events in MySQL by default, and falls back to an in-memory store when the MySQL JDBC driver isn't available.

Setup
-----
1. Install Java (JDK 17+ recommended).
2. Install MySQL and create a user.
3. Initialize the database (optional — the app will create tables if it can):

```powershell
mysql -u root -p < "C:\Users\hp\OneDrive\Desktop\KALAIJAVA\init_db.sql"
```

Environment variables (optional, defaults shown):
- `SMEO_DB_HOST` (default: `localhost`)
- `SMEO_DB_PORT` (default: `3306`)
- `SMEO_DB_NAME` (default: `smart_events`)
- `SMEO_DB_USER` (default: `root`)
- `SMEO_DB_PASS` (default: empty)

Running
-------
1. Download MySQL Connector/J (mysql-connector-java-x.x.x.jar).
2. Compile and run (update path to connector jar):

```powershell
javac -cp ".;C:\path\to\mysql-connector-java.jar" SmartEventOrganizer.java
java -cp ".;C:\path\to\mysql-connector-java.jar" SmartEventOrganizer
```

Notes
-----
- If the MySQL JDBC driver is not on the classpath, the app will run using an in-memory store (no persistence between runs).
- To persist data, ensure MySQL Connector/J is on the classpath and MySQL server is reachable with the configured credentials.

Need help adjusting DB credentials or packaging a runnable JAR? Reply and I will do it.