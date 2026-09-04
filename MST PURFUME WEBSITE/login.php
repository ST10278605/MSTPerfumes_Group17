<?php
session_start();
error_reporting(E_ALL);
ini_set('display_errors', 1);
include "MSTDBConn.php";

$message = "";
$active_form = "login"; // track which form should show

// allow toggling via ?show=register or ?show=login
if (isset($_GET['show']) && in_array($_GET['show'], ['register', 'login'])) {
    $active_form = $_GET['show'];
}

// REGISTER USER
if (isset($_POST["register"])) {
    $active_form = "register";
    $name = trim($_POST["name"] ?? '');
    $email = trim($_POST["email"] ?? '');
    $password = $_POST["password"] ?? ''; // Fixed: use null coalescing
    $role = $_POST["role"] ?? 'user';

    if (empty($name) || empty($email) || empty($password)) {
        $message = "All fields are required";
    } elseif (strlen($password) < 8) {
        $message = "Password must be at least 8 characters";
    } else {
        $hashedPassword = password_hash($password, PASSWORD_DEFAULT);

        $stmt = $conn->prepare("INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)");
        $stmt->bind_param("ssss", $name, $email, $hashedPassword, $role);

        try {
            $stmt->execute();
            $message = "Registered successfully. You can login now.";
            $active_form = "login";
        } catch (mysqli_sql_exception $e) {
            if ($e->getCode() == 1062) {
                $message = "Email already exists";
            } else {
                $message = "Error: " . $e->getMessage();
            }
        }
        $stmt->close();
    }
}

// LOGIN USER
if (isset($_POST["login"])) {
    $active_form = "login";
    $email = trim($_POST["email"] ?? '');
    $password = $_POST["password"] ?? '';

    if ($email === '' || $password === '') {
        $message = "Email and password required";
    } else {
        $stmt = $conn->prepare("SELECT id, name, email, password, role FROM users WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($user = $result->fetch_assoc()) {
            if (isset($user["password"]) && password_verify($password, $user["password"])) {
                $_SESSION["user_id"] = $user["id"];
                $_SESSION["name"] = $user["name"];
                $_SESSION["email"] = $user["email"];
                $_SESSION["role"] = $user["role"];
                
                $redirect = $_POST["redirect"] ?? "index.php";
                header("Location: " . $redirect);
                exit();
            } else {
                $message = "Wrong password";
            }
        } else {
            $message = "User not found";
        }
        $stmt->close();
    }
}

// LOGOUT
if (isset($_GET["logout"])) {
    session_destroy();
    header("Location: login.php");
    exit();
}

// Already logged in - but only redirect if not logging out
if (isset($_SESSION["user_id"]) && !isset($_GET["logout"])) {
    header("Location: index.php");
    exit();
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/loginstyle.css">
    <link rel="icon" type="image/x-icon" href="pictures/logo.png">
    <title>MST LOGIN</title>
</head>

<body>
    <header>
        <nav class="navbar">
            <div class="navbar-container">
                <div>
                    <a href="index.php"><img src="pictures/logo.png" alt="logo"></a>
                </div>
            </div>
        </nav>
    </header>

    <main>
        <div class="wrapper <?= $active_form === 'register' ? 'active' : '' ?>">
            <!-- LOGIN FORM -->
            <div class="form-box login">
                <h2>Login</h2>
                <form action="" method="post">
                    <?php if ($message && $active_form === 'login'): ?>
                        <p class="error-msg"><?= htmlspecialchars($message) ?></p>
                    <?php endif; ?>
                    <div class="input-box">
                        <input type="email" name="email" required>
                        <label>Email</label>
                    </div>
                    <div class="input-box">
                        <input type="password" name="password" required>
                        <label>Password</label>
                    </div>
                    <div class="remember-forgot">
                        <label><input type="checkbox" name="remember">Remember me</label>
                        <a href="#">Forgot password?</a>
                    </div>
                    <button type="submit" name="login" class="btnRL">Login</button>
                    <div class="login-register">
                        <p>Not a member? <a href="login.php?show=register" class="register-link">Register</a></p>
                    </div>
                </form>
            </div>

            <!-- REGISTER FORM -->
            <div class="form-box register">
                <h2>Registration</h2>
                <form action="" method="post">
                    <?php if ($message && $active_form === 'register'): ?>
                        <p class="error-msg"><?= htmlspecialchars($message) ?></p>
                    <?php endif; ?>
                    <div class="input-box">
                        <input type="text" name="name" required>
                        <label>Name</label>
                    </div>
                    <div class="input-box">
                        <input type="email" name="email" required>
                        <label>Email</label>
                    </div>
                    <div class="input-box">
                        <input type="password" name="password" required minlength="8">
                        <label>Password</label>
                    </div>
                    <input type="hidden" name="role" value="user">
                    <div class="remember-forgot">
                        <label><input type="checkbox" required>Agree to terms and conditions</label>
                    </div>
                    <button type="submit" name="register" class="btnRL">Register</button>
                    <div class="login-register">
                        <p>Already a member? <a href="login.php?show=login" class="login-link">Login</a></p>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <footer>
        <h1 class="footer-brand">MST PERFUMES</h1>
        <p>&copy; 2025 MST Perfumes. All rights reserved.</p>
    </footer>
<p style="text-align:center; ">
    <?php echo $message; ?>
</p>
    
</body>
</html> 