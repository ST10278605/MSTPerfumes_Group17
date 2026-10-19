<?php
session_start();

require_once __DIR__ . '/MSTDBConn.php';

$message = '';

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
        // Only allow normal user role via the public register form
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
    <link rel="icon" type="image/x-icon" href="pictures/logo.png">
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
                    <input type="text" name="name" required autocomplete="name">
                    <label>Name</label>
                </div>

                <div class="input-box">
                    <input type="email" name="email" required autocomplete="email">
                    <label>Email</label>
                </div>

                <div class="input-box">
                    <input type="password" name="password" required autocomplete="new-password">
                    <label>Password</label>
                </div>

                <button type="submit" name="register" class="btn">Register</button>

                <div class="login-register">
                    <p>Already registered? <a href="login.php">Login</a></p>
                </div>
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
<?php
session_start();


// ---------------------------------------------------------------------------
// Log out: login.php?logout=1
// ---------------------------------------------------------------------------
if (isset($_GET['logout'])) {
    $_SESSION = [];
    if (ini_get('session.use_cookies')) {
        $params = session_get_cookie_params();
        setcookie(session_name(), '', time() - 42000, $params['path'], $params['domain'], $params['secure'], $params['httponly']);
    }
    session_destroy();
    header('Location: login.php');
    exit();
}

// Already signed in? Go to the home page.
if (isset($_SESSION['user_id'])) {
    header('Location: index.php');
    exit();
}

// Which form to show: login.php (log in) or login.php?show=register
$mode        = (($_GET['show'] ?? '') === 'register') ? 'register' : 'login';
$message     = '';
$messageType = 'error';
$old         = ['name' => '', 'username' => ''];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    require_once __DIR__ . '/MSTDBConn.php'; // gives us $conn

    $action = $_POST['action'] ?? '';

    // -----------------------------------------------------------------------
    // Register
    // -----------------------------------------------------------------------
    if ($action === 'register') {
        $mode     = 'register';
        $name     = trim($_POST['name'] ?? '');
        $username = strtolower(trim($_POST['username'] ?? ''));
        $password = $_POST['password'] ?? '';
        $old      = ['name' => $name, 'username' => $username];

        if ($name === '' || $username === '' || $password === '') {
            $message = 'Please fill in every field.';
        } elseif (mb_strlen($name) > 100) {
            $message = 'Your name must be 100 characters or fewer.';
        } elseif (!preg_match('/^[a-z0-9_.]{3,30}$/', $username)) {
            $message = 'Username must be 3 to 30 characters: letters, numbers, dots or underscores.';
        } elseif (strlen($password) < 8) {
            $message = 'Password must be at least 8 characters.';
        } else {
            try {
                $hash = password_hash($password, PASSWORD_DEFAULT);

                $conn->begin_transaction();

                $stmt = $conn->prepare('INSERT INTO users (name, username, password) VALUES (?, ?, ?)');
                $stmt->bind_param('sss', $name, $username, $hash);
                $stmt->execute();
                $newId = $conn->insert_id;
                $stmt->close();

                // user_id is the key the cart, orders and other tables point at.
                $stmt = $conn->prepare('UPDATE users SET user_id = id WHERE id = ?');
                $stmt->bind_param('i', $newId);
                $stmt->execute();
                $stmt->close();

                $conn->commit();

                $mode        = 'login';
                $messageType = 'success';
                $message     = 'Account created. Log in below.';
                $old         = ['name' => '', 'username' => $username];
            } catch (mysqli_sql_exception $ex) {
                try {
                    $conn->rollback();
                } catch (Throwable $ignored) {
                }
                if ((int) $ex->getCode() === 1062) {
                    $message = 'That username is taken. Try another one.';
                } else {
                    error_log('MST register failed: ' . $ex->getMessage());
                    $message = 'We could not create your account. Please try again.';
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Log in
    // -----------------------------------------------------------------------
    if ($action === 'login') {
        $mode     = 'login';
        $username = strtolower(trim($_POST['username'] ?? ''));
        $password = $_POST['password'] ?? '';
        $old      = ['name' => '', 'username' => $username];

        if ($username === '' || $password === '') {
            $message = 'Enter your username and password.';
        } else {
            try {
                $stmt = $conn->prepare('SELECT id, user_id, name, username, password, role FROM users WHERE username = ? LIMIT 1');
                $stmt->bind_param('s', $username);
                $stmt->execute();
                $user = $stmt->get_result()->fetch_assoc();
                $stmt->close();

                if ($user && password_verify($password, $user['password'])) {
                    session_regenerate_id(true);
                    $_SESSION['user_id']  = (int) ($user['user_id'] ?? $user['id']);
                    $_SESSION['name']     = $user['name'];
                    $_SESSION['username'] = $user['username'];
                    $_SESSION['role']     = $user['role'];
                    header('Location: index.php');
                    exit();
                }

                // Same message for "no such user" and "wrong password".
                $message = 'Incorrect username or password.';
            } catch (mysqli_sql_exception $ex) {
                error_log('MST login failed: ' . $ex->getMessage());
                $message = 'We could not log you in right now. Please try again.';
            }
        }
    }
}

$pageTitle   = $mode === 'register' ? 'Create account' : 'Log in';
$activePage  = '';
$extraCss    = ['css/loginstyle.css'];
$hideAccount = true;
include __DIR__ . '/partials/header.php';
?>

    <div class="auth">
        <div class="auth-card">
<?php if ($mode === 'register'): ?>
            <h1>Create your account</h1>
<?php else: ?>
            <h1>Log in</h1>
<?php endif; ?>

<?php if ($message !== ''): ?>
            <p class="<?= $messageType === 'success' ? 'success-msg' : 'error-msg' ?>" role="alert"><?= e($message) ?></p>
<?php endif; ?>

<?php if ($mode === 'register'): ?>
            <form method="post" action="login.php?show=register">
                <input type="hidden" name="action" value="register">

                <div class="field">
                    <label for="name">Name</label>
                    <input class="input" type="text" id="name" name="name" value="<?= e($old['name']) ?>" maxlength="100" autocomplete="name" required>
                </div>
                <div class="field">
                    <label for="username">Username</label>
                    <input class="input" type="text" id="username" name="username" value="<?= e($old['username']) ?>" minlength="3" maxlength="30" pattern="[A-Za-z0-9_.]{3,30}" autocomplete="username" required>
                    <p class="hint">3 to 30 letters, numbers, dots or underscores.</p>
                </div>
                <div class="field">
                    <label for="password">Password</label>
                    <input class="input" type="password" id="password" name="password" minlength="8" autocomplete="new-password" required>
                    <p class="hint">At least 8 characters.</p>
                </div>

                <button type="submit" class="btn-primary auth-submit">Create account</button>
            </form>
            <p class="auth-switch">Already have an account? <a href="login.php">Log in</a></p>
<?php else: ?>
            <form method="post" action="login.php">
                <input type="hidden" name="action" value="login">

                <div class="field">
                    <label for="username">Username</label>
                    <input class="input" type="text" id="username" name="username" value="<?= e($old['username']) ?>" autocomplete="username" required>
                </div>
                <div class="field">
                    <label for="password">Password</label>
                    <input class="input" type="password" id="password" name="password" autocomplete="current-password" required>
                </div>

                <button type="submit" class="btn-primary auth-submit">Log in</button>
            </form>
            <p class="auth-switch">New to MST Perfumes? <a href="login.php?show=register">Create an account</a></p>
<?php endif; ?>
        </div>
    </div>

<?php include __DIR__ . '/partials/footer.php'; ?>
