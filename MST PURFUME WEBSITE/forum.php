<?php
session_start();

$isLoggedIn = isset($_SESSION["user_id"]);
$username = $isLoggedIn ? htmlspecialchars($_SESSION["name"] ?? $_SESSION["username"] ?? 'User') : null;

$cartCount = 0;
if (isset($_SESSION["cart"]) && is_array($_SESSION["cart"])) {
    $cartCount = array_sum($_SESSION["cart"]);
}
// include DB and handle forum actions
include "MSTDBConn.php";

$forumMessage = '';
if (isset($_POST['new_discussion'])) {
    $title = trim($_POST['title'] ?? '');
    $body = trim($_POST['body'] ?? '');
    $user_name = $isLoggedIn ? ($_SESSION['name'] ?? 'Member') : trim($_POST['user_name'] ?? 'Guest');
    if ($title === '' || $body === '') {
        $forumMessage = 'Title and message are required';
    } else {
        // detect discussions table columns and insert using available names
        $colsRes = $conn->query("SHOW COLUMNS FROM discussions");
        $cols = [];
        if ($colsRes) {
            while ($c = $colsRes->fetch_assoc()) $cols[] = $c['Field'];
        }

        $titleCol = null; foreach (['title','subject','topic'] as $c) if (in_array($c, $cols)) { $titleCol = $c; break; }
        $bodyCol  = null; foreach (['body','content','message','post'] as $c) if (in_array($c, $cols)) { $bodyCol = $c; break; }
        $userCol  = null; foreach (['user_name','author','name','username'] as $c) if (in_array($c, $cols)) { $userCol = $c; break; }
        $userIdCol = in_array('user_id', $cols) ? 'user_id' : null;

        if (!$titleCol || !$bodyCol) {
            $forumMessage = 'Database `discussions` table missing required columns. Run db_setup.sql or contact admin.';
        } else {
            // If the table enforces a user_id foreign key, require login
            if ($userIdCol && !$isLoggedIn) {
                $forumMessage = 'You must be logged in to post a discussion.';
            } else {
                $insertCols = [];
                $placeholders = [];
                $types = '';
                $values = [];
                // title
                $insertCols[] = $titleCol; $placeholders[]='?'; $types.='s'; $values[]=$title;
                // user_id if present
                if ($userIdCol) { $insertCols[] = $userIdCol; $placeholders[]='?'; $types.='i'; $values[]=$_SESSION['user_id']; }
                // user name if present
                if ($userCol) { $insertCols[] = $userCol; $placeholders[]='?'; $types.='s'; $values[]=$user_name; }
                // body
                $insertCols[] = $bodyCol; $placeholders[]='?'; $types.='s'; $values[]=$body;

                $sql = 'INSERT INTO discussions (' . implode(', ', $insertCols) . ') VALUES (' . implode(', ', $placeholders) . ')';
                $stmt = $conn->prepare($sql);
                if ($stmt) {
                    try {
                        $stmt->bind_param($types, ...$values);
                        $stmt->execute();
                        $stmt->close();
                        $forumMessage = 'Discussion created';
                    } catch (mysqli_sql_exception $e) {
                        $forumMessage = 'Failed to save discussion: ' . $e->getMessage();
                    }
                } else {
                    $forumMessage = 'Failed to save discussion (prepare failed)';
                }
            }
        }
    }
}

// fetch discussions (use available columns)
$discussions = [];
$colsRes = $conn->query("SHOW COLUMNS FROM discussions");
if ($colsRes) {
    $cols = [];
    while ($c = $colsRes->fetch_assoc()) $cols[] = $c['Field'];
    $titleCol = null; foreach (['title','subject','topic'] as $c) if (in_array($c, $cols)) { $titleCol = $c; break; }
    $bodyCol  = null; foreach (['body','content','message','post'] as $c) if (in_array($c, $cols)) { $bodyCol = $c; break; }
    $userCol  = null; foreach (['user_name','author','name','username'] as $c) if (in_array($c, $cols)) { $userCol = $c; break; }
    $dateCol  = null; foreach (['created_at','created','date'] as $c) if (in_array($c, $cols)) { $dateCol = $c; break; }

    $select = ['id'];
    $select[] = $titleCol ? "$titleCol AS title" : "'' AS title";
    $select[] = $userCol  ? "$userCol AS user_name" : "NULL AS user_name";
    $select[] = $bodyCol  ? "$bodyCol AS body" : "'' AS body";
    $select[] = $dateCol  ? "$dateCol AS created_at" : "NOW() AS created_at";

    $orderBy = $dateCol ? $dateCol : 'created_at';
    $sql = 'SELECT ' . implode(', ', $select) . ' FROM discussions ORDER BY ' . $orderBy . ' DESC';
    $dres = $conn->query($sql);
    if ($dres) {
        while ($row = $dres->fetch_assoc()) $discussions[] = $row;
    }
} else {
    $discussions = [];
}

