# MST Perfume Website

## Video link for App Presentation
https://drive.google.com/file/d/1uC9yH4C8FeUr7D5rOXt0N5QIRzWn4A2F/view?pli=1

## Active DevOps Link 
https://dev.azure.com/ST10446802/MST%20PERFUMES  


# MOBILE APPLICATION SCREENSHOTS & FEATURES
### User Registration and Secure Login Interface

When users open the MST Perfumes app for the first time, they can sign in or choose **Register Now** to create an account. Registration collects the user's name, email address, phone number, and password. After completing the form, users can sign in with their account.

Passwords should not be stored as plain text. The app hashes a password before saving it, then checks a hash of the password entered at sign-in against the stored value. This helps protect users' actual passwords if the database is exposed.

#### 1.Registration / Sign-Up Screen

<img width="599" height="326" alt="RegistrationSign-Up Screen" src="https://github.com/user-attachments/assets/59efe201-c6fc-4ea4-b313-53ad1c86b15f" />

#### 2. Login Screen

<img width="661" height="357" alt="Login Screen" src="https://github.com/user-attachments/assets/445c9f33-97c8-4f2c-97f8-a6056773b4fb" />

#### 3. Error message

<img width="643" height="357" alt="Error message" src="https://github.com/user-attachments/assets/3ca0533e-2f7c-4635-ae9e-41731ea04ae1" />

### Single Sign-On (SSO) Integration Proof

For our Single Sign-On (SSO) feature, we integrated Google Sign-In using Firebase Authentication. This allows users to skip the lengthy registration process entirely. Instead of typing out their name, email, and a new password, they simply tap the "Sign in with Google" button on our login screen.
When they tap it, a secure Google pop-up window appears, letting them choose which Google account they want to use. Once they select an account, Google handles all the authentication behind the scenes and passes a secure token back to our app. The app then automatically logs them in, creates a session, and takes them straight to the home screen. This session authentication works perfectly across both our mobile app and our web interfaces, meaning if they log in on their phone, their account state is securely maintained without needing to log in again.

#### 1. The "Sign in with Google" Button

<img width="1160" height="627" alt="1" src="https://github.com/user-attachments/assets/a219d57e-60f9-4b03-9be1-29101ef890dd" />

#### 2. Google Pop-up Window

<img width="1152" height="634" alt="2" src="https://github.com/user-attachments/assets/7f4fc348-f4d4-429d-a2ce-9da6c42bece9" />

#### 3. Successful Redirect

<img width="1248" height="679" alt="3" src="https://github.com/user-attachments/assets/7fc8e50e-bb12-4a06-8cf6-c47cb62c1f75" />

### User Settings and Password Management Screen

Our app is designed to give users complete control over their accounts and personal information. When a user navigates to their profile, they are greeted by a clean Settings menu where they can update their personal preferences, manage their delivery addresses, and adjust notification settings.
To keep their account secure, users can easily change their password right from this menu. When they tap "Change Password," they are taken to a secure form. Here, they must first enter their current password to prove their identity. Then, they type in their new password and confirm it by typing it a second time. The app validates that the new password is strong enough and that both new entries match perfectly. Once submitted, the app securely hashes the new password and updates it in the database, ensuring the user's account remains protected.

#### 1. The User Profile / Settings Screen

<img width="1189" height="616" alt="4" src="https://github.com/user-attachments/assets/ca765db0-b90b-443d-b4e8-91360eb4bb03" />

#### 2.	The Change Password Form

<img width="1379" height="765" alt="5" src="https://github.com/user-attachments/assets/64196a5e-1a3e-433b-bf44-a5b55ffcd51a" />

### Built-in User Support and Help Documentation System

We know that users can sometimes get stuck or confused while using the app, so we built a dedicated Help and Support system right into the application. From the main menu, users can tap "Help & FAQ" to open a screen filled with clear, easy-to-read instructions covering everything from how to create an account to how to track an order. The screen uses an expandable layout, so users only see the answers to the questions they need.
For more detailed guidance, we also included a "Download User Manual" button at the bottom of the screen. When tapped, the app automatically downloads our official MST Perfumes User Guide PDF directly to the user's phone, allowing them to read the full instructions offline whenever they need them.

#### 1.	The Help / FAQ Screen

<img width="1056" height="555" alt="6" src="https://github.com/user-attachments/assets/dcc8974f-69d6-4ba9-b5c3-f5ee00230c75" />

### Database Screenshot
<img width="1501" height="964" alt="FIREBASE SCREENSHOT MST" src="https://github.com/user-attachments/assets/ff2338b5-6ea3-41bd-bf18-9b9254d70617" />


# WEBSITE FEATURES
### Registration / Sign-Up Screen

A PHP-based e-commerce and community website for an online perfume store. The project includes product browsing, shopping cart functionality, user login/registration, reviews, forum-style pages, and admin management.

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

## App Login details for Admin

- Username: Admin001@gmail.com
- Password: Admin001@

Create an admin user in the `users` table with the `role` set to `admin` before trying to access the admin dashboard. The application checks for a user with the admin role before allowing access.

## Notes

- The project is designed for a local development environment.
- Some files and functions may need minor fixes depending on database structure and hosting setup.
- For production use, consider adding stronger validation, security hardening, and environment-based configuration.

## License

This project is for educational/demo use unless otherwise specified by the project owner.
