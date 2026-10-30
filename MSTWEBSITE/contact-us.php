<?php
session_start();

function e(mixed $value): string
{
    return htmlspecialchars((string) $value, ENT_QUOTES, 'UTF-8');
}

$isLoggedIn = isset($_SESSION['user_id']);
$userId = $isLoggedIn ? (int) $_SESSION['user_id'] : 0;
$userName = $isLoggedIn
    ? (string) ($_SESSION['name'] ?? $_SESSION['email'] ?? 'My account')
    : '';
$userRole = strtolower((string) ($_SESSION['role'] ?? 'user'));
$isAdmin = $isLoggedIn && $userRole === 'admin';
$filter = 'all';
$cartCount = 0;
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MST PERFUMES | Contact Us</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/account.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
    <link rel="icon" type="image/x-icon" href="pictures/logo.jpeg">
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

<main class="contact-page">
    <section class="contact-card">
        <h1>Contact MST Perfumes</h1>
        <p class="lead">
            MST Perfumes is a fragrance brand inspired by elegance, confidence, and personal expression.
            We curate premium perfumes for men, women, and unisex collections designed to leave a lasting impression.
        </p>

        <div class="contact-grid">
            <div class="contact-info-box">
                <h2>Our Brand</h2>
                <p>
                    We believe the right fragrance should feel like your signature — refined, distinctive, and memorable.
                </p>
            </div>

            <div class="contact-info-box">
                <h2>Our Collections</h2>
                <p>
                    Explore luxury scents for every personality, from fresh daytime aromas to rich evening fragrances.
                </p>
            </div>

            <div class="contact-info-box">
                <h2>Customer Care</h2>
                <p>
                    Need help with an order, fragrance recommendation, or delivery question? Our team is ready to assist.
                </p>
            </div>
        </div>

        <div class="contact-details">
            <h2>Get in touch</h2>
            <p>
                <strong>Email:</strong> <a href="mailto:mohaochiloane539@gmail.com">mohaochiloane539@gmail.com</a>
            </p>
            <p>
                <strong>Phone:</strong> <a href="tel:+27715118509">+27 71 511 8509</a>
            </p>
            <p>
                <strong>Business Hours:</strong> Monday to Saturday, 9:00 AM – 5:00 PM
            </p>
            <p>
                <strong>Location:</strong> Pretoria, South Africa
            </p>
        </div>
    </section>

    <div class="contact-layout">
        <div class="contact-form-panel">
            <h2>Send us a message</h2>
            <form class="contact-form" action="mailto:mohaochiloane539@gmail.com" method="post" enctype="text/plain">
                <div class="contact-field">
                    <label for="name">Full name</label>
                    <input id="name" name="name" type="text" placeholder="Your name" required>
                </div>

                <div class="contact-field">
                    <label for="email">Email address</label>
                    <input id="email" name="email" type="email" placeholder="you@example.com" required>
                </div>

                <div class="contact-field">
                    <label for="subject">Subject</label>
                    <select id="subject" name="subject">
                        <option value="Order enquiry">Order enquiry</option>
                        <option value="Product question">Product question</option>
                        <option value="Delivery issue">Delivery issue</option>
                        <option value="General enquiry">General enquiry</option>
                    </select>
                </div>

                <div class="contact-field">
                    <label for="message">Message</label>
                    <textarea id="message" name="message" placeholder="Tell us how we can help..." required></textarea>
                </div>

                <button type="submit">Send message</button>
            </form>
        </div>

        <div>
            <div class="contact-story">
                <h2>Our story</h2>
                <p>
                    MST Perfumes was created for people who want their fragrance to say something meaningful.
                    From the first spritz to the final trail, each scent is chosen to reflect personality, confidence,
                    and everyday luxury.
                </p>
                <p>
                    <strong>Our promise:</strong> premium quality, memorable fragrances, and a shopping experience that feels personal.
                </p>
            </div>

            <div class="contact-map" style="margin-top: 20px;">
                <h2>Find us</h2>
                <p>Pretoria, South Africa</p>
                <iframe
                    src="https://www.google.com/maps?q=Pretoria%20South%20Africa&z=11&output=embed"
                    loading="lazy"
                    referrerpolicy="no-referrer-when-downgrade"
                    title="MST Perfumes location map">
                </iframe>
            </div>

            <div class="contact-social" style="margin-top: 20px;">
                <h2>Follow us</h2>
                <p>Stay connected with new launches, seasonal scents, and fragrance inspiration.</p>
                <div class="social-links">
                    <a href="https://www.instagram.com" target="_blank" rel="noreferrer"><i class="fab fa-instagram"></i> Instagram</a>
                    <a href="https://www.facebook.com" target="_blank" rel="noreferrer"><i class="fab fa-facebook-f"></i> Facebook</a>
                    <a href="https://www.tiktok.com" target="_blank" rel="noreferrer"><i class="fab fa-tiktok"></i> TikTok</a>
                </div>
            </div>
        </div>
    </div>
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
                <li><a href="review.php">Reviews</a></li>
                <li><a href="contact-us.php">Contact Us</a></li>
                <li><a href="cart.php">Shopping Cart</a></li>
            </ul>
        </div>
    </div>
</footer>
</body>
</html>