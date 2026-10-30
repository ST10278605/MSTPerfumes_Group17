<?php

declare(strict_types=1);
require_once __DIR__ . '/account_shared.php';
account_require_login();

if (strtolower((string) ($_SESSION['role'] ?? 'user')) === 'admin') {
    header('Location: admin_orders.php');
    exit;
}

$userId = (int) $_SESSION['user_id'];
account_header('My Orders', 'order.php');

if (!account_table_exists($conn, 'orders')) {
    ?>
    <div class="empty-state">
        <h3>Your orders will appear here</h3>
        <p>The orders table has not been created yet, or checkout has not been connected to save orders. See README.md for the expected schema.</p>
    </div>
    <?php
} elseif (!account_column_exists($conn, 'orders', 'user_id')) {
    account_flash('The orders table needs a user_id column to show each customer only their own orders.', 'error');
} else {
    $hasStatus = account_column_exists($conn, 'orders', 'status');
    $hasTotal = account_column_exists($conn, 'orders', 'total_amount');
    $hasCreated = account_column_exists($conn, 'orders', 'created_at');

    $sql = 'SELECT order_id'
        . ($hasStatus ? ',status' : '')
        . ($hasTotal ? ',total_amount' : '')
        . ($hasCreated ? ',created_at' : '')
        . ' FROM orders WHERE user_id=? ORDER BY order_id DESC';

    $stmt = $conn->prepare($sql);
    if ($stmt) {
        $stmt->bind_param('i', $userId);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result && $result->num_rows === 0) {
            ?>
            <div class="empty-state">
                <h3>No orders yet</h3>
                <p>When you place an order, you will be able to track it here.</p>
                <a class="btn" href="index.php">Explore perfumes</a>
            </div>
            <?php
        } else {
            ?>
            <section class="panel">
                <h2>Order history</h2>
                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th>Order number</th>
                                <?php if ($hasCreated): ?><th>Placed on</th><?php endif; ?>
                                <?php if ($hasTotal): ?><th>Total</th><?php endif; ?>
                                <?php if ($hasStatus): ?><th>Status</th><?php endif; ?>
                            </tr>
                        </thead>
                        <tbody>
                            <?php while ($o = $result->fetch_assoc()): ?>
                                <tr>
                                    <td>#<?= (int) $o['order_id'] ?></td>
                                    <?php if ($hasCreated): ?><td><?= account_e($o['created_at']) ?></td><?php endif; ?>
                                    <?php if ($hasTotal): ?><td>R<?= number_format((float) $o['total_amount'], 2) ?></td><?php endif; ?>
                                    <?php if ($hasStatus): ?><td><span class="badge"><?= account_e($o['status']) ?></span></td><?php endif; ?>
                                </tr>
                            <?php endwhile; ?>
                        </tbody>
                    </table>
                </div>
            </section>
            <?php
        }

        $stmt->close();
    }
}

account_footer();
