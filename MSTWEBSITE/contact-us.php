<?php
session_start();


// Already signed in? Go to the home page.
if (isset($_SESSION['user_id'])) {
    header('Location: index.php');
    exit();
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MST PERFUMES | Home</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
    <link rel="icon" type="image/x-icon" href="pictures/logo.svg">
</head>
<body>
<div class="top-bar">
    <span><i class="fas fa-star" aria-hidden="true"></i> Discover your next signature scent</span>
    <span>Free delivery on orders over R600</span>
</div>

<header class="site-header">
    <div class="site-header__inner">
        <a class="brand" href="index.php" aria-label="MST Perfumes home">
            <img src="pictures/logo.jpeg" alt="MST Perfumes logo">
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

            <a href="review">Reviews</a>
            <a href="contact-us">Contact Us</a>
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
                        <li><a href="usersprofile.php">My Profile</a></li>
                        <li><a href="order.php">My Orders</a></li>
                        <li><a href="review.php">My Reviews</a></li>
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

</body>
</html>