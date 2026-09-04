<?php
session_start();

$isLoggedIn = isset($_SESSION["user_id"]);
$username = $isLoggedIn ? htmlspecialchars($_SESSION["name"]) : null;

$cartCount = 0;
if (isset($_SESSION["cart"]) && is_array($_SESSION["cart"])) {
    $cartCount = array_sum($_SESSION["cart"]);
}
// DB: handle reviews
include "MSTDBConn.php";

$reviewMessage = '';
if (isset($_POST['add_review'])) {
    $rname = trim($_POST['rname'] ?? '');
    $rating = (int)($_POST['rating'] ?? 5);
    $review = trim($_POST['review'] ?? '');
    if ($rname === '' || $review === '') {
        $reviewMessage = 'Name and review are required.';
    } else {
        // detect available columns and map accordingly
        $colsRes = $conn->query("SHOW COLUMNS FROM reviews");
        $cols = [];
        if ($colsRes) {
            while ($c = $colsRes->fetch_assoc()) $cols[] = $c['Field'];
        }

        $nameCol = null; foreach (['name','rname','author','user_name','username'] as $c) if (in_array($c, $cols)) { $nameCol = $c; break; }
        $textCol = null; foreach (['review','body','comment','text'] as $c) if (in_array($c, $cols)) { $textCol = $c; break; }
        $ratingCol = null; foreach (['rating','stars'] as $c) if (in_array($c, $cols)) { $ratingCol = $c; break; }
        $userIdCol = in_array('user_id', $cols) ? 'user_id' : null;

        if (!$textCol || !$nameCol) {
            $reviewMessage = 'Database table `reviews` does not have expected columns. Please run db_setup.sql or contact the admin.';
        } else {
            // If reviews table requires user_id, require login
            if ($userIdCol && !isset($_SESSION['user_id'])) {
                $reviewMessage = 'You must be logged in to post a review.';
            } else {
                $insertCols = [];
                $placeholders = [];
                $types = '';
                $values = [];
                // name (if logged in prefer session name)
                $useName = isset($_SESSION['name']) ? $_SESSION['name'] : $rname;
                $insertCols[] = $nameCol; $placeholders[] = '?'; $types .= 's'; $values[] = $useName;
                // user_id if present
                if ($userIdCol) { $insertCols[] = $userIdCol; $placeholders[] = '?'; $types .= 'i'; $values[] = (int)$_SESSION['user_id']; }
                // rating if present
                if ($ratingCol) { $insertCols[] = $ratingCol; $placeholders[] = '?'; $types .= 'i'; $values[] = $rating; }
                // review/text
                $insertCols[] = $textCol; $placeholders[] = '?'; $types .= 's'; $values[] = $review;

                $sql = 'INSERT INTO reviews (' . implode(', ', $insertCols) . ') VALUES (' . implode(', ', $placeholders) . ')';
                $stmt = $conn->prepare($sql);
                if ($stmt) {
                    try {
                        $stmt->bind_param($types, ...$values);
                        $stmt->execute();
                        $stmt->close();
                        $reviewMessage = 'Thanks — your review was added.';
                    } catch (mysqli_sql_exception $e) {
                        if ($e->getCode() == 1452) {
                            $reviewMessage = 'Unable to post review: your account is not recognized by the system. Please login or contact support.';
                        } else {
                            $reviewMessage = 'Failed to save review: ' . $e->getMessage();
                        }
                    }
                } else {
                    $reviewMessage = 'Failed to save review (prepare failed).';
                }
            }
        }
    }
}

