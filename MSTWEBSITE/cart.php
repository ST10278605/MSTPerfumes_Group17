<?php
session_start();
include "MSTDBConn.php";

$isLoggedIn = isset($_SESSION["user_id"]);
$username = $isLoggedIn ? htmlspecialchars($_SESSION["name"] ?? $_SESSION["username"] ?? 'User') : null;

$cartCount = 0;
if (isset($_SESSION["cart"]) && is_array($_SESSION["cart"])) {
    $cartCount = array_sum($_SESSION["cart"]);
}

// products map (same ids as shopping.php)
$products = [
    'p1' => ['name' => 'Amen (60ml)', 'price' => 160, 'img' => 'pictures/product.JPEG'],
    'p2' => ['name' => 'Silver Sent Intense (50ml)', 'price' => 140, 'img' => 'pictures/product1.JPEG'],
    'p3' => ['name' => 'Hugo Boss (50ml)', 'price' => 140, 'img' => 'pictures/product2.JPEG'],
    'p4' => ['name' => 'Dunhill Desire Red (50ml)', 'price' => 140, 'img' => 'pictures/product3.JPEG'],
    'p5' => ['name' => 'Dunhill Desire Blue (50ml)', 'price' => 140, 'img' => 'pictures/product4.JPEG'],
    'p6' => ['name' => 'One Million (50ml)', 'price' => 140, 'img' => 'pictures/product5.JPEG'],
    'p7' => ['name' => 'Coco Mademoiselle (50ml)', 'price' => 180, 'img' => 'pictures/product6.JPEG'],
    'p8' => ['name' => 'Lady Million (50ml)', 'price' => 140, 'img' => 'pictures/product7.JPEG'],
    'p9' => ['name' => 'Scandal (50ml)', 'price' => 140, 'img' => 'pictures/product8.JPEG'],
    'p10' => ['name' => 'Red Door (50ml)', 'price' => 140, 'img' => 'pictures/product9.JPEG'],
    'p11' => ['name' => 'Far Away (50ml)', 'price' => 140, 'img' => 'pictures/product10.JPEG'],
    'p12' => ['name' => 'La vie est belle (50ml)', 'price' => 140, 'img' => 'pictures/product11.JPEG'],
];

// Actions: remove, update quantities, checkout
if (isset($_GET['remove'])) {
    $pid = $_GET['remove'];
    if (isset($_SESSION['cart'][$pid])) {
        unset($_SESSION['cart'][$pid]);
    }
    header('Location: cart.php');
    exit();
}

if (isset($_POST['update_qty'])) {
    foreach ($_POST['qty'] ?? [] as $pid => $q) {
        $q = (int)$q;
        if ($q <= 0) {
            unset($_SESSION['cart'][$pid]);
        } else {
            $_SESSION['cart'][$pid] = $q;
        }
    }
    header('Location: cart.php');
    exit();
}

