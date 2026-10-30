<?php
session_start();

error_reporting(E_ALL);
ini_set('display_errors', 1);

include "MSTDBConn.php";

$message = "";

function finalizeUserSession($conn, $user) {
    $_SESSION["user_id"] = $user["user_id"];
    $_SESSION["email"] = $user["email"];
    $_SESSION["name"] = $user["name"];
    $_SESSION["role"] = $user["role"];
    $_SESSION["is_verified_seller"] = $user["is_verified_seller"] ?? 0;
    session_regenerate_id(true);
}

function mergeGuestCartIntoUser(mysqli $conn, int $userId): void {
    if (!isset($_SESSION['cart']) || !is_array($_SESSION['cart']) || empty($_SESSION['cart'])) {
        return;
    }

    $stmt = $conn->prepare('SELECT cart_id FROM carts WHERE user_id = ? LIMIT 1');
    if (!$stmt) {
        return;
    }

    $stmt->bind_param('i', $userId);
    $stmt->execute();
    $cartRow = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    $cartId = $cartRow ? (int) $cartRow['cart_id'] : 0;
    if ($cartId === 0) {
        $insert = $conn->prepare('INSERT INTO carts (user_id) VALUES (?)');
        if (!$insert) {
            return;
        }

        $insert->bind_param('i', $userId);
        if (!$insert->execute()) {
            $insert->close();
            return;
        }

        $cartId = (int) $conn->insert_id;
        $insert->close();
    }

    if ($cartId <= 0) {
        return;
    }

    $delete = $conn->prepare('DELETE FROM cart_items WHERE cart_id = ?');
    if ($delete) {
        $delete->bind_param('i', $cartId);
        $delete->execute();
        $delete->close();
    }

    $insertItem = $conn->prepare('INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?)');
    if (!$insertItem) {
        return;
    }

    foreach ($_SESSION['cart'] as $pid => $qty) {
        $productId = filter_var($pid, FILTER_VALIDATE_INT);
        if ($productId === false || $productId < 1) {
            continue;
        }

        $quantity = max(1, (int) $qty);
        $insertItem->bind_param('iii', $cartId, $productId, $quantity);
        $insertItem->execute();
    }

    $insertItem->close();
}

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['google_auth']) && $_POST['google_auth'] === '1') {
    $email = trim((string) ($_POST['email'] ?? ''));
    $name = trim((string) ($_POST['name'] ?? ''));

    if ($email === '') {
        $message = "Google login failed. Please try again.";
    } else {
        $name = $name !== '' ? $name : 'Google User';

        $stmt = $conn->prepare("SELECT * FROM users WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result && $result->num_rows > 0) {
            $user = $result->fetch_assoc();
            if (($user['role'] ?? 'user') === 'admin') {
                finalizeUserSession($conn, $user);
                header('Location: admin_dashboard.php');
                exit();
            }
            finalizeUserSession($conn, $user);
            $redirect = $_POST['redirect'] ?? 'index.php';
            if (!preg_match('/^[a-zA-Z0-9_\-\/]+\.php$/', $redirect)) {
                $redirect = 'index.php';
            }
            header('Location: ' . $redirect);
            exit();
        }

        $tempPassword = 'google_' . bin2hex(random_bytes(24));
        $hashedPassword = password_hash($tempPassword, PASSWORD_DEFAULT);
        $insert = $conn->prepare("INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, 'user')");
        $insert->bind_param("sss", $name, $email, $hashedPassword);

        if ($insert->execute()) {
            $userId = $conn->insert_id;
            $newUserStmt = $conn->prepare("SELECT * FROM users WHERE user_id = ?");
            $newUserStmt->bind_param("i", $userId);
            $newUserStmt->execute();
            $newUserResult = $newUserStmt->get_result();
            $user = $newUserResult->fetch_assoc();
            if (($user['role'] ?? 'user') === 'admin') {
                finalizeUserSession($conn, $user);
                header('Location: admin_dashboard.php');
                exit();
            }
            finalizeUserSession($conn, $user);
            $redirect = $_POST['redirect'] ?? 'index.php';
            if (!preg_match('/^[a-zA-Z0-9_\-\/]+\.php$/', $redirect)) {
                $redirect = 'index.php';
            }
            header('Location: ' . $redirect);
            exit();
        } else {
            $message = 'Unable to create your Google account. Please try again.';
        }
    }
}

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

                mergeGuestCartIntoUser($conn, (int) $user["user_id"]);

                session_regenerate_id(true);

                /*
                |------------------------------------------------------
                | Redirect
                |------------------------------------------------------
                */

                $redirect = $_POST["redirect"] ?? "index.php";

                // Admins always go to the dashboard after login.
                if (($user["role"] ?? "") === "admin") {
                    $redirect = "admin_dashboard.php";
                }

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

if (
    isset($_GET["logout"]) &&
    $_GET["logout"] === "1"
) {
    $_SESSION = [];
    session_destroy();
    header("Location: login.php");
    exit();
}

if (isset($_SESSION['user_id']) && strtolower((string) ($_SESSION['role'] ?? 'user')) === 'admin') {
    header('Location: admin_dashboard.php');
    exit();
}

if (isset($_SESSION['user_id']) && strtolower((string) ($_SESSION['role'] ?? 'user')) !== 'admin') {
    header('Location: index.php');
    exit();
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"> 
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" href="css/style.css">
<link rel="icon" type="image/x-icon" href="pictures/logo.svg">
<title>MST LOGIN</title>

<script type="module">
    import { loginWithGoogle } from './js/form.js';

    window.loginWithGoogle = async function () {
        const result = await loginWithGoogle();

        if (result.success && result.user && result.user.email) {
            const form = document.getElementById('googleLoginForm');
            form.name.value = result.user.displayName || 'Google User';
            form.email.value = result.user.email;
            form.google_auth.value = '1';
            form.submit();
        } else {
            alert(result.error || 'Google login failed.');
        }
    };
</script>
</head>


<body>

<header>

    <nav class="navbar">

        <div class="navbar-container">

            <div class="navbar-logo">

                <a href="index.php">

                    <img src="pictures/logo.jpeg" alt="MST Perfumes Logo" >

                </a>

            </div>

        </div>

    </nav>

</header>


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

            <form action="login.php" method="POST">

                <div class="input-box">

                    <input type="email" name="email" placeholder=" " required autocomplete="email">

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

                <div class="divider"><span>or</span></div>

                <button
                    type="button"
                    class="btn"
                    onclick="loginWithGoogle()"
                >
                    Continue with Google
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

            <form id="googleLoginForm" action="login.php" method="POST" style="display:none;">
                <input type="hidden" name="google_auth" value="0">
                <input type="hidden" name="name" value="">
                <input type="hidden" name="email" value="">
                <input type="hidden" name="redirect" value="index.php">
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