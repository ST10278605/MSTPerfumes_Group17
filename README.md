MSTPerfumeApp/Web
# MST Perfume Website

A PHP-based e-commerce and community website for an online perfume store. The project includes product browsing, shopping cart functionality, user login/registration, reviews, forum-style pages, and an admin panel for managing products.

## Features

- Home landing page for the perfume brand
- Product catalog and shopping experience
- Shopping cart and order flow support
- User registration and login
- Admin dashboard for product management
- Customer reviews section
- Forum/community page
- MySQL database integration
- Docker-based local setup

## Tech Stack

- PHP 8.2
- Apache web server
- MySQL 8.0
- phpMyAdmin
- HTML, CSS, JavaScript

## Project Structure

- `Index.php` – main homepage
- `shopping.php` – product listing/shopping page
- `login.php` – user login and registration
- `cart.php` – shopping cart page
- `forum.php` – community/forum page
- `admin.php` – admin product management dashboard
- `adminlogin.php` – admin login page
- `MSTDBConn.php` – database connection setup
- `mst_website.sql` – SQL schema and sample data
- `docker-compose.yml` – container setup
- `Dockerfile` – PHP/Apache image configuration
- `css/` – stylesheets
- `js/` – JavaScript files
- `pictures/` – image assets

## Prerequisites

- Docker Desktop (recommended)
- Or a local PHP + MySQL environment

## Run with Docker

1. Open a terminal in the project folder.
2. Run:

```bash
docker compose up --build
```

3. Open the website in your browser:

- Website: http://localhost:8080
- phpMyAdmin: http://localhost:8081

4. The database is created automatically with:

- Host: `db`
- Database: `mst_website`
- User: `websiteuser`
- Password: `websitepassword`
- Root password: `rootpassword`

## Database Setup

This project includes the SQL file `mst_website.sql` for database setup.

If Docker is used, the database is created automatically, but you can also import the SQL file manually:

```bash
docker exec -i MST_website_db mysql -uroot -prootpassword mst_website < mst_website.sql
```

If you are running the project without Docker, import `mst_website.sql` into your MySQL database and make sure the credentials in `MSTDBConn.php` match your local database configuration.

## Local Manual Setup

If you prefer not to use Docker:

1. Install PHP, Apache, and MySQL.
2. Create a database named `mst_website`.
3. Import `mst_website.sql`.
4. Update the connection details in `MSTDBConn.php` to match your local environment.
5. Start your Apache server and open the project in a browser.

## Admin Access

The admin panel is available at:

- `adminlogin.php`
- `admin.php`

Create an admin user in the `users` table with the `role` set to `admin` before trying to access the admin dashboard. The application checks for a user with the admin role before allowing access.

## Notes

- The project is designed for a local development environment.
- Some files and functions may need minor fixes depending on database structure and hosting setup.
- For production use, consider adding stronger validation, security hardening, and environment-based configuration.

## License

This project is for educational/demo use unless otherwise specified by the project owner.

