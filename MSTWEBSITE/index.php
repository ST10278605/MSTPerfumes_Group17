<?php
declare(strict_types=1);

session_start();

require_once __DIR__ . '/MSTDBConn.php';

if (!isset($conn) || !($conn instanceof mysqli)) {
    http_response_code(500);
    exit('Database connection is not available. Check MSTDBConn.php.');
}

$conn->set_charset('utf8mb4');

function e(mixed $value): string
{
    return htmlspecialchars((string) $value, ENT_QUOTES, 'UTF-8');
}


function tableExists(mysqli $conn, string $tableName): bool
{
    // Only allow a valid table name.
    $allowedTables = ['products', 'reviews', 'carts', 'cart_items'];

    if (!in_array($tableName, $allowedTables, true)) {
        return false;
    }

    // Check the database's list of tables safely.
    $sql = "SELECT COUNT(*) AS table_count
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
            AND table_name = ?";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("s", $tableName);
    $stmt->execute();

    $result = $stmt->get_result();
    $row = $result->fetch_assoc();

    $stmt->close();

    return (int) $row['table_count'] > 0;
}


$isLoggedIn = isset($_SESSION['user_id']);
$userId = $isLoggedIn ? (int) $_SESSION['user_id'] : 0;
$userName = $isLoggedIn
    ? (string) ($_SESSION['name'] ?? $_SESSION['email'] ?? 'My account')
    : '';
$userRole = strtolower((string) ($_SESSION['role'] ?? 'user'));
$isAdmin = $isLoggedIn && $userRole === 'admin';

if (empty($_SESSION['csrf_token'])) {
    $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
}

/*
 * Product categories in the database should ideally use:
 * Male, Female, or Unisex.
 * The helper below also accepts common older category labels.
 */
function normaliseCategory(string $category): string
{
    $category = strtolower(trim($category));

    if (in_array($category, ['male', 'men', "men's", 'mens', 'men perfume', "men's perfume"], true)) {
        return 'Male';
    }

    if (in_array($category, ['female', 'women', "women's", 'ladies', 'woman', 'women perfume', "women's perfume"], true)) {
        return 'Female';
    }

    if (in_array($category, ['unisex', 'all'], true)) {
        return 'Unisex';
    }

    return trim($category) !== '' ? trim($category) : 'Unisex';
}

$allowedFilters = [
    'all' => 'All Perfumes',
    'male' => "Men's Perfume",
    'female' => "Women's Perfume",
    'unisex' => 'Unisex Perfume',
];

$filter = strtolower(trim((string) ($_GET['filter'] ?? 'all')));
if (!array_key_exists($filter, $allowedFilters)) {
    $filter = 'all';
}

$sort = (string) ($_GET['sort'] ?? 'name');
$allowedSorts = ['name', 'price_low', 'price_high', 'newest'];
if (!in_array($sort, $allowedSorts, true)) {
    $sort = 'name';
}

$search = trim((string) ($_GET['search'] ?? ''));
$notice = '';
$error = '';

