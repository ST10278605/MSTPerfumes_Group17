<?php
$session_start_line = false;
session_start();

$isLoggedIn = isset($_SESSION["user_id"]);
$username = $isLoggedIn ? htmlspecialchars($_SESSION["name"] ?? $_SESSION["username"] ?? 'User') : null;

$cartCount = 0;
if (isset($_SESSION["cart"]) && is_array($_SESSION["cart"])) {
    $cartCount = array_sum($_SESSION["cart"]);
}

// attempt to load products from DB; fallback to embedded list
include_once 'MSTDBConn.php';
$products = [];
$hasProducts = false;
$res = $conn->query("SHOW TABLES LIKE 'products'");
if ($res && $res->num_rows > 0) {
    $hasProducts = true;
}
if ($hasProducts) {
    $q = $conn->query("SELECT * FROM products ORDER BY id ASC");
    if ($q) {
        while ($row = $q->fetch_assoc()) {
            $sku = $row['sku'] ?? $row['id'];
            $products[$sku] = [
                'name' => $row['name'],
                'price' => (float)$row['price'],
                'img' => $row['img'] ?? 'pictures/product.JPEG',
                'stock' => isset($row['stock']) ? (int)$row['stock'] : null,
                'id' => $row['id']
            ];
        }
    }
}
if (empty($products)) {
    // fallback hardcoded map
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
}

