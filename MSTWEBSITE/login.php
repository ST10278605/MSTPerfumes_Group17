<?php
session_start();

error_reporting(E_ALL);
ini_set('display_errors', 1);

include "MSTDBConn.php";

$message = "";

/*
|--------------------------------------------------------------------------
| REGISTER USER
|--------------------------------------------------------------------------
*/

if (isset($_POST["register"])) {

    $name = trim($_POST["name"] ?? "");
    $email = trim($_POST["email"] ?? "");
    $password = $_POST["password"] ?? "";
    $role = $_POST["role"] ?? "user";

    // Basic validation
    if (empty($name) || empty($email) || empty($password)) {

        $message = "Please complete all required fields.";

    } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {

        $message = "Please enter a valid email address.";

    } elseif (strlen($password) < 8) {

        $message = "Password must be at least 8 characters.";

    } else {

        /*
        |--------------------------------------------------------------
        | Only allow user registration.
        | Admin accounts should be created manually.
        |--------------------------------------------------------------
        */

        $role = "user";

        // Check whether email already exists
        $checkStmt = $conn->prepare(
            "SELECT user_id FROM users WHERE email = ?"
        );

        $checkStmt->bind_param("s", $email);
        $checkStmt->execute();

        $checkResult = $checkStmt->get_result();

        if ($checkResult->num_rows > 0) {

            $message = "An account with this email already exists.";

        } else {

            // Hash password
            $hashedPassword = password_hash(
                $password,
                PASSWORD_DEFAULT
            );

            /*
            |----------------------------------------------------------
            | Insert new user
            |----------------------------------------------------------
            */

            $stmt = $conn->prepare(
                "INSERT INTO users
                (name, email, password, role)
                VALUES (?, ?, ?, ?)"
            );

            $stmt->bind_param(
                "ssss",
                $name,
                $email,
                $hashedPassword,
                $role
            );

            if ($stmt->execute()) {

                $message = "Registered successfully. You can login now.";

            } else {

                $message = "Registration error: " . $stmt->error;
            }

            $stmt->close();
        }

        $checkStmt->close();
    }
}


/*
|--------------------------------------------------------------------------
| LOGIN USER
|--------------------------------------------------------------------------
*/

if (isset($_POST["login"])) {

    $email = trim($_POST["email"] ?? "");
    $password = $_POST["password"] ?? "";

    if (empty($email) || empty($password)) {

        $message = "Please enter your email and password.";

    } else {

        /*
        |--------------------------------------------------------------
        | Find user using email
        |--------------------------------------------------------------
        */

        $stmt = $conn->prepare(
            "SELECT *
             FROM users
             WHERE email = ?"
        );

        $stmt->bind_param("s", $email);

        $stmt->execute();

        $result = $stmt->get_result();


        if ($result->num_rows > 0) {

            $user = $result->fetch_assoc();


            /*
            |----------------------------------------------------------
            | Verify password
            |----------------------------------------------------------
            */

            if (password_verify($password, $user["password"])) {

                /*
                |------------------------------------------------------
                | Create session variables
                |------------------------------------------------------
                */

                $_SESSION["user_id"] = $user["user_id"];

                $_SESSION["email"] = $user["email"];

                $_SESSION["name"] = $user["name"];

                $_SESSION["role"] = $user["role"];

                $_SESSION["is_verified_seller"] =
                    $user["is_verified_seller"] ?? 0;


                /*
                |------------------------------------------------------
                | Regenerate session ID for security
                |------------------------------------------------------
                */

                session_regenerate_id(true);


                /*
                |------------------------------------------------------
                | Redirect
                |------------------------------------------------------
                */

                $redirect = $_POST["redirect"] ?? "index.php";

                // Only allow local PHP pages
                if (
                    !preg_match('/^[a-zA-Z0-9_\-\/]+\.php$/', $redirect)
                ) {
                    $redirect = "index.php";
                }

                header("Location: " . $redirect);
                exit();

            } else {

                $message = "Incorrect password.";
            }

        } else {

            $message = "No account was found with that email address.";
        }

        $stmt->close();
    }
}


/*
|--------------------------------------------------------------------------
| LOGOUT
|--------------------------------------------------------------------------
*/

if (
    isset($_GET["logout"]) &&
    $_GET["logout"] === "1"
) {

    // Remove session variables
    $_SESSION = [];

    // Destroy session
    session_destroy();

    // Redirect to login
    header("Location: login.php");
    exit();
}
?>

<!DOCTYPE html>

<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0"
    >

    <link
        rel="stylesheet"
        href="css/style.css"
    >

    <link
        rel="icon"
        type="image/x-icon"
        href="pictures/logo.png"
    >

    <title>MST LOGIN</title>

</head>


<body>


<!-- =========================================================
     NAVIGATION
========================================================= -->

<header>

    <nav class="navbar">

        <div class="navbar-container">

            <div class="navbar-logo">

                <a href="index.php">

                    <img
                        src="pictures/logo.png"
                        alt="MST Perfumes Logo"
                    >

                </a>

            </div>

        </div>

    </nav>

</header>


<!-- =========================================================
     LOGIN
========================================================= -->

<main>

    <div class="wrapper">

        <div class="form-box">

            <h2>Login</h2>


            <!-- Display PHP message -->

            <?php if (!empty($message)): ?>

                <div class="message">

                    <?= htmlspecialchars(
                        $message,
                        ENT_QUOTES,
                        "UTF-8"
                    ) ?>

                </div>

            <?php endif; ?>


            <!-- LOGIN FORM -->

            <form
                action="login.php"
                method="POST"
            >

                <div class="input-box">

                    <input
                        type="email"
                        name="email"
                        placeholder=" "
                        required
                        autocomplete="email"
                    >

                    <label>Email</label>

                </div>


                <div class="input-box">

                    <input
                        type="password"
                        name="password"
                        placeholder=" "
                        required
                        autocomplete="current-password"
                    >

                    <label>Password</label>

                </div>


                <div class="remember-forgot">

                    <label>

                        <input
                            type="checkbox"
                            name="remember"
                        >

                        Remember me

                    </label>


                    <a href="#">
                        Forgot password?
                    </a>

                </div>


                <!-- Tells PHP this is the login request -->

                <button
                    type="submit"
                    name="login"
                    class="btn"
                >
                    Login
                </button>


                <div class="login-register">

                    <p>

                        Not a member?

                        <a href="register.php">
                            Register
                        </a>

                    </p>

                </div>

            </form>

        </div>

    </div>

</main>


<!-- =========================================================
     FOOTER
========================================================= -->

<footer>

    <h1 class="footer-brand">
        MST PERFUMES
    </h1>

    <p>
        &copy; <?= date("Y") ?>
        MST Perfumes. All rights reserved.
    </p>

</footer>


</body>

</html>