// fetch recent reviews to show here as well
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
    $sql = 'SELECT ' . implode(', ', $select) . ' FROM reviews ORDER BY ' . $orderBy . ' DESC LIMIT 6';
    $rres = $conn->query($sql);
    if ($rres) {
        while ($r = $rres->fetch_assoc()) $reviews[] = $r;
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
    <title>MST PERFUMES | FORUM</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
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

            <!-- Menu Items -->
            <ul class="navbar-items">
                <li><a href="index.php">HOME</a></li>
                <li><a href="shopping.php">Products</a></li>
                <li><a href="forum.php">Forum</a></li>
                <li><a href="cart.php">View Cart (<?= $cartCount ?>)</a></li>
            </ul>

            <!-- User Actions -->
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

    <main>
        <section id="forum-hero" class="page-hero">
            <div class="container">
                <h2>The Entrepreneur Hub</h2>
                <p>Connect with peers, share sales tips, and grow your fragrance business.</p>
                <a href="#new-post" class="btn btn-primary">Start a New Discussion</a>
            </div>
        </section>

        <section id="forum-topics" class="page-content container">
            <h3>Recent Discussions</h3>
            <?php if ($forumMessage): ?><p class="error-msg"><?= htmlspecialchars($forumMessage) ?></p><?php endif; ?>
            <div class="forum-list">
                <?php if (!empty($discussions)): ?>
                    <?php foreach ($discussions as $d): ?>
                        <article class="forum-topic">
                            <div class="topic-header">
                                <h4><?= htmlspecialchars($d['title']) ?></h4>
                                <span class="topic-meta">Posted by: <?= htmlspecialchars($d['user_name'] ?? 'Guest') ?> | <?= htmlspecialchars($d['created_at']) ?></span>
                            </div>
                            <div class="topic-excerpt">
                                <p><?= nl2br(htmlspecialchars(substr($d['body'],0,400))) ?></p>
                            </div>
                        </article>
                    <?php endforeach; ?>
                <?php else: ?>
                    <p>No discussions yet. Start one below.</p>
                <?php endif; ?>
            </div>
        </section>

        <section id="new-post" class="page-content container">
            <h3>Start a New Discussion</h3>
            <form method="post">
                <input type="text" name="title" placeholder="Topic title" required style="width:100%;padding:8px;margin:6px 0;">
                <?php if (!$isLoggedIn): ?>
                    <input type="text" name="user_name" placeholder="Your name" style="width:100%;padding:8px;margin:6px 0;">
                <?php endif; ?>
                <textarea name="body" placeholder="Write your message" required style="width:100%;height:140px;padding:8px;margin:6px 0;"></textarea>
                <button type="submit" name="new_discussion" class="btn-primary">Post Discussion</button>
            </form>
        </section>

        <section class="slider">
            <div class="reviews">
                <h2><span>/</span>reviews</h2>
            </div>
            <div class="slide-container">
                <?php if (!empty($reviews)): ?>
                    <?php foreach ($reviews as $rv): ?>
                        <article class="slide active">
                            <h4><?= htmlspecialchars($rv['name']) ?> - Rating <?= (int)$rv['rating'] ?>/5</h4>
                            <p><?= nl2br(htmlspecialchars($rv['review'])) ?></p>
                            <small><?= htmlspecialchars($rv['created_at']) ?></small>
                        </article>
                    <?php endforeach; ?>
                <?php else: ?>
                    <p>No reviews yet.</p>
                <?php endif; ?>
            </div>
            <!-- buttons -->
            <button class="btn prev-btn">
                <i class="fas fa-chevron-left"></i>
            </button>
            <button class="btn next-btn">
                <i class="fas fa-chevron-right"></i>
            </button>
        </section>

        <section id="add-review" class="page-content container">
            <h3>Leave a Review</h3>
            <form method="post" action="Index.php">
                <p>Reviews are posted on the homepage. You will be redirected to submit a review there.</p>
                <a href="Index.php#reviews" class="btn-primary">Go to Reviews</a>
            </form>
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