# COVID-19 Dashboard

This Spring Boot application serves a country-wise COVID-19 dashboard and a
REST API backed by MySQL. If the `country_stats` table is empty when the
application starts, it inserts 20 country records from the dataset supplied
for this project, ranked by confirmed cases and including India. These are
historical sample figures, not current live COVID-19 data.

## Run locally

Requirements: Java 17 or later and a running MySQL server.

1. Create a database named `covid_db`.
2. Set the database credentials in PowerShell (replace the example password
   with your own; do not commit it):

   ```powershell
   $env:DATABASE_URL = "jdbc:mysql://localhost:3306/covid_db"
   $env:DATABASE_USERNAME = "root"
   $env:DATABASE_PASSWORD = "your-local-password"
   ```

3. From the same PowerShell window, start the application:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. Open <http://localhost:8080/>.

The login page is available at <http://localhost:8080/index2.html>. If you
open it with VS Code Live Server on port 5500, keep the Spring Boot application
running; the page will send login and signup requests to `http://localhost:8080`.
For Live Server, use `http://localhost:5500` or `http://127.0.0.1:5500`; these
origins are explicitly allowed for the API.

Login creates a server-side session. The browser keeps the session cookie and
uses a CSRF token for login, signup, and logout requests. The country-data GET
endpoints remain public, while adding country statistics requires a signed-in
user. Use the dashboard's **Log out** button to end the session.

For signup or login requests from Postman, first send
`GET http://localhost:8080/api/user/csrf`. Keep the `XSRF-TOKEN` cookie from that
response, then send the returned token in the `X-XSRF-TOKEN` header on the POST
request. Keep both requests on the same host (`localhost`); `localhost` and
`127.0.0.1` use separate cookies. The signup JSON uses `username` for the
email address (the `email` property is not used):

```json
{
  "username": "person@example.com",
  "password": "your-password"
}
```

## API

The dashboard loads records from `GET /api/covid/all`. Demo rows are inserted
only when the table is empty; existing records are not overwritten.
The country table supports searching by country or region name and sorting by
name, confirmed cases, deaths, recovered cases, or active cases. Search and
sorting affect only the table; the overview totals continue to show all records.
By default, the table shows the 20 countries with the most confirmed cases.
Choose "All countries" to view the full list.
If the database contains only a recognized set of the application's demo/test
rows, startup replaces those rows with the 20 supplied historical sample
records. Other existing database records are left unchanged.
The country comparison chart shows the top 10 countries in the selected list
for the chosen statistic. It follows the country search and list selection.

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/covid/all` | List country statistics |
| `GET` | `/api/covid/country/{countryRegion}` | Find a country by name |
| `GET` | `/api/covid/search?countryRegion={text}` | Search country names |
| `POST` | `/api/covid/add` | Add country statistics (login required) |
| `PUT` | `/api/covid/country/{id}` | Replace/update country statistics (login required) |
| `DELETE` | `/api/covid/country/{id}` | Delete country statistics (login required) |
| `POST` | `/api/user/signup` | Create an account |
| `GET` | `/api/user/csrf` | Get a CSRF token for browser requests |
| `POST` | `/api/user/login` | Sign in and create a session |
| `POST` | `/api/user/logout` | End the current session |

Example request body for `POST /api/covid/add`:

```json
{
  "countryRegion": "Example",
  "confirmed": 120,
  "deaths": 4,
  "recovered": 90,
  "active": 26
}
```

Counts are stored as supplied, and the dashboard calculates active cases from
confirmed, deaths, and recovered when the active value is not present.

For updates, use the record's numeric database ID:

```json
PUT /api/covid/country/1
{
  "countryRegion": "Example",
  "confirmed": 130,
  "deaths": 5,
  "recovered": 100,
  "active": 25
}
```

A successful update returns the updated record. Use
`DELETE /api/covid/country/{id}` to remove a record. Both endpoints require an
authenticated session; a missing ID returns HTTP 404.

The login page uses the email as `username` and sends credentials as JSON:

```json
{
  "username": "person@example.com",
  "password": "your-password"
}
```

Signup creates an account immediately. Login verifies the email and password
against the stored BCrypt hash, then creates an authenticated session.

Run the test suite with:

```powershell
.\mvnw.cmd test
```

## Deployment configuration

Deploy the Spring Boot application with Java 17 or later and a reachable MySQL
database. Set these environment variables in the hosting provider's secret or
environment-variable settings; do not put production credentials in source
files:

| Variable | Required | Purpose |
| --- | --- | --- |
| `DATABASE_URL` | Yes | JDBC URL, for example `jdbc:mysql://host:3306/covid_db` |
| `DATABASE_USERNAME` | Yes | MySQL username |
| `DATABASE_PASSWORD` | Yes | MySQL password |
| `PORT` | If required by host | HTTP port; defaults to `8080` |
| `SESSION_COOKIE_SECURE` | Yes for HTTPS | Set to `true` when the site is served over HTTPS |
| `APP_CORS_ALLOWED_ORIGINS` | Only for a separate frontend | Comma-separated exact frontend origins, with no trailing slash |
| `DDL_AUTO` | Optional | Hibernate schema mode; defaults to `update` |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Optional | SMTP credentials if email sending is configured |

Build and run the executable JAR from the project directory:

```powershell
.\mvnw.cmd clean package
java -jar target/covid-dashboard-0.0.1-SNAPSHOT.jar
```

When the frontend is served by this same Spring Boot application, CORS does not
need an additional origin. If hosting the frontend separately, set
`APP_CORS_ALLOWED_ORIGINS` to its exact HTTPS origin. Keep the database on a
persistent managed service and back it up; this app currently uses Hibernate
`update` and does not include a database migration tool. The COVID figures
loaded at startup are historical sample data, not live data.

### AWS EC2 (Amazon Linux 2023)

The repository includes a systemd unit at
[`deploy/aws/covid-dashboard.service`](./deploy/aws/covid-dashboard.service).
Build the JAR locally, launch an Amazon Linux 2023 EC2 instance, and install
Java 17:

```bash
sudo dnf install -y java-17-amazon-corretto-headless
sudo useradd --system --home-dir /opt/covid-dashboard --shell /sbin/nologin covid-dashboard
sudo install -d -o covid-dashboard -g covid-dashboard /opt/covid-dashboard
```

Upload `target/covid-dashboard-0.0.1-SNAPSHOT.jar` to the instance, then place
it in `/opt/covid-dashboard/` with owner `covid-dashboard`. Create a
root-readable-only environment file and add the MySQL connection settings from
the table above:

```bash
sudo install -o root -g root -m 600 /dev/null /etc/covid-dashboard.env
sudoedit /etc/covid-dashboard.env
```

For example, set `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and
`PORT=8080` in that file. Keep the password private. Copy the service file to
`/etc/systemd/system/covid-dashboard.service`, then enable and start it:

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now covid-dashboard
sudo systemctl status covid-dashboard
curl http://localhost:8080/api/covid/all
```

Allow the EC2 instance to reach MySQL through its network/security-group rules;
prefer allowing the database connection from the EC2 security group rather
than exposing MySQL publicly. Do not expose port `8080` publicly. For public
traffic, put an HTTPS-enabled load balancer or reverse proxy in front of the
app, allow only the required inbound ports, and then set
`SESSION_COOKIE_SECURE=true` in the environment file.
