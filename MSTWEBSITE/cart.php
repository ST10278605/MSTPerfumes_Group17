<?php
session_start();
include "MSTDBConn.php";

function e(mixed $value): string
{
    return htmlspecialchars((string) $value, ENT_QUOTES, 'UTF-8');
}

function getUserCartId(mysqli $conn, int $userId): ?int
{
    $stmt = $conn->prepare('SELECT cart_id FROM carts WHERE user_id = ? LIMIT 1');
    if (!$stmt) {
        return null;
    }

    $stmt->bind_param('i', $userId);
    if (!$stmt->execute()) {
        $stmt->close();
        return null;
    }

    $result = $stmt->get_result();
    $row = $result ? $result->fetch_assoc() : null;
    $stmt->close();

    if ($row && isset($row['cart_id'])) {
        return (int) $row['cart_id'];
    }

    $insert = $conn->prepare('INSERT INTO carts (user_id) VALUES (?)');
    if (!$insert) {
        return null;
    }

    $insert->bind_param('i', $userId);
    if (!$insert->execute()) {
        $insert->close();
        return null;
    }

    $cartId = (int) $conn->insert_id;
    $insert->close();
    return $cartId;
}

function syncSessionCartToDatabase(mysqli $conn, int $userId): void
{
    if (!isset($_SESSION['cart']) || !is_array($_SESSION['cart'])) {
        $_SESSION['cart'] = [];
    }

    $cartId = getUserCartId($conn, $userId);
    if ($cartId === null) {
        return;
    }

    $deleteStmt = $conn->prepare('DELETE FROM cart_items WHERE cart_id = ?');
    if (!$deleteStmt) {
        return;
    }

    $deleteStmt->bind_param('i', $cartId);
    $deleteStmt->execute();
    $deleteStmt->close();

    if (empty($_SESSION['cart'])) {
        return;
    }

    $insertStmt = $conn->prepare('INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?)');
    if (!$insertStmt) {
        return;
    }

    foreach ($_SESSION['cart'] as $pid => $qty) {
        $productId = filter_var($pid, FILTER_VALIDATE_INT);
        if ($productId === false || $productId < 1) {
            continue;
        }

        $quantity = max(1, (int) $qty);
        $insertStmt->bind_param('iii', $cartId, $productId, $quantity);
        $insertStmt->execute();
    }

    $insertStmt->close();
}

$isLoggedIn = isset($_SESSION["user_id"]);
$userRole = strtolower((string) ($_SESSION['role'] ?? 'user'));
$isAdmin = $isLoggedIn && $userRole === 'admin';
$filter = $_GET['filter'] ?? 'all';
$userName = $isLoggedIn ? htmlspecialchars($_SESSION["name"] ?? $_SESSION["username"] ?? 'User') : null;

if (!isset($_SESSION['cart']) || !is_array($_SESSION['cart'])) {
    $_SESSION['cart'] = [];
}

if ($isLoggedIn) {
    $cartDb = [];
    $stmt = $conn->prepare('SELECT ci.product_id, ci.quantity FROM carts c INNER JOIN cart_items ci ON ci.cart_id = c.cart_id WHERE c.user_id = ?');
    if ($stmt) {
        $stmt->bind_param('i', $_SESSION['user_id']);
        if ($stmt->execute()) {
            $result = $stmt->get_result();
            while ($result && ($row = $result->fetch_assoc())) {
                $pid = (string) $row['product_id'];
                $cartDb[$pid] = (int) $row['quantity'];
            }
        }
        $stmt->close();
    }

    if (!empty($cartDb)) {
        $_SESSION['cart'] = $cartDb;
    }
}

$cartCount = array_sum($_SESSION['cart']);

function resolveProductImage(string $imagePath, string $fallback = 'pictures/perfume.jpeg'): string
{
    $imagePath = trim($imagePath);
    if ($imagePath === '') {
        return $fallback;
    }

    if (preg_match('#^https?://#i', $imagePath)) {
        return $imagePath;
    }

    $imagePath = preg_replace('/\.(jpe?g)$/i', '.jpeg', $imagePath);

    if (strpos($imagePath, 'pictures/') === 0) {
        if (file_exists(__DIR__ . '/' . $imagePath)) {
            return $imagePath;
        }

        $baseName = basename($imagePath);
        if (file_exists(__DIR__ . '/pictures/' . $baseName)) {
            return 'pictures/' . $baseName;
        }

        return $fallback;
    }

    if (file_exists(__DIR__ . '/pictures/' . $imagePath)) {
        return 'pictures/' . $imagePath;
    }

    if (file_exists(__DIR__ . '/' . $imagePath)) {
        return $imagePath;
    }

    $baseName = basename($imagePath);
    if ($baseName !== '' && file_exists(__DIR__ . '/pictures/' . $baseName)) {
        return 'pictures/' . $baseName;
    }

    return $fallback;
}

