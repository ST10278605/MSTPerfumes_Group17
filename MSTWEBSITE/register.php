<?php
session_start();

require_once __DIR__ . '/MSTDBConn.php';

$message = '';

function loginGoogleUserSession($conn, $user) {
    $_SESSION['user_id'] = $user['user_id'];
    $_SESSION['email'] = $user['email'];
    $_SESSION['name'] = $user['name'];
    $_SESSION['role'] = $user['role'];
    $_SESSION['is_verified_seller'] = $user['is_verified_seller'] ?? 0;
    session_regenerate_id(true);
    header('Location: index.php');
    exit();
}

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['google_register']) && $_POST['google_register'] === '1') {
    $name = trim((string) ($_POST['name'] ?? ''));
    $email = trim((string) ($_POST['email'] ?? ''));

    if ($name === '' || $email === '') {
        $message = 'Google registration failed. Please try again.';
    } else {
        $checkStmt = $conn->prepare('SELECT * FROM users WHERE email = ?');
        $checkStmt->bind_param('s', $email);
        $checkStmt->execute();
        $checkResult = $checkStmt->get_result();

        if ($checkResult && $checkResult->num_rows > 0) {
            $user = $checkResult->fetch_assoc();
            loginGoogleUserSession($conn, $user);
        }

        $tempPassword = 'google_' . bin2hex(random_bytes(24));
        $hashedPassword = password_hash($tempPassword, PASSWORD_DEFAULT);
        $insert = $conn->prepare('INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, "user")');
        $insert->bind_param('sss', $name, $email, $hashedPassword);

        if ($insert->execute()) {
            $userId = $conn->insert_id;
            $newUserStmt = $conn->prepare('SELECT * FROM users WHERE user_id = ?');
            $newUserStmt->bind_param('i', $userId);
            $newUserStmt->execute();
            $newUserResult = $newUserStmt->get_result();
            $user = $newUserResult->fetch_assoc();
            loginGoogleUserSession($conn, $user);
        } else {
            $message = 'Unable to create your Google account. Please try again.';
        }
    }
}

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['register'])) {
    $name = trim((string) ($_POST['name'] ?? ''));
    $email = trim((string) ($_POST['email'] ?? ''));
    $password = $_POST['password'] ?? '';

    if ($name === '' || $email === '' || $password === '') {
        $message = 'Please complete all required fields.';
    } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $message = 'Please enter a valid email address.';
    } elseif (strlen($password) < 8) {
        $message = 'Password must be at least 8 characters.';
    } else {
        $role = 'user';

        $checkStmt = $conn->prepare('SELECT user_id FROM users WHERE email = ?');
        if ($checkStmt) {
            $checkStmt->bind_param('s', $email);
            $checkStmt->execute();
            $checkResult = $checkStmt->get_result();
            if ($checkResult && $checkResult->num_rows > 0) {
                $message = 'An account with this email already exists.';
            } else {
                $hashedPassword = password_hash($password, PASSWORD_DEFAULT);
                $stmt = $conn->prepare('INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)');
                if ($stmt) {
                    $stmt->bind_param('ssss', $name, $email, $hashedPassword, $role);
                    if ($stmt->execute()) {
                        $message = 'Registered successfully. You can login now.';
                    } else {
                        $message = 'Registration error: ' . $stmt->error;
                    }
                    $stmt->close();
                } else {
                    $message = 'Unable to create account. Please try again later.';
                }
            }
            $checkStmt->close();
        } else {
            $message = 'Registration check failed. Please try again later.';
        }
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>MST REGISTER</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="icon" type="image/x-icon" href="pictures/logo.svg">
    <script type="module">
        import { registerWithGoogle } from './js/form.js';

        window.registerWithGoogle = async function () {
            const result = await registerWithGoogle();

            if (result.success && result.user && result.user.email) {
                const form = document.getElementById('googleRegisterForm');
                form.name.value = result.user.displayName || 'Google User';
                form.email.value = result.user.email;
                form.google_register.value = '1';
                form.submit();
            } else {
                alert(result.error || 'Google registration failed.');
            }
        };
    </script>
</head>
<body>
<header>
    <nav class="navbar">
        <div class="navbar-container">
            <div class="navbar-logo">
                <a href="index.php"><img src="pictures/logo.png" alt="MST Perfumes Logo"></a>
            </div>
        </div>
    </nav>
</header>
<main>
    <div class="wrapper">
        <div class="form-box">
            <h2>Create account</h2>

            <?php if ($message !== ''): ?>
                <div class="message"><?= htmlspecialchars($message, ENT_QUOTES, 'UTF-8') ?></div>
            <?php endif; ?>

            <form action="register.php" method="post">
                <div class="input-box">
                    <input type="text" name="name" placeholder=" " required autocomplete="name">
                    <label>Name</label>
                </div>

                <div class="input-box">
                    <input type="email" name="email" placeholder=" " required autocomplete="email">
                    <label>Email</label>
                </div>

                <div class="input-box">
                    <input type="password" name="password" placeholder=" " required autocomplete="new-password">
                    <label>Password</label>
                </div>

                <button type="submit" name="register" class="btn">Register</button>

                <div class="divider"><span>or</span></div>

                <button type="button" class="btn" onclick="registerWithGoogle()">
                    Sign up with Google
                </button>

                <div class="login-register">
                    <p>Already registered? <a href="login.php">Login</a></p>
                </div>
            </form>

            <form id="googleRegisterForm" action="register.php" method="post" style="display:none;">
                <input type="hidden" name="google_register" value="0">
                <input type="hidden" name="name" value="">
                <input type="hidden" name="email" value="">
            </form>
        </div>
    </div>
</main>
<footer>
    <h1 class="footer-brand">MST PERFUMES</h1>
    <p>&copy; <?= date('Y') ?> MST Perfumes. All rights reserved.</p>
</footer>
</body>
</html>