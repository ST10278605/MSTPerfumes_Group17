<?php
declare(strict_types=1);
require_once __DIR__ . '/account_shared.php';
account_require_admin();
$userId = (int)$_SESSION['user_id'];
$message = '';
$error = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    account_check_csrf();
    $name = trim((string)($_POST['name'] ?? ''));
    $email = trim((string)($_POST['email'] ?? ''));
    $phone = trim((string)($_POST['phone_number'] ?? ''));
    $address = trim((string)($_POST['address'] ?? ''));
    $delivery = trim((string)($_POST['delivery_address'] ?? ''));
    if ($name === '' || !filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $error = 'Please enter your name and a valid email address.';
    } else {
        $stmt = $conn->prepare('UPDATE users SET name = ?, email = ?, phone_number = ?, address = ?, delivery_address = ? WHERE user_id = ?');
        if ($stmt) {
            $stmt->bind_param('sssssi', $name, $email, $phone, $address, $delivery, $userId);
            if ($stmt->execute()) {
                $_SESSION['name'] = $name; $_SESSION['email'] = $email; $message = 'Your profile has been updated.';
            } else $error = 'Could not update your profile. Check whether the email is already in use.';
            $stmt->close();
        } else $error = 'The users table does not match the expected columns.';
    }
}
$stmt = $conn->prepare('SELECT name,email,phone_number,address,delivery_address,role FROM users WHERE user_id = ? LIMIT 1');
$user = [];
if ($stmt) { $stmt->bind_param('i',$userId); $stmt->execute(); $user = $stmt->get_result()->fetch_assoc() ?: []; $stmt->close(); }
account_header('Administrator Profile','profile.php');
if ($message) account_flash($message);
if ($error) account_flash($error,'error');
if (!$user) { echo '<div class="empty-state">Your account record could not be found in the users table.</div>'; }
else { ?>
<section class="panel"><h2>Personal details</h2><p class="muted">Manage the contact information connected to your administrator account.</p>
<form method="post"><input type="hidden" name="csrf_token" value="<?= account_e(account_csrf_token()) ?>">
<div class="form-grid">
<div class="field"><label for="name">Full name</label><input id="name" name="name" required value="<?= account_e($user['name'] ?? '') ?>"></div>
<div class="field"><label for="email">Email address</label><input id="email" name="email" type="email" required value="<?= account_e($user['email'] ?? '') ?>"></div>
<div class="field"><label for="phone_number">Phone number</label><input id="phone_number" name="phone_number" value="<?= account_e($user['phone_number'] ?? '') ?>"></div>
<div class="field"><label for="address">Address</label><input id="address" name="address" value="<?= account_e($user['address'] ?? '') ?>"></div>
<div class="field full"><label for="delivery_address">Delivery address</label><textarea id="delivery_address" name="delivery_address"><?= account_e($user['delivery_address'] ?? '') ?></textarea></div>
</div><div class="form-actions"><button class="btn" type="submit">Save changes</button><a class="btn btn-outline" href="admin_dashboard.php">Back to dashboard</a></div></form></section>
<section class="panel"><h2>Account access</h2><p><span class="badge"><?= account_e($user['role'] ?? 'admin') ?></span></p><p class="small-note">For security, this page does not allow changing your role or password. Use a dedicated password-change workflow for password updates.</p></section>
<?php }
account_footer();