/* Add to cart: POST only, validate the product and stock, then redirect. */
if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['action'] ?? '') === 'add_to_cart') {
    $postedToken = (string) ($_POST['csrf_token'] ?? '');
    $productId = filter_input(INPUT_POST, 'product_id', FILTER_VALIDATE_INT);

    if (!hash_equals((string) $_SESSION['csrf_token'], $postedToken)) {
        $error = 'Your session token was not valid. Please refresh the page and try again.';
    } elseif (!$isLoggedIn) {
        header('Location: login.php?redirect=' . rawurlencode('index.php'));
        exit;
    } elseif (!$productId || $productId < 1) {
        $error = 'That product could not be found.';
    } else {
        $stmt = $conn->prepare('SELECT product_id, name, stock_quantity FROM products WHERE product_id = ? LIMIT 1');

        if (!$stmt) {
            $error = 'Unable to prepare the product lookup. Please try again.';
        } else {
            $stmt->bind_param('i', $productId);
            $stmt->execute();
            $productResult = $stmt->get_result();
            $product = $productResult ? $productResult->fetch_assoc() : null;
            $stmt->close();

            if (!$product) {
                $error = 'That product is no longer available.';
            } elseif ((int) $product['stock_quantity'] < 1) {
                $error = 'Sorry, this perfume is currently out of stock.';
            } else {
                // Create a cart for this customer if they do not already have one.
                $stmt = $conn->prepare('SELECT cart_id FROM carts WHERE user_id = ? LIMIT 1');

                if (!$stmt) {
                    $error = 'Unable to access your cart. Please check that the cart tables exist.';
                } else {
                    $stmt->bind_param('i', $userId);
                    $stmt->execute();
                    $cartResult = $stmt->get_result();
                    $cartRow = $cartResult ? $cartResult->fetch_assoc() : null;
                    $stmt->close();

                    if ($cartRow) {
                        $cartId = (int) $cartRow['cart_id'];
                    } else {
                        $stmt = $conn->prepare('INSERT INTO carts (user_id) VALUES (?)');
                        if (!$stmt) {
                            $error = 'Unable to create your cart.';
                            $cartId = 0;
                        } else {
                            $stmt->bind_param('i', $userId);
                            if ($stmt->execute()) {
                                $cartId = (int) $conn->insert_id;
                            } else {
                                $cartId = 0;
                                $error = 'Unable to create your cart. Please try again.';
                            }
                            $stmt->close();
                        }
                    }

                    if ($error === '' && $cartId > 0) {
                        $stmt = $conn->prepare('SELECT quantity FROM cart_items WHERE cart_id = ? AND product_id = ? LIMIT 1');

                        if (!$stmt) {
                            $error = 'Unable to check the quantity already in your cart.';
                        } else {
                            $stmt->bind_param('ii', $cartId, $productId);
                            $stmt->execute();
                            $itemResult = $stmt->get_result();
                            $itemRow = $itemResult ? $itemResult->fetch_assoc() : null;
                            $stmt->close();

                            $existingQuantity = $itemRow ? (int) $itemRow['quantity'] : 0;

                            if ($existingQuantity >= (int) $product['stock_quantity']) {
                                $error = 'You already have the maximum available stock of this perfume in your cart.';
                            } elseif ($itemRow) {
                                $newQuantity = min($existingQuantity + 1, (int) $product['stock_quantity']);
                                $stmt = $conn->prepare('UPDATE cart_items SET quantity = ? WHERE cart_id = ? AND product_id = ?');
                                if (!$stmt) {
                                    $error = 'Unable to update your cart.';
                                } else {
                                    $stmt->bind_param('iii', $newQuantity, $cartId, $productId);
                                    if (!$stmt->execute()) {
                                        $error = 'Unable to update your cart. Please try again.';
                                    }
                                    $stmt->close();
                                }
                            } else {
                                $quantity = 1;
                                $stmt = $conn->prepare('INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?)');
                                if (!$stmt) {
                                    $error = 'Unable to add this perfume to your cart.';
                                } else {
                                    $stmt->bind_param('iii', $cartId, $productId, $quantity);
                                    if (!$stmt->execute()) {
                                        $error = 'Unable to add this perfume to your cart. Please try again.';
                                    }
                                    $stmt->close();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if ($error === '') {
        $params = ['filter' => $filter, 'sort' => $sort];
        if ($search !== '') {
            $params['search'] = $search;
        }
        $params['added'] = '1';
        header('Location: index.php?' . http_build_query($params));
        exit;
    }
}

if (isset($_GET['added']) && $_GET['added'] === '1') {
    $notice = 'Perfume added to your cart.';
}

/* Get cart count from the database for logged-in customers. */
$cartCount = 0;
if ($isLoggedIn && tableExists($conn, 'carts') && tableExists($conn, 'cart_items')) {
    $stmt = $conn->prepare(
        'SELECT COALESCE(SUM(ci.quantity), 0) AS item_count
         FROM carts c
         LEFT JOIN cart_items ci ON ci.cart_id = c.cart_id
         WHERE c.user_id = ?'
    );

    if ($stmt) {
        $stmt->bind_param('i', $userId);
        $stmt->execute();
        $countResult = $stmt->get_result();
        if ($countResult && ($countRow = $countResult->fetch_assoc())) {
            $cartCount = (int) $countRow['item_count'];
        }
        $stmt->close();
    }
}

/* Load products using the actual products table columns. */
$products = [];
$productsTableExists = tableExists($conn, 'products');
$reviewsTableExists = tableExists($conn, 'reviews');

if ($productsTableExists) {
    $reviewSelect = $reviewsTableExists
        ? ', COALESCE(rv.review_count, 0) AS review_count, COALESCE(rv.average_rating, 0) AS average_rating'
        : ', 0 AS review_count, 0 AS average_rating';

    $reviewJoin = $reviewsTableExists
        ? ' LEFT JOIN (
                SELECT product_id, COUNT(*) AS review_count, AVG(rating) AS average_rating
                FROM reviews
                GROUP BY product_id
            ) rv ON rv.product_id = p.product_id'
        : '';

    $sql = 'SELECT p.product_id, p.name, p.description, p.price, p.image, p.category, p.stock_quantity'
        . $reviewSelect
        . ' FROM products p'
        . $reviewJoin
        . ' WHERE 1 = 1';

    $types = '';
    $values = [];

    if ($filter !== 'all') {
        if ($filter === 'male') {
            $sql .= " AND LOWER(TRIM(p.category)) IN ('male', 'men', 'men''s', 'mens', 'men perfume', 'men''s perfume')";
        } elseif ($filter === 'female') {
            $sql .= " AND LOWER(TRIM(p.category)) IN ('female', 'women', 'women''s', 'ladies', 'woman', 'women perfume', 'women''s perfume')";
        } elseif ($filter === 'unisex') {
            $sql .= " AND LOWER(TRIM(p.category)) = 'unisex'";
        }
    }

    if ($search !== '') {
        $sql .= ' AND (p.name LIKE ? OR p.description LIKE ? OR p.category LIKE ?)';
        $searchTerm = '%' . $search . '%';
        $types .= 'sss';
        array_push($values, $searchTerm, $searchTerm, $searchTerm);
    }

    switch ($sort) {
        case 'price_low':
            $sql .= ' ORDER BY p.price ASC, p.name ASC';
            break;
        case 'price_high':
            $sql .= ' ORDER BY p.price DESC, p.name ASC';
            break;
        case 'newest':
            $sql .= ' ORDER BY p.created_at DESC, p.product_id DESC';
            break;
        default:
            $sql .= ' ORDER BY p.name ASC';
            break;
    }

    $stmt = $conn->prepare($sql);
    if ($stmt) {
        if ($types !== '') {
            $stmt->bind_param($types, ...$values);
        }
        if ($stmt->execute()) {
            $result = $stmt->get_result();
            while ($result && ($row = $result->fetch_assoc())) {
                $row['category'] = normaliseCategory((string) ($row['category'] ?? 'Unisex'));
                $row['image'] = trim((string) ($row['image'] ?? ''));
                $products[] = $row;
            }
        } else {
            $error = 'We could not load the products. Please try again later.';
        }
        $stmt->close();
    } else {
        $error = 'We could not prepare the product list. Please check your products table structure.';
    }
}

$pageTitle = $allowedFilters[$filter] ?? 'All Perfumes';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MST PERFUMES | Home</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
    
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
    <section class="shop">
        <div class="container">
            <div class="shop__intro">
                <h1 class="shop__title"><?= e($pageTitle) ?></h1>
                <p class="shop__hint">Find a fragrance that feels like you. Explore the MST Perfumes collection.</p>

                <?php if ($notice !== ''): ?>
                    <p class="flash--success" role="status"><?= e($notice) ?></p>
                <?php endif; ?>
                <?php if ($error !== ''): ?>
                    <p class="flash--error" role="alert"><?= e($error) ?></p>
                <?php endif; ?>

                <div class="filter-row" aria-label="Filter perfumes">
                    <?php foreach ($allowedFilters as $filterKey => $filterLabel): ?>
                        <a class="filter-pill <?= $filter === $filterKey ? 'active' : '' ?>"
                           href="index.php?<?= e(http_build_query(array_filter(['filter' => $filterKey, 'sort' => $sort, 'search' => $search], static fn($v) => $v !== ''))) ?>">
                            <?= e($filterLabel) ?>
                        </a>
                    <?php endforeach; ?>
                </div>
            </div>

            <div class="shop-controls">
                <form class="search-form" method="get" action="index.php">
                    <input type="hidden" name="filter" value="<?= e($filter) ?>">
                    <input type="hidden" name="sort" value="<?= e($sort) ?>">
                    <label class="visually-hidden" for="product-search">Search perfumes</label>
                    <input id="product-search" type="search" name="search" value="<?= e($search) ?>" placeholder="Search perfumes">
                    <button class="btn btn-primary" type="submit"><i class="fas fa-search" aria-hidden="true"></i> Search</button>
                </form>

                <form method="get" action="index.php">
                    <input type="hidden" name="filter" value="<?= e($filter) ?>">
                    <?php if ($search !== ''): ?>
                        <input type="hidden" name="search" value="<?= e($search) ?>">
                    <?php endif; ?>
                    <label for="sort">Sort by</label>
                    <select id="sort" name="sort" onchange="this.form.submit()">
                        <option value="name" <?= $sort === 'name' ? 'selected' : '' ?>>Name</option>
                        <option value="price_low" <?= $sort === 'price_low' ? 'selected' : '' ?>>Price: Low to High</option>
                        <option value="price_high" <?= $sort === 'price_high' ? 'selected' : '' ?>>Price: High to Low</option>
                        <option value="newest" <?= $sort === 'newest' ? 'selected' : '' ?>>Newest</option>
                    </select>
                    <noscript><button class="btn btn-primary" type="submit">Apply</button></noscript>
                </form>
            </div>

            <?php if (!$productsTableExists): ?>
                <div class="empty-products">
                    <h2>Your product catalogue is not ready yet</h2>
                    <p>The database does not contain a <code>products</code> table. Create the table and add products before they can appear here.</p>
                </div>
            <?php elseif (count($products) === 0): ?>
                <div class="empty-products">
                    <h2>No perfumes found</h2>
                    <p>Try another search or category filter.</p>
                    <a class="btn btn-primary" href="index.php">Show all perfumes</a>
                </div>
            <?php else: ?>
                <ul class="grid product-grid">
                    <?php foreach ($products as $product): ?>
                        <?php
                            $productId = (int) $product['product_id'];
                            $productName = (string) $product['name'];
                            $imagePath = trim((string) ($product['image'] ?? ''));
                            // Normalize common JPEG extensions to lowercase `.jpeg`.
                            if ($imagePath !== '') {
                                $imagePath = preg_replace('/\.(jpe?g)$/i', '.jpeg', $imagePath);
                            }

                            // Determine a usable path: prefer values inside `pictures/`,
                            // accept bare filenames, and fall back to the default.
                            if ($imagePath === '') {
                                $imagePath = 'pictures/perfume.jpeg';
                            } else {
                                // If the image is referenced without the pictures/ prefix,
                                // check for its existence in the pictures folder.
                                if (strpos($imagePath, 'pictures/') !== 0) {
                                    if (file_exists(__DIR__ . '/pictures/' . $imagePath)) {
                                        $imagePath = 'pictures/' . $imagePath;
                                    } elseif (file_exists(__DIR__ . '/' . $imagePath)) {
                                        // keep as provided (relative to project root)
                                    } else {
                                        $basename = basename($imagePath);
                                        if (file_exists(__DIR__ . '/pictures/' . $basename)) {
                                            $imagePath = 'pictures/' . $basename;
                                        } else {
                                            $imagePath = 'pictures/perfume.jpeg';
                                        }
                                    }
                                } else {
                                    // If path starts with pictures/ ensure the file exists,
                                    // otherwise try a fallback inside the pictures folder.
                                    if (!file_exists(__DIR__ . '/' . $imagePath)) {
                                        $basename = basename($imagePath);
                                        if (file_exists(__DIR__ . '/pictures/' . $basename)) {
                                            $imagePath = 'pictures/' . $basename;
                                        } else {
                                            $imagePath = 'pictures/perfume.jpeg';
                                        }
                                    }
                                }
                            }
                            $stock = (int) ($product['stock_quantity'] ?? 0);
                            $reviewCount = (int) ($product['review_count'] ?? 0);
                            $averageRating = (float) ($product['average_rating'] ?? 0);
                        ?>
                        <li class="product-card">
                            <article>
                                <div class="tile__frame">
                                    <img src="<?= e($imagePath) ?>" alt="<?= e($productName) ?>" loading="lazy">
                                    <span class="tile__cue">View fragrance</span>
                                </div>
                                <div class="tile__caption">
                                    <span class="tile__name"><?= e($productName) ?></span>
                                    <span class="tile__price">R<?= number_format((float) $product['price'], 2) ?></span>
                                    <span class="tile__meta"><?= e($product['category']) ?></span>
                                    <?php if ($reviewCount > 0): ?>
                                        <span class="product-reviews">
                                            <?= number_format($averageRating, 1) ?>/5 ★ · <?= $reviewCount ?> <?= $reviewCount === 1 ? 'review' : 'reviews' ?>
                                        </span>
                                    <?php else: ?>
                                        <span class="product-reviews">No reviews yet</span>
                                    <?php endif; ?>
                                    <span class="product-stock <?= $stock < 1 ? 'out-of-stock' : '' ?>">
                                        <?= $stock > 0 ? 'In stock' : 'Out of stock' ?>
                                    </span>
                                </div>

                                <div class="product-actions">
                                    <?php if ($stock > 0): ?>
                                        <?php if ($isLoggedIn): ?>
                                            <form method="post" action="index.php?<?= e(http_build_query(array_filter(['filter' => $filter, 'sort' => $sort, 'search' => $search], static fn($v) => $v !== ''))) ?>">
                                                <input type="hidden" name="csrf_token" value="<?= e($_SESSION['csrf_token']) ?>">
                                                <input type="hidden" name="action" value="add_to_cart">
                                                <input type="hidden" name="product_id" value="<?= $productId ?>">
                                                <button class="btn btn-primary" type="submit">
                                                    Add to Cart <i class="fas fa-shopping-cart" aria-hidden="true"></i>
                                                </button>
                                            </form>
                                        <?php else: ?>
                                            <a class="btn btn-primary" href="login.php?redirect=index.php">Login to Add to Cart</a>
                                        <?php endif; ?>
                                    <?php else: ?>
                                        <button class="btn btn-primary" type="button" disabled>Out of Stock</button>
                                    <?php endif; ?>
                                </div>
                            </article>
                        </li>
                    <?php endforeach; ?>
                </ul>
            <?php endif; ?>
        </div>
    </section>

    <section class="contact-section" id="contact-us">
        <div class="container">
            <h2>Contact MST Perfumes</h2>
            <p>Have a question about a fragrance or your order? Get in touch with our team.</p>
            <p>Email: <a href="mailto:info@mstperfumes.co.za">info@mstperfumes.co.za</a></p>
            <p><small>Replace this example email address with your actual MST Perfumes contact email.</small></p>
        </div>
    </section>
</main>

<footer class="site-footer">
    <div class="site-footer__inner">
        <div>
            <h2 class="site-footer__brand">MST PERFUMES</h2>
            <p class="site-footer__fine">Find a fragrance that feels like you.</p>
            <p class="site-footer__fine">&copy; <?= date('Y') ?> MST Perfumes. All rights reserved.</p>
        </div>
        <div>
            <h3 class="site-footer__title">Shop</h3>
            <ul>
                <li><a href="index.php">All Perfumes</a></li>
                <li><a href="index.php?filter=male">Men's Perfume</a></li>
                <li><a href="index.php?filter=female">Women's Perfume</a></li>
                <li><a href="index.php?filter=unisex">Unisex Perfume</a></li>
            </ul>
        </div>
        <div>
            <h3 class="site-footer__title">Help</h3>
            <ul>
                <li><a href="forum.php#reviews">Reviews</a></li>
                <li><a href="#contact-us">Contact Us</a></li>
                <li><a href="cart.php">Shopping Cart</a></li>
            </ul>
        </div>
    </div>
</footer>

<script>
(() => {
    const dropdowns = document.querySelectorAll('.nav-dropdown');

    function closeDropdown(dropdown) {
        const button = dropdown.querySelector('.nav-dropdown__toggle');
        const menu = dropdown.querySelector('.nav-dropdown__menu');
        if (!button || !menu) return;
        button.setAttribute('aria-expanded', 'false');
        menu.classList.remove('is-open');
    }

    dropdowns.forEach((dropdown) => {
        const button = dropdown.querySelector('.nav-dropdown__toggle');
        const menu = dropdown.querySelector('.nav-dropdown__menu');
        if (!button || !menu) return;

        button.addEventListener('click', () => {
            const shouldOpen = button.getAttribute('aria-expanded') !== 'true';

            dropdowns.forEach(closeDropdown);

            if (shouldOpen) {
                button.setAttribute('aria-expanded', 'true');
                menu.classList.add('is-open');
            }
        });
    });

    document.addEventListener('click', (event) => {
        dropdowns.forEach((dropdown) => {
            if (!dropdown.contains(event.target)) closeDropdown(dropdown);
        });
    });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') dropdowns.forEach(closeDropdown);
    });
})();
</script>
</body>
</html>
