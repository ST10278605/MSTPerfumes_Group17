<?php
session_start();
error_reporting(E_ALL);
ini_set('display_errors', 1);
include "MSTDBConn.php";

$message = "";

// ADMIN LOGIN
if (isset($_POST["login"])) {
    $username = trim($_POST["username"]);
    $password = $_POST["password"];

    $stmt = $conn->prepare("SELECT * FROM users WHERE username = ? AND role = 'admin'");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $user = $result->fetch_assoc();

        if (password_verify($password, $user["password"])) {
            $_SESSION["user_id"] = $user["user_id"];
            $_SESSION["username"] = $user["username"];
            $_SESSION["name"] = $user["name"];
            $_SESSION["role"] = $user["role"];
           

            $redirect = isset($_POST["redirect"]) ? $_POST["redirect"] : "index.php";
            header("Location: " . $redirect);
            exit();

           

        } 
        else {
           echo $message = "Wrong password";
        }
    } else {
        $message = "User not found";
    }
    $stmt->close();
}

// LOGOUT
if (isset($_GET["logout"])) {
    session_destroy();
    header("Location: login.php");
    exit();
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <link rel="stylesheet" href="css/loginstyle.css">
    <title>MST ADMIN LOGIN </title>
</head>



<body>
    <header>
      
    </header>

    <main>
        <div class="center">
            <input type="checkbox" id="show">
            <label for="show" class="showbtn">Login</label>
            <div class="container">
                <label for="show" class="close-btn" title="close"></label>
                <div class="text">Login Form</div>
                <form action="#" method="post">
                    <div class="data">
                        <label for="email">Email or Phone</label>
                        <input type="text" id="email" name="username" required autocomplete="email">
                    </div>
                    <div class="data">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" required autocomplete="current-password">
                    </div>
                    <div class="forgot-pass">
                        <a href="#">Forgot Password?</a>
                    </div>
                    <div class="btn">
                        <div class="inner"></div>
                        <button type="submit" name="login">login</button>
                    </div>
                </form>
            </div>
        </div>
    </main>
    <footer>
        <h1 class="footer-brand">MST PERFUMES</h1>
        <p>&copy; 2025 MST Perfumes. All rights reserved.</p>
    </footer>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</body>

</html>