// Handle add-to-cart via GET parameter
if (isset($_GET['add'])) {
    $pid = $_GET['add'];
    if (isset($products[$pid])) {
        if (!isset($_SESSION['cart']) || !is_array($_SESSION['cart'])) {
            $_SESSION['cart'] = [];
        }
        if (!isset($_SESSION['cart'][$pid])) {
            $_SESSION['cart'][$pid] = 0;
        }
        $_SESSION['cart'][$pid]++;
        // redirect to avoid duplicate add on refresh
        header('Location: shopping.php');
        exit();
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MST PERFUMES | SHOPPING</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
</head>

<body>
    <header class="header">
        <nav class="navbar">
            <div class="navbar-container">
                <!-- Logo -->
                <div class="navbar-brand">
                    <a href="index.php">
                        <img src="pictures/logo.png" alt="MST Perfumes Logo">
                    </a>
                </div>

                <!-- Menu Items -->
                <ul class="navbar-items">
                    <li><a href="index.php">HOME</a></li>
                    <li><a href="shopping.php" class="active">Products</a></li>
                    <li><a href="forum.php">Forum</a></li>
                    <li><a href="cart.php">View Cart (<?= $cartCount ?>)</a></li>
                </ul>

                <!-- User Actions -->
                <div class="navbar-user">
                    <?php if ($isLoggedIn): ?>
                        <span>Welcome, <?= $username ?></span>
                        <a href="login.php?logout=1" class="btn-logout">Logout</a>
                    <?php else: ?>
                        <a href="login.php" class="btn-login">Login</a>
                    <?php endif; ?>
                </div>
            </div>
        </nav>
    </header>

    <main>

        <section id="men-products" class="page-content container">
            <h2>Men's Fragrances</h2>
            <div class="product-grid">
                <article class="product-card">
                    <img src="pictures/product.JPEG" alt="Amen perfume">
                    <h3>Amen (60ml)</h3>
                    <p class="bulk-price">R160 <span class="bulk-label">Price</span></p>
                    <p class="scent-profile">Male Fragrance</p>
                    <a href="shopping.php?add=p1" class="btn-primary add-to-cart">Add to Cart <i class="fas fa-shopping-cart"></i></a>
                </article>

                <article class="product-card">
                    <img src="pictures/product1.JPEG" alt="Silver Sent Intense perfume">
                    <h3>Silver Sent Intense (50ml)</h3>
                    <p class="bulk-price">R140 <span class="bulk-label">Price</span></p>
                    <p class="scent-profile">Male Fragrance</p>
                    <a href="shopping.php?add=p2" class="btn-primary add-to-cart">Add to Cart <i class="fas fa-shopping-cart"></i></a>
                </article>

                <article class="product-card">
                    <img src="pictures/product2.JPEG" alt="Hugo Boss perfume">
                    <h3>Hugo Boss (50ml)</h3>
                    <p class="bulk-price">R140 <span class="bulk-label">Price</span></p>
                    <p class="scent-profile">Male Fragrance</p>
                    <a href="shopping.php?add=p3" class="btn-primary add-to-cart">Add to Cart <i class="fas fa-shopping-cart"></i></a>
                </article>

                <article class="product-card">
                    <img src="pictures/product3.JPEG" alt="Dunhill Desire Red perfume">
                    <h3>Dunhill Desire Red (50ml)</h3>
                    <p class="bulk-price">R140 <span class="bulk-label">Price</span></p>
                    <p class="scent-profile">Male Fragrance</p>
                    <a href="shopping.php?add=p4" class="btn-primary add-to-cart">Add to Cart <i class="fas fa-shopping-cart"></i></a>
                </article>

                <article class="product-card">
                    <img src="pictures/product4.JPEG" alt="Dunhill Desire Blue perfume">
                    <h3>Dunhill Desire Blue (50ml)</h3>
                    <p class="bulk-price">R140 <span class="bulk-label">Price</span></p>
                    <p class="scent-profile">Male Fragrance</p>
                    <a href="shopping.php?add=p5" class="btn-primary add-to-cart">Add to Cart <i class="fas fa-shopping-cart"></i></a>
                </article>

                <article class="product-card">
                    <img src="pictures/product5.JPEG" alt="One Million perfume">
                    <h3>One Million (50ml)</h3>
                    <p class="bulk-price">R140 <span class="bulk-label">Price</span></p>
                    <p class="scent-profile">Male Fragrance</p>
                    <a href="shopping.php?add=p6" class="btn-primary add-to-cart">Add to Cart <i class="fas fa-shopping-cart"></i></a>
                </article>
            </div>
        </section>

        
        <section id="women-products" class="page-content container">
            <h2>Women's Fragrances</h2>
            <div class="product-grid">
                <article class="product-card">
                    <img src="pictures/product6.JPEG" alt="Coco Mademoiselle perfume">
                    <h3>Coco Mademoiselle (50ml)</h3>
                    <p class="bulk-price">R180 <span class="bulk-label">Price</span></p>
                    <p class="scent-profile">Female Fragrance</p>
                    <a href="shopping.php?add=p7" class="btn-primary add-to-cart">Add to Cart <i class="fas fa-shopping-cart"></i></a>
                </article>

                <article class="product-card">
                    <img src="pictures/product7.JPEG" alt="Lady Million perfume">
                    <h3>Lady Million (50ml)</h3>
                    <p class="bulk-price">R140 <span class="bulk-label">Price</span></p>
                    <p class="scent-profile">Female Fragrance</p>
                    <a href="shopping.php?add=p8" class="btn-primary add-to-cart">Add to Cart <i class="fas fa-shopping-cart"></i></a>
                </article>

                <article class="product-card">
                    <img src="pictures/product8.JPEG" alt="Scandal perfume">
                    <h3>Scandal (50ml)</h3>
                    <p class="bulk-price">R140 <span class="bulk-label">Price</span></p>
                    <p class="scent-profile">Female Fragrance</p>
                    <a href="shopping.php?add=p9" class="btn-primary add-to-cart">Add to Cart <i class="fas fa-shopping-cart"></i></a>
                </article>

                <article class="product-card">
                    <img src="pictures/product9.JPEG" alt="Red Door perfume">
                    <h3>Red Door (50ml)</h3>
                    <p class="bulk-price">R140 <span class="bulk-label">Price</span></p>
                    <p class="scent-profile">Female Fragrance</p>
                    <a href="shopping.php?add=p10" class="btn-primary add-to-cart">Add to Cart <i class="fas fa-shopping-cart"></i></a>
                </article>

                <article class="product-card">
                    <img src="pictures/product10.JPEG" alt="Far Away perfume">
                    <h3>Far Away (50ml)</h3>
                    <p class="bulk-price">R140 <span class="bulk-label">Price</span></p>
                    <p class="scent-profile">Female Fragrance</p>
                    <a href="shopping.php?add=p11" class="btn-primary add-to-cart">Add to Cart <i class="fas fa-shopping-cart"></i></a>
                </article>

                <article class="product-card">
                    <img src="pictures/product11.JPEG" alt="La vie est belle perfume">
                    <h3>La vie est belle (50ml)</h3>
                    <p class="bulk-price">R140 <span class="bulk-label">Price</span></p>
                    <p class="scent-profile">Female Fragrance</p>
                    <a href="shopping.php?add=p12" class="btn-primary add-to-cart">Add to Cart <i class="fas fa-shopping-cart"></i></a>
                </article>
            </div>
        </section>
    </main>

   <footer>
        <div class="footer-brand">
            <h1 >MST PERFUMES</h1>
            <p>&copy; 2025 MST Perfumes. All rights reserved.</p>
        </div>
        <div id="footer-social-icon">
            <i class="fa-brands fa-instagram"></i>
            <i class="fa-brands fa-twitter"></i>
            <i class="fa-brands fa-linkedin"></i>
            <i class="fa-brands fa-facebook"></i>
        </div>
        </div>
        <div class="footer-link">
            <h4>ABOUT US</h4>
            <div>
                <a href="#">Works</a>
                <a href="#">Stractragy</a>
                <a href="#">Release</a>
                <a href="#">Press</a>
                <a href="#">mission</a>
            </div>
        </div>
        <div class="footer-link">
            <h4>CUSTOMERS</h4>
            <div>
                <a href="#">Tranding</a>
                <a href="#">Popular</a>
                <a href="#">Customers</a>
                <a href="#">Features</a>
            </div>
        </div>
        <div class="footer-link">
            <h4>SUPPORT</h4>
            <div>
                <a href="#">Developer</a>
                <a href="#">Support</a>
                <a href="#">Customer Service</a>
                <a href="#">Get started</a>
                <a href="#">Guide</a>
            </div>
        </div>
    </footer>
    <script src="js/Script1.js"></script>
</body>
</html>