$checkoutMessage = '';
if (isset($_POST['checkout'])) {
    if (!isset($_SESSION['user_id'])) {
        header('Location: login.php');
        exit();
    }
    $user_id = $_SESSION['user_id'];
    $cart = $_SESSION['cart'] ?? [];
    if (empty($cart)) {
        $checkoutMessage = 'Your cart is empty';
    } else {
        // calculate totals
        $subtotal = 0;
        foreach ($cart as $pid => $qty) {
            $price = $products[$pid]['price'] ?? 0;
            $subtotal += $price * $qty;
        }
        $shipping = 100; // simple flat rate
        $total = $subtotal + $shipping;

        // attempt to insert order and items - build INSERT based on actual orders table columns
        $order_ref = uniqid('ORD-');

        // desired columns and their bind types
        $desired = [
            'user_id' => ['type' => 'i', 'value' => $user_id],
            'order_ref' => ['type' => 's', 'value' => $order_ref],
            'total' => ['type' => 'd', 'value' => $total],
        ];

        // fetch existing columns
        $existingCols = [];
        $res = $conn->query("SHOW COLUMNS FROM orders");
        if ($res) {
            while ($row = $res->fetch_assoc()) {
                $existingCols[] = $row['Field'];
            }
            $res->free();
        }

        // build insert column list and bind data
        $insertCols = [];
        $placeholders = [];
        $bindTypes = '';
        $bindValues = [];
        foreach ($desired as $col => $meta) {
            if (in_array($col, $existingCols)) {
                $insertCols[] = $col;
                $placeholders[] = '?';
                $bindTypes .= $meta['type'];
                $bindValues[] = $meta['value'];
            }
        }

        // include created_at as NOW() if column exists
        $includeCreatedAtNow = in_array('created_at', $existingCols);
        if ($includeCreatedAtNow) {
            $insertCols[] = 'created_at';
            $placeholders[] = 'NOW()';
        }

        if (!empty($insertCols)) {
            $colsSql = implode(', ', $insertCols);
            $valsSql = implode(', ', $placeholders);
            // prepare statement
            $sql = "INSERT INTO orders ($colsSql) VALUES ($valsSql)";
            $stmt = $conn->prepare($sql);
            if ($stmt) {
                if (!empty($bindTypes)) {
                    // bind params dynamically
                    $refs = [];
                    foreach ($bindValues as $k => $v) {
                        $refs[$k] = &$bindValues[$k];
                    }
                    array_unshift($refs, $bindTypes);
                    call_user_func_array([$stmt, 'bind_param'], $refs);
                }
                if ($stmt->execute()) {
                    $order_id = $stmt->insert_id;
                    $stmt->close();
                } else {
                    $checkoutMessage = 'Order failed: ' . $conn->error;
                    $stmt->close();
                }
            } else {
                $checkoutMessage = 'Order failed: ' . $conn->error;
            }
        } else {
            $checkoutMessage = 'Order failed: orders table has no usable columns';
        }

        // if we have an order id, insert items
        if (empty($checkoutMessage) && isset($order_id)) {
            $itemStmt = $conn->prepare("INSERT INTO order_items (order_id, product_id, product_name, qty, price) VALUES (?, ?, ?, ?, ?)");
            if ($itemStmt) {
                foreach ($cart as $pid => $qty) {
                    $pname = $products[$pid]['name'] ?? $pid;
                    $price = $products[$pid]['price'] ?? 0;
                    $itemStmt->bind_param('issid', $order_id, $pid, $pname, $qty, $price);
                    $itemStmt->execute();
                }
                $itemStmt->close();
            }

            // attempt to decrement stock in products table (if present)
            $hasProductsTable = false;
            $r = $conn->query("SHOW TABLES LIKE 'products'");
            if ($r && $r->num_rows > 0) {
                $hasProductsTable = true;
            }
            if ($hasProductsTable) {
                $upd = $conn->prepare("UPDATE products SET stock = GREATEST(stock - ?, 0) WHERE sku = ? OR id = ?");
                if ($upd) {
                    foreach ($cart as $pid => $qty) {
                        // try by sku first (p1..p12), also allow numeric id
                        $pidInt = is_numeric($pid) ? (int)$pid : 0;
                        $upd->bind_param('isi', $qty, $pid, $pidInt);
                        $upd->execute();
                    }
                    $upd->close();
                }
            }

            // clear cart and set confirmation; save last order info in session
            unset($_SESSION['cart']);
            $_SESSION['last_order_ref'] = $order_ref;
            if (isset($order_id)) $_SESSION['last_order_id'] = $order_id;
            $checkoutMessage = "Order placed. Reference: $order_ref";
        }
        
    }
}

?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="css/style.css">
    <title>MST PERFUMES | CART</title>

</head>


<body>

<div class="top-bar">
    <span><i class="fas fa-star" aria-hidden="true"></i> Discover your next signature scent</span>
    <span>Free shipping on orders over R600</span>
</div>