// fetch recent reviews
$reviews = [];
$colsRes = $conn->query("SHOW COLUMNS FROM reviews");
if ($colsRes) {
    $cols = [];
    while ($c = $colsRes->fetch_assoc()) $cols[] = $c['Field'];

    $nameCol = null; foreach (['name','rname','author','user_name','username'] as $c) if (in_array($c, $cols)) { $nameCol = $c; break; }
    $textCol = null; foreach (['review','body','comment','text'] as $c) if (in_array($c, $cols)) { $textCol = $c; break; }
    $ratingCol = null; foreach (['rating','stars'] as $c) if (in_array($c, $cols)) { $ratingCol = $c; break; }
    $dateCol = null; foreach (['created_at','created','date'] as $c) if (in_array($c, $cols)) { $dateCol = $c; break; }

    $select = ['id'];
    $select[] = $nameCol ? "$nameCol AS name" : "NULL AS name";
    $select[] = $ratingCol ? "$ratingCol AS rating" : "5 AS rating";
    $select[] = $textCol ? "$textCol AS review" : "'' AS review";
    $select[] = $dateCol ? "$dateCol AS created_at" : "NOW() AS created_at";

    $orderBy = $dateCol ? $dateCol : 'created_at';
    $sql = 'SELECT ' . implode(', ', $select) . ' FROM reviews ORDER BY ' . $orderBy . ' DESC LIMIT 8';
    $rres = $conn->query($sql);
    if ($rres) {
        while ($row = $rres->fetch_assoc()) $reviews[] = $row;
    }
} else {
    $reviews = [];
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/testimone.css">
    <link rel="icon" type="image/x-icon" href="pictures/logo.png">
    <title>MST PERFUMES | HOMEPAGE</title>
</head>
<body>

<header class="header">
    <nav class="navbar">
        <div class="navbar-container">
            <!-- Logo -->
            <div class="navbar-brand">
                <a href="index.php">
                    <img src="pictures/logo.png" alt="MST Logo">
                </a>
            </div>

            
            <ul class="navbar-items">
                <li><a href="index.php">HOME</a></li>
                <li><a href="shopping.php">Products</a></li>
                <li><a href="forum.php">Forum</a></li>
                <li><a href="cart.php">View Cart (<?= $cartCount ?>)</a></li>
            </ul>

            
            <div class="navbar-user">
                <?php if ($isLoggedIn): ?>
                    <span>Welcome, <?= htmlspecialchars($username) ?></span>
                    <a href="login.php?logout=1" class="btn-logout">Logout</a>
                <?php else: ?>
                    <a href="login.php" class="btn-login">Login</a>
                <?php endif; ?>
            </div>
        </div>
    </nav>
</header>
        
    <section class="section" id="home-page">
        <div class="card-wrapper">
            <div class="card-top">
                <div class="name">
                    <h2>welcome to MST perfumes of the future</h2>
                    <p>Where anything becomes possible with perfume.</p>
                </div>
            </div>
            <div class="card-bottom">
                <span class="top-text">Login to be a Member</span><br>
                <span class="bottom-text">Join our MST Perfume page to purchase and live a luxury life, We also offer sponsorship</span> <br>
                <a href="login.php" class="button">Join Us</a>
            </div>
        </div>
    </section>
    <section id="products">
        <h2>Testimonials</h2>
    </section>

    <!-- Reviews list and add form -->
    <section id="reviews" class="page-content container">
        <h2>User Reviews</h2>
        <?php if ($reviewMessage): ?>
            <p class="error-msg"><?= htmlspecialchars($reviewMessage) ?></p>
        <?php endif; ?>

        <div class="review-grid">
            <?php if (!empty($reviews)): ?>
                <?php foreach ($reviews as $r): ?>
                    <article class="product-card">
                        <h3><?= htmlspecialchars($r['name']) ?> <small style="float:right;">Rating: <?= (int)$r['rating'] ?>/5</small></h3>
                        <p><?= nl2br(htmlspecialchars($r['review'])) ?></p>
                        <small><?= htmlspecialchars($r['created_at']) ?></small>
                    </article>
                <?php endforeach; ?>
            <?php else: ?>
                <p>No reviews yet — be the first to leave one.</p>
            <?php endif; ?>
        </div>

        <div style="margin-top:20px;">
            <h3>Leave a Review</h3>
            <form method="post">
                <input type="text" name="rname" placeholder="Your name" required style="width:100%;padding:8px;margin:6px 0;">
                <label>Rating</label>
                <select name="rating" style="margin:6px 0;">
                    <option value="5">5</option>
                    <option value="4">4</option>
                    <option value="3">3</option>
                    <option value="2">2</option>
                    <option value="1">1</option>
                </select>
                <textarea name="review" placeholder="Your review" required style="width:100%;height:100px;margin:6px 0;padding:8px;"></textarea>
                <button type="submit" name="add_review" class="btnRL">Submit Review</button>
            </form>
        </div>
    </section>
    <section id="community">
        <h2>Community Knowledge Hub</h2>
    </section>
    <section class="awesome-feature">
        <div id="feature-text">
            <h1>Contact US</h1>
            <p>MST PERFUMES of the future<br> for the elit. </p>
        </div>
    </section>
    <section id="contact-us">
        <div>
            <div id="contact-form-item">
                <div>
                    <label for="name">Name</label>
                    <br>
                    <input type="text" id="name" placeholder="Your Name">
                    <br>
                </div>
                <div>
                    <label for="email">Email</label>
                    <br>
                    <input type="email" id="email" placeholder="Your Email">
                    <br>
                </div>
            </div>
            <label for="message">Messages</label>
            <br>
            <textarea id="message" placeholder="Details your message"></textarea>
            <span id="send-message-btn">
                <input type="submit" value="Send Message">
                <i class="fa-solid fa-rocket"></i>
            </span>
        </div>
        <div>
            <div class="contact-info">
                <i class="fa-solid fa-envelope"></i> &#58; <p>mohaochiloane539@gmail.com</p>
            </div>
            <div class="contact-info">
                <i class="fa-solid fa-link"></i> &#58; <p>www.MSTPERFUMES.com</p>
            </div>
            <div class="contact-info">
                <i class="fa-solid fa-phone"></i> &#58; <p>(+27) 0715118509</p>
            </div>
            <div class="contact-info">
                <i class="fa-solid fa-clock"></i> &#58; <p>9.00 AM - 5.00 PM</p>
            </div>
        </div>
    </section>
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