// products map. Prefer the real database product ids and keep the legacy p1..p12 map as fallback.
$products = [
    'p1' => ['name' => 'Amen (60ml)', 'price' => 160, 'img' => resolveProductImage('pictures/perfume.jpeg')],
    'p2' => ['name' => 'Silver Sent Intense (50ml)', 'price' => 140, 'img' => resolveProductImage('pictures/perfume1.jpeg')],
    'p3' => ['name' => 'Hugo Boss (50ml)', 'price' => 140, 'img' => resolveProductImage('pictures/perfume2.jpeg')],
    'p4' => ['name' => 'Dunhill Desire Red (50ml)', 'price' => 140, 'img' => resolveProductImage('pictures/perfume3.jpeg')],
    'p5' => ['name' => 'Dunhill Desire Blue (50ml)', 'price' => 140, 'img' => resolveProductImage('pictures/perfume4.jpeg')],
    'p6' => ['name' => 'One Million (50ml)', 'price' => 140, 'img' => resolveProductImage('pictures/perfume5.jpeg')],
    'p7' => ['name' => 'Coco Mademoiselle (50ml)', 'price' => 180, 'img' => resolveProductImage('pictures/perfume6.jpeg')],
    'p8' => ['name' => 'Lady Million (50ml)', 'price' => 140, 'img' => resolveProductImage('pictures/perfume7.jpeg')],
    'p9' => ['name' => 'Scandal (50ml)', 'price' => 140, 'img' => resolveProductImage('pictures/perfume8.jpeg')],
    'p10' => ['name' => 'Red Door (50ml)', 'price' => 140, 'img' => resolveProductImage('pictures/perfume9.jpeg')],
    'p11' => ['name' => 'Far Away (50ml)', 'price' => 140, 'img' => resolveProductImage('pictures/perfume10.jpeg')],
    'p12' => ['name' => 'La vie est belle (50ml)', 'price' => 140, 'img' => resolveProductImage('pictures/perfume11.jpeg')],
];

$productResult = $conn->query('SELECT product_id, name, price, image FROM products ORDER BY product_id');
if ($productResult) {
    while ($row = $productResult->fetch_assoc()) {
        $id = (string) $row['product_id'];
        $imageValue = (string) ($row['image'] ?? '');
        $resolvedImage = resolveProductImage($imageValue, 'pictures/perfume.jpeg');

        $products[$id] = [
            'name' => (string) ($row['name'] ?? $id),
            'price' => (float) ($row['price'] ?? 0),
            'img' => $resolvedImage,
        ];
        $products['p' . $id] = $products[$id];
    }
    $productResult->free();
}

// Actions: remove, update quantities, checkout
if (isset($_GET['remove'])) {
    $pid = (string) $_GET['remove'];
    $pidInt = filter_var($pid, FILTER_VALIDATE_INT);

    if (isset($_SESSION['cart'][$pid])) {
        unset($_SESSION['cart'][$pid]);
    }

    if ($pidInt !== false && isset($_SESSION['cart'][(string) $pidInt])) {
        unset($_SESSION['cart'][(string) $pidInt]);
    }

    if ($isLoggedIn) {
        syncSessionCartToDatabase($conn, (int) $_SESSION['user_id']);
    }

    header('Location: cart.php');
    exit();
}

