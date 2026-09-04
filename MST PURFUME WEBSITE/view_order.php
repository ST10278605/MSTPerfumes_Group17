<?php
session_start();
include 'MSTDBConn.php';

// Accept order_ref or order_id via GET or use last_order_* in session
$order_ref = $_GET['ref'] ?? $_SESSION['last_order_ref'] ?? null;
$order_id = isset($_GET['id']) ? (int)$_GET['id'] : ($_SESSION['last_order_id'] ?? null);

$order = null;
$items = [];

// inspect orders table columns to find a suitable reference or id column
$orderCols = [];
$r = $conn->query("SHOW COLUMNS FROM orders");
if ($r) {
    while ($row = $r->fetch_assoc()) {
        $orderCols[] = $row['Field'];
    }
    $r->free();
}

// find a ref-like column (order_ref, ref, reference)
$refCol = null;
$candidates = ['order_ref','orderReference','reference','ref','order_reference'];
foreach ($candidates as $c) {
    if (in_array($c, $orderCols)) { $refCol = $c; break; }
}
// fallback: any column containing 'ref'
if (!$refCol) {
    foreach ($orderCols as $c) {
        if (stripos($c, 'ref') !== false) { $refCol = $c; break; }
    }
}

// find id-like column
$idCol = null;
$idCandidates = ['id','order_id','orders_id','orderID','orderId'];
foreach ($idCandidates as $c) {
    if (in_array($c, $orderCols)) { $idCol = $c; break; }
}

// Try to load by reference if provided and column exists
if ($order_ref && $refCol) {
    $sql = "SELECT * FROM orders WHERE `$refCol` = ? LIMIT 1";
    $s = $conn->prepare($sql);
    if ($s) {
        $s->bind_param('s', $order_ref);
        $s->execute();
        $res = $s->get_result();
        $order = $res->fetch_assoc();
        $s->close();
        if ($order && !$order_id) {
            $order_id = $order[$idCol] ?? $order['id'] ?? $order['order_id'] ?? null;
        }
    }
}

// If not found yet, try by id parameter or session id and id column
if (!$order && $order_id && $idCol) {
    $sql = "SELECT * FROM orders WHERE `$idCol` = ? LIMIT 1";
    $s = $conn->prepare($sql);
    if ($s) {
        $s->bind_param('i', $order_id);
        $s->execute();
        $res = $s->get_result();
        $order = $res->fetch_assoc();
        $s->close();
    }
}

// final fallback: if logged in, fetch last order for this user
if (!$order && isset($_SESSION['user_id']) && $idCol) {
    $uid = $_SESSION['user_id'];
    if (in_array('user_id', $orderCols)) {
        $s = $conn->prepare("SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC LIMIT 1");
        if ($s) {
            $s->bind_param('i', $uid);
            $s->execute();
            $res = $s->get_result();
            $order = $res->fetch_assoc();
            $s->close();
            if ($order) $order_id = $order[$idCol] ?? $order['id'] ?? $order['order_id'] ?? null;
        }
    }
}

// if we have an order, fetch order items using detected id column
if ($order) {
    $oid = $order[$idCol] ?? $order['id'] ?? $order['order_id'] ?? null;
    if ($oid) {
        $it = $conn->prepare("SELECT * FROM order_items WHERE order_id = ?");
        if ($it) {
            $it->bind_param('i', $oid);
            $it->execute();
            $res = $it->get_result();
            while ($row = $res->fetch_assoc()) $items[] = $row;
            $it->close();
        }
    }
}
?>
<!doctype html>
<html>
<head><meta charset="utf-8"><title>Order Details</title>
<link rel="stylesheet" href="css/style.css">
</head>
<body>
    <main class="container">
        <h1>Order Details</h1>
        <?php if (!$order): ?>
            <p>No order found. Provide `?ref=ORDERREF` or `?id=ORDER_ID`, or place an order to see the confirmation.</p>
        <?php else: ?>
            <p><strong>Reference:</strong> <?=htmlspecialchars($order['order_ref'] ?? $order['ref'] ?? $order['reference'] ?? ($_SESSION['last_order_ref'] ?? ''))?></p>
            <p><strong>Order ID:</strong> <?=htmlspecialchars($order['id'] ?? $order['order_id'] ?? '')?></p>
            <p><strong>User:</strong> <?=htmlspecialchars($order['user_id'] ?? $order['customer_id'] ?? 'Guest')?></p>
            <p><strong>Total:</strong> R<?=number_format((float)($order['total'] ?? $order['amount'] ?? 0),2)?></p>
            <h2>Items</h2>
            <?php if (empty($items)): ?>
                <p>No items recorded for this order.</p>
            <?php else: ?>
                <table border="1" cellpadding="6">
                    <thead><tr><th>Product</th><th>SKU/ID</th><th>Qty</th><th>Price</th></tr></thead>
                    <tbody>
                    <?php foreach ($items as $it): ?>
                        <tr>
                            <td><?=htmlspecialchars($it['product_name'] ?? $it['name'] ?? '')?></td>
                            <td><?=htmlspecialchars($it['product_id'] ?? $it['sku'] ?? '')?></td>
                            <td><?=htmlspecialchars($it['qty'] ?? $it['quantity'] ?? '')?></td>
                            <td>R<?=number_format((float)($it['price'] ?? $it['unit_price'] ?? 0),2)?></td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            <?php endif; ?>
        <?php endif; ?>
    </main>
</body>
</html>