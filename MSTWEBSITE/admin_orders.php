<?php
declare(strict_types=1);
require_once __DIR__ . '/account_shared.php';
account_require_admin();

$message = '';
$error = '';

if (account_table_exists($conn, 'orders') && $_SERVER['REQUEST_METHOD'] === 'POST') {
    account_check_csrf();

    $orderId = filter_input(INPUT_POST, 'order_id', FILTER_VALIDATE_INT);
    $status = trim((string) ($_POST['status'] ?? ''));
    $allowed = ['Pending', 'Processing', 'Shipped', 'Completed', 'Cancelled'];

    if ($orderId && in_array($status, $allowed, true) && account_column_exists($conn, 'orders', 'status')) {
        $stmt = $conn->prepare('UPDATE orders SET status = ? WHERE order_id = ?');
        if ($stmt) {
            $stmt->bind_param('si', $status, $orderId);
            if ($stmt->execute()) {
                $message = 'Order status updated.';
            } else {
                $error = 'Could not update this order.';
            }
            $stmt->close();
        }
    } else {
        $error = 'Cannot update order status. Check the orders table columns and selected status.';
    }
}

account_header('Manage Orders', 'admin_orders.php');
if ($message) {
    account_flash($message);
}
if ($error) {
    account_flash($error, 'error');
}

if (!account_table_exists($conn, 'orders')) {
    ?>
    <div class="empty-state">
        <h3>Orders table not found</h3>
        <p>Create the orders table using the schema in README.md. This page will display orders after checkout starts saving them there.</p>
    </div>
    <?php
} else {
    $hasUser = account_column_exists($conn, 'orders', 'user_id');
    $hasStatus = account_column_exists($conn, 'orders', 'status');
    $hasTotal = account_column_exists($conn, 'orders', 'total_amount');
    $hasCreated = account_column_exists($conn, 'orders', 'created_at');

    $select = 'SELECT o.order_id'
        . ($hasUser ? ', o.user_id' : '')
        . ($hasStatus ? ', o.status' : '')
        . ($hasTotal ? ', o.total_amount' : '')
        . ($hasCreated ? ', o.created_at' : '')
        . ' FROM orders o ORDER BY o.order_id DESC';

    $result = $conn->query($select);

    if (!$result) {
        account_flash('Could not read orders. Check your orders table structure.', 'error');
    } elseif ($result->num_rows === 0) {
        ?>
        <div class="empty-state">
            <h3>No orders yet</h3>
            <p>Customer orders will appear here when your checkout process stores them in the database.</p>
        </div>
        <?php
    } else {
        ?>
        <section class="panel">
            <h2>Customer orders</h2>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Order</th>
                            <?php if ($hasUser): ?>
                                <th>Customer ID</th>
                            <?php endif; ?>
                            <?php if ($hasCreated): ?>
                                <th>Date</th>
                            <?php endif; ?>
                            <?php if ($hasTotal): ?>
                                <th>Total</th>
                            <?php endif; ?>
                            <?php if ($hasStatus): ?>
                                <th>Status</th>
                            <?php endif; ?>
                        </tr>
                    </thead>
                    <tbody>
                        <?php while ($o = $result->fetch_assoc()): ?>
                            <tr>
                                <td>#<?= (int) $o['order_id'] ?></td>
                                <?php if ($hasUser): ?>
                                    <td><?= (int) $o['user_id'] ?></td>
                                <?php endif; ?>
                                <?php if ($hasCreated): ?>
                                    <td><?= account_e($o['created_at']) ?></td>
                                <?php endif; ?>
                                <?php if ($hasTotal): ?>
                                    <td>R<?= number_format((float) $o['total_amount'], 2) ?></td>
                                <?php endif; ?>
                                <?php if ($hasStatus): ?>
                                    <td>
                                        <form class="inline-form" method="post">
                                            <input type="hidden" name="csrf_token" value="<?= account_e(account_csrf_token()) ?>">
                                            <input type="hidden" name="order_id" value="<?= (int) $o['order_id'] ?>">
                                            <select name="status">
                                                <?php foreach (['Pending', 'Processing', 'Shipped', 'Completed', 'Cancelled'] as $s): ?>
                                                    <option value="<?= account_e($s) ?>" <?= (($o['status'] ?? 'Pending') === $s ? 'selected' : '') ?>><?= account_e($s) ?></option>
                                                <?php endforeach; ?>
                                            </select>
                                            <button class="btn btn-small" type="submit">Update</button>
                                        </form>
                                    </td>
                                <?php endif; ?>
                            </tr>
                        <?php endwhile; ?>
                    </tbody>
                </table>
            </div>
        </section>
        <?php
    }
}

account_footer();
