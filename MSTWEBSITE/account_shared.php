<?php
declare(strict_types=1);

/*
 * Shared helpers for MST Perfumes account pages.
 * Requires MSTDBConn.php to create a mysqli connection named $conn.
 */
if (session_status() !== PHP_SESSION_ACTIVE) {
    session_start();
}
require_once __DIR__ . '/MSTDBConn.php';

if (!isset($conn) || !($conn instanceof mysqli)) {
    http_response_code(500);
    exit('Database connection is not available. Please check MSTDBConn.php.');
}
$conn->set_charset('utf8mb4');

function account_e(mixed $value): string {
    return htmlspecialchars((string)($value ?? ''), ENT_QUOTES, 'UTF-8');
}
function account_table_exists(mysqli $conn, string $table): bool {
    $allowed = ['users','products','orders','order_items','reviews','carts','cart_items'];
    if (!in_array($table, $allowed, true)) return false;
    $stmt = $conn->prepare('SELECT COUNT(*) AS n FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?');
    if (!$stmt) return false;
    $stmt->bind_param('s', $table);
    $stmt->execute();
    $row = $stmt->get_result()->fetch_assoc();
    $stmt->close();
    return (int)($row['n'] ?? 0) > 0;
}
function account_require_login(): void {
    if (empty($_SESSION['user_id'])) {
        header('Location: login.php?redirect=' . rawurlencode(basename($_SERVER['PHP_SELF'] ?? 'index.php')));
        exit;
    }
}
function account_require_admin(): void {
    account_require_login();
    if (strtolower((string)($_SESSION['role'] ?? 'user')) !== 'admin') {
        http_response_code(403);
        exit('Access denied. This page is only available to administrators.');
    }
}
function account_csrf_token(): string {
    if (empty($_SESSION['csrf_token'])) $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
    return (string)$_SESSION['csrf_token'];
}
function account_check_csrf(): void {
    $posted = (string)($_POST['csrf_token'] ?? '');
    if (empty($_SESSION['csrf_token']) || !hash_equals((string)$_SESSION['csrf_token'], $posted)) {
        http_response_code(400);
        exit('Your security token has expired. Go back, refresh the page, and try again.');
    }
}
function account_header(string $title, string $active = ''): void {
    $name = isset($_SESSION['user_id'])
        ? ((string)($_SESSION['name'] ?? $_SESSION['email'] ?? 'My account'))
        : 'Guest';
    $admin = isset($_SESSION['user_id']) && strtolower((string)($_SESSION['role'] ?? 'user')) === 'admin';
    $links = $admin
        ? [['admin_dashboard.php','Dashboard'],['profile.php','My Profile'],['admin_products.php','Products'],['admin_orders.php','Orders']]
        : (isset($_SESSION['user_id'])
            ? [['usersprofile.php','My Profile'],['order.php','My Orders'],['review.php','My Reviews']]
            : [['index.php','Shop'],['review.php','Reviews'],['contact-us.php','Contact Us'],['login.php','Login']]
        );
    echo '<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">';
    echo '<title>'.account_e($title).' | MST Perfumes</title><link rel="stylesheet" href="css/account.css">';
    echo '<link rel="stylesheet" href="css/style.css"></head><body>';
    echo '<div class="account-topline">MST PERFUMES <span>•</span> A fragrance that feels like you</div>';
    echo '<header class="account-header"><a class="account-brand" href="index.php"><span class="brand-mark">M</span><span><strong>MST PERFUMES</strong><small>THE ART OF FRAGRANCE</small></span></a>';
    echo '<nav class="account-nav"><a href="index.php">Shop</a>';
    foreach ($links as $link) {
        echo '<a '.($active === $link[0] ? 'class="active" ' : '').'href="'.account_e($link[0]).'">'.account_e($link[1]).'</a>';
    }
    if (isset($_SESSION['user_id'])) {
        echo '<a href="login.php?logout=1">Log out</a>';
    }
    echo '</nav></header>';
    echo '<main class="account-main"><div class="account-welcome"><div><p class="eyebrow">'.($admin ? 'ADMINISTRATION' : (isset($_SESSION['user_id']) ? 'YOUR MST ACCOUNT' : 'MST PERFUMES')).'</p><h1>'.account_e($title).'</h1><p class="muted">'.(isset($_SESSION['user_id']) ? 'Welcome back, ' : 'Welcome, ').account_e($name).'.</p></div><a class="text-link" href="index.php">← Return to shop</a></div>';
}
function account_footer(): void {
    echo '</main><footer class="account-footer"><span>© '.date('Y').' MST Perfumes</span><span>Elegance in every note.</span></footer></body></html>';
}
function account_flash(string $message, string $type = 'success'): void {
    echo '<div class="alert alert-'.account_e($type).'" role="status">'.account_e($message).'</div>';
}
function account_column_exists(mysqli $conn, string $table, string $column): bool {
    $allowed = ['users','products','orders','order_items','reviews'];
    if (!in_array($table, $allowed, true)) return false;
    $stmt = $conn->prepare('SELECT COUNT(*) AS n FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?');
    if (!$stmt) return false;
    $stmt->bind_param('ss', $table, $column);
    $stmt->execute();
    $row = $stmt->get_result()->fetch_assoc();
    $stmt->close();
    return (int)($row['n'] ?? 0) > 0;
}