if (isset($_POST['update_qty'])) {
    foreach ($_POST['qty'] ?? [] as $pid => $q) {
        $q = (int) $q;
        if ($q <= 0) {
            unset($_SESSION['cart'][$pid]);
        } else {
            $_SESSION['cart'][$pid] = $q;
        }
    }

    if ($isLoggedIn) {
        syncSessionCartToDatabase($conn, (int) $_SESSION['user_id']);
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
    <link rel="stylesheet" href="css/account.css">
    <title>MST PERFUMES | CART</title>
    <link rel="icon" type="image/x-icon" href="pictures/logo.svg">

</head>


<body>

<div class="top-bar">
    <span><i class="fas fa-star" aria-hidden="true"></i> Discover your next signature scent</span>
    <span>Free delivery on orders over R600</span>
</div>

<div class="account-topline">
    MST PERFUMES <span>•</span> A fragrance that feels like you
</div>

<header class="account-header">
    <a class="account-brand" href="index.php" aria-label="MST Perfumes home">
        <span class="brand-mark">M</span>
        <span>
            <strong>MST PERFUMES</strong>
            <small>THE ART OF FRAGRANCE</small>
        </span>
    </a>

    <nav class="account-nav" aria-label="Main navigation">
        <a href="index.php" class="active">Shop</a>
        <a href="index.php?filter=male">Men's Perfume</a>
        <a href="index.php?filter=female">Women's Perfume</a>
        <a href="index.php?filter=unisex">Unisex Perfume</a>
        <a href="review.php">Reviews</a>
        <a href="contact-us.php">Contact Us</a>
    </nav>

    <div class="account account-tools">
        <?php if ($isLoggedIn): ?>
            <span class="account__name">
                Hi, <?= e($userName) ?>
            </span>

            <div class="nav-dropdown account-dropdown">
                <button
                    class="nav-dropdown__toggle"
                    type="button"
                    aria-expanded="false"
                    aria-controls="profile-menu"
                >
                    <i class="fas fa-user-circle" aria-hidden="true"></i>
                    My Account
                    <i class="fas fa-chevron-down" aria-hidden="true"></i>
                </button>

                <ul class="nav-dropdown__menu" id="profile-menu">
                    <?php if (!$isLoggedIn): ?>
                        <li><a href="login.php">Login</a></li>
                        <li><a href="register.php">Create an account</a></li>
                    <?php elseif ($isAdmin): ?>
                        <li><a href="profile.php">My Profile</a></li>
                        <li><a href="admin_dashboard.php">Admin Dashboard</a></li>
                        <li><a href="login.php?logout=1">Logout</a></li>
                    <?php else: ?>
                        <li><a href="usersprofile.php">My Profile</a></li>
                        <li><a href="order.php">My Orders</a></li>
                        <li><a href="review.php">My Reviews</a></li>
                        <li><a href="login.php?logout=1">Logout</a></li>
                    <?php endif; ?>
                </ul>
            </div>
        <?php else: ?>
            <a class="btn btn-ghost" href="login.php">Login</a>
        <?php endif; ?>

        <a href="cart.php" aria-label="Shopping cart, <?= $cartCount ?> items">
            <i class="fas fa-shopping-bag" aria-hidden="true"></i>
            <span class="cart-count"><?= $cartCount ?></span>
        </a>
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
                        <article class="cart-item" data-price="<?= (float) $p['price'] ?>">
                            <img src="<?= htmlspecialchars($p['img']) ?>" alt="<?= htmlspecialchars($p['name']) ?>">
                            <div class="item-details">
                                <h3><?= htmlspecialchars($p['name']) ?></h3>
                                <p class="item-price">R<?= number_format($p['price'], 2) ?> x <span class="item-qty-label"><?= $qty ?></span> = R<span class="line-total"><?= number_format($lineTotal, 2) ?></span></p>
                                <div class="item-quantity">
                                    <button type="button" class="qty-btn" data-product-id="<?= e($pid) ?>" data-change="-1" aria-label="Decrease quantity">-</button>
                                    <input type="number" name="qty[<?= htmlspecialchars($pid) ?>]" value="<?= (int)$qty ?>" min="0" inputmode="numeric" aria-label="Quantity for <?= htmlspecialchars($p['name']) ?>" class="cart-qty-input">
                                    <button type="button" class="qty-btn" data-product-id="<?= e($pid) ?>" data-change="1" aria-label="Increase quantity">+</button>
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
                        <p>Subtotal: R<span id="cart-subtotal"><?= number_format($subtotal, 2) ?></span></p>
                        <p>Shipping: R<?= number_format($shipping, 2) ?></p>
                        <p>Total: R<span id="cart-total"><?= number_format($total, 2) ?></span></p>
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
    <script>
        document.addEventListener('click', function (event) {
            const button = event.target.closest('.qty-btn');
            if (!button) {
                return;
            }

            const parent = button.closest('.item-quantity');
            const input = parent ? parent.querySelector('input[type="number"]') : null;
            if (!input) {
                return;
            }

            const currentValue = parseInt(input.value, 10) || 0;
            const change = parseInt(button.dataset.change || '0', 10);
            const nextValue = Math.max(0, currentValue + change);
            input.value = nextValue;
            updateCartSummary();
        });

        document.addEventListener('input', function (event) {
            if (!event.target.classList.contains('cart-qty-input')) {
                return;
            }
            updateCartSummary();
        });

        function updateCartSummary() {
            const itemCards = document.querySelectorAll('.cart-item');
            let subtotal = 0;

            itemCards.forEach(function (card) {
                const price = parseFloat(card.dataset.price || '0');
                const qtyInput = card.querySelector('.cart-qty-input');
                const quantity = Math.max(0, parseInt(qtyInput ? qtyInput.value : '0', 10) || 0);
                const lineTotal = price * quantity;

                const label = card.querySelector('.item-qty-label');
                if (label) {
                    label.textContent = quantity;
                }

                const lineTotalEl = card.querySelector('.line-total');
                if (lineTotalEl) {
                    lineTotalEl.textContent = lineTotal.toFixed(2);
                }

                const itemPrice = card.querySelector('.item-price');
                if (itemPrice) {
                    itemPrice.innerHTML = 'R' + price.toFixed(2) + ' x <span class="item-qty-label">' + quantity + '</span> = R<span class="line-total">' + lineTotal.toFixed(2) + '</span>';
                }

                subtotal += lineTotal;
            });

            const shipping = <?= !empty($_SESSION['cart']) ? 100 : 0 ?>;
            const total = subtotal + shipping;

            const subtotalEl = document.getElementById('cart-subtotal');
            const totalEl = document.getElementById('cart-total');

            if (subtotalEl) subtotalEl.textContent = subtotal.toFixed(2);
            if (totalEl) totalEl.textContent = total.toFixed(2);
        }
    </script>

    <footer>
        <h1 class="footer-brand">MST PERFUMES</h1>
        <p>&copy; 2025 MST Perfumes. All rights reserved.</p>
    </footer>

</body>
</html>