<header class="site-header">
    <div class="site-header__inner">
        <a class="brand" href="index.php" aria-label="MST Perfumes home">
            <img src="pictures/logo.png" alt="MST Perfumes logo">
        </a>

        <nav class="site-nav" aria-label="Main navigation">
            <a href="index.php" class="<?= $filter === 'all' ? 'is-current' : '' ?>">Shop All</a>

            <div class="nav-dropdown">
                <button class="nav-dropdown__toggle" type="button" aria-expanded="false" aria-controls="shop-menu">
                    Shop by scent <i class="fas fa-chevron-down" aria-hidden="true"></i>
                </button>
                <ul class="nav-dropdown__menu" id="shop-menu">
                    <li><a href="index.php?filter=male">Men's Perfume</a></li>
                    <li><a href="index.php?filter=female">Women's Perfume</a></li>
                    <li><a href="index.php?filter=unisex">Unisex Perfume</a></li>
                </ul>
            </div>

            <a href="forum.php#reviews">Reviews</a>
            <a href="#contact-us">Contact Us</a>
        </nav>

        <div class="account">
            <?php if ($isLoggedIn): ?>
                <span class="account__name">Hi, <?= e($userName) ?></span>
            <?php else: ?>
                <a class="btn btn-ghost" href="login.php">Login</a>
            <?php endif; ?>

            <div class="nav-dropdown account-dropdown">
                <button class="nav-dropdown__toggle" type="button" aria-expanded="false" aria-controls="profile-menu">
                    <i class="fas fa-user-circle" aria-hidden="true"></i> Profile
                    <i class="fas fa-chevron-down" aria-hidden="true"></i>
                </button>
                <ul class="nav-dropdown__menu" id="profile-menu">
                    <?php if (!$isLoggedIn): ?>
                        <li><a href="login.php">Login</a></li>
                        <li><a href="register.php">Create an account</a></li>
                    <?php elseif ($isAdmin): ?>
                        <li><a href="profile.php">My Profile</a></li>
                        <li><a href="admin_dashboard.php">Admin Dashboard</a></li>
                        <li><a href="admin_products.php">Manage Products</a></li>
                        <li><a href="admin_orders.php">Manage Orders</a></li>
                        <li><a href="login.php?logout=1">Logout</a></li>
                    <?php else: ?>
                        <li><a href="profile.php">My Profile</a></li>
                        <li><a href="my_orders.php">My Orders</a></li>
                        <li><a href="forum.php#reviews">My Reviews</a></li>
                        <li><a href="login.php?logout=1">Logout</a></li>
                    <?php endif; ?>
                </ul>
            </div>

            <a href="cart.php" aria-label="Shopping cart, <?= $cartCount ?> items">
                <i class="fas fa-shopping-bag" aria-hidden="true"></i>
                <span class="cart-count"><?= $cartCount ?></span>
            </a>
        </div>
    </div>
</header>


            
    <main>
        <section id="cart-hero" class="page-hero">
            <div class="container">
                <h2>Your Cart</h2>
                <p>Review your items before checkout.</p>
            </div>
        </section>
        <section id="cart-content" class="page-content container">
            <div class="cart-grid">
                <div class="cart-items">
                    <?php if (!empty($checkoutMessage)): ?>
                        <div class="message"><?= htmlspecialchars($checkoutMessage) ?></div>
                    <?php endif; ?>

                    <?php if (!empty($_SESSION['cart'])): ?>
                    <form method="post">
                        <?php
                        $subtotal = 0;
                        foreach ($_SESSION['cart'] as $pid => $qty):
                            $p = $products[$pid] ?? null;
                            if (!$p) continue;
                            $lineTotal = $p['price'] * $qty;
                            $subtotal += $lineTotal;
                        ?>
                        <article class="cart-item">
                            <img src="<?= htmlspecialchars($p['img']) ?>" alt="<?= htmlspecialchars($p['name']) ?>">
                            <div class="item-details">
                                <h3><?= htmlspecialchars($p['name']) ?></h3>
                                <p class="item-price">R<?= number_format($p['price'], 2) ?> x <?= $qty ?> = R<?= number_format($lineTotal, 2) ?></p>
                                <div class="item-quantity">
                                    <input type="number" name="qty[<?= htmlspecialchars($pid) ?>]" value="<?= (int)$qty ?>" min="0">
                                </div>
                            </div>
                            <a class="remove-item" href="cart.php?remove=<?= urlencode($pid) ?>">Remove</a>
                        </article>
                        <?php endforeach; ?>

                        <div style="margin:20px 0;">
                            <button type="submit" name="update_qty" class="btn-primary">Update Quantities</button>
                        </div>
                    </form>
                    <?php else: ?>
                        <p>Your cart is empty.</p>
                    <?php endif; ?>
                </div>

                <div class="cart-summary">
                    <h3>Cart Summary</h3>
                    <?php if (!empty($_SESSION['cart'])): ?>
                        <?php $shipping = 100; $total = ($subtotal ?? 0) + $shipping; ?>
                        <p>Subtotal: R<?= number_format($subtotal, 2) ?></p>
                        <p>Shipping: R<?= number_format($shipping, 2) ?></p>
                        <p>Total: R<?= number_format($total, 2) ?></p>
                        <form method="post">
                            <button type="submit" name="checkout" class="btn-primary checkout">Checkout</button>
                        </form>
                    <?php else: ?>
                        <p>Subtotal: R0.00</p>
                        <p>Shipping: R0.00</p>
                        <p>Total: R0.00</p>
                    <?php endif; ?>
                </div>
            </div>
        </section>
    </main>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="js/Script1.js"></script>

    <footer>
        <h1 class="footer-brand">MST PERFUMES</h1>
        <p>&copy; 2025 MST Perfumes. All rights reserved.</p>
    </footer>

</body>
</html>