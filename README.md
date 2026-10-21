# EV ChargeNet - Smart EV Charging Network Planning & Management System

Micro Project 01 | ABC Green Energy Solutions Pvt. Ltd. | Java Enterprise + MySQL

## What it is
A complete EV charging network platform:
- **Customer web portal**: register, add EVs, book charging slots, join a live queue when
  chargers are busy, start/stop charging, view history, pay and download invoices.
- **Admin dashboard (JSP + Servlets)**: live stats, demand charts, station & charger
  management, queue monitoring, customers, daily/monthly reports, CSV export, database backup.
- **Desktop GUI (Swing)**: `dist/ev-chargenet-desktop-1.0.0.jar` - connect to the same MySQL
  database (DB_URL / DB_USER / DB_PASS env vars), view stations, book slots, see history.
- **Database**: MySQL via plain JDBC (JdbcTemplate). Schema auto-created on first run
  (`src/main/resources/schema.sql`).

## Course modules demonstrated
| Module | Where |
|---|---|
| I - Java basics | `util/Validators.java` (strings, regex, conditionals, input validation) |
| II - OOP | `model/Models.java`, service classes, encapsulation, records, interfaces (RowMapper, Runnable) |
| III - Exceptions, threads, files | `service/Exceptions.java`, `service/QueueProcessor.java` (daemon thread), `util/CsvUtil.java`, backup/export |
| IV - Collections & GUI | `Queue`, `LinkedList`, `HashMap`, lambdas in `service/BookingService.java`; Swing in `desktop/` |
| V - JDBC & web | JDBC everywhere via JdbcTemplate; `servlet/ExportServlet.java` (raw HttpServlet); JSP pages in `src/main/webapp/WEB-INF/jsp/` |

## Tech stack
Java 17, Spring Boot 2.7 (WAR, embedded Tomcat + Jasper), JSP + JSTL, raw Servlet,
JdbcTemplate (JDBC), MySQL 8, BCrypt passwords, Maven, Docker.

## Run locally
```
mvn clean package
java -jar target/ev-chargenet.war
# open http://localhost:8080 - runs on an in-memory DB by default
```

## Deploy on Render + Aiven (production, free)
1. **Aiven** (free MySQL): aiven.io -> sign up -> Create service -> MySQL -> Free plan ->
   Create. From the service page copy host, port, user, password, database name.
2. **GitHub**: create a repository and push this project.
3. **Render**: render.com -> New -> Web Service -> connect the repo ->
   Runtime: **Docker** (the included Dockerfile builds everything).
   Add environment variables:
   - `DB_URL` = `jdbc:mysql://<host>:<port>/<dbname>?sslMode=REQUIRED`
   - `DB_USER` = Aiven user
   - `DB_PASS` = Aiven password
   - `ADMIN_PASSWORD` = any strong password you choose (for the admin account)
4. Deploy. Tables are created automatically on first boot.
5. Sign in as **admin@abcgreen.in** with your `ADMIN_PASSWORD` to add stations and chargers.
   Customers self-register on the home page.

## Accounts
- Admin: `admin@abcgreen.in` (password from `ADMIN_PASSWORD` env var; if unset, one is
  generated and printed once in the server logs).
- Customers: self-register.

## Security notes
- Passwords are BCrypt-hashed. Duplicate emails / vehicle numbers / station names are rejected.
- Role-based access: admin pages require the ADMIN role.
- Full audit log of every action (visible on the admin dashboard).
