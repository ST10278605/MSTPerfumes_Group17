<?php

session_start();

/*
|--------------------------------------------------------------------------
| USER SESSION
|--------------------------------------------------------------------------
*/

// Check if the user is logged in
$isLoggedIn = isset($_SESSION["user_id"]);

// Get the user's name
$username = null;

if ($isLoggedIn) {
    $username = htmlspecialchars(
        $_SESSION["name"] ?? "User",
        ENT_QUOTES,
        "UTF-8"
    );
}


/*
|--------------------------------------------------------------------------
| CART
|--------------------------------------------------------------------------
*/

// Default cart count
$cartCount = 0;

// Check whether the session cart exists
if (
    isset($_SESSION["cart"]) &&
    is_array($_SESSION["cart"])
) {
    $cartCount = array_sum($_SESSION["cart"]);
}

?>

<!DOCTYPE html>

<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0"
    >

    <title>MST PERFUMES | reviews</title>


    <!-- Main CSS -->
    <link
        rel="stylesheet"
        href="css/style.css"
    >

    <!-- Font Awesome -->
    <link
        rel="stylesheet"
        href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css"
    >

</head>


<body>


<!-- =========================================================
     NAVIGATION
========================================================= -->

<nav class="navbar">

    <div class="navbar-container">


        <!-- Brand -->

        <div class="navbar-brand">

            <h1>

                <a href="index.php">
                    MST PERFUMES
                </a>

            </h1>

        </div>


        <!-- Navigation links -->

        <ul class="navbar-items">

            <li>
                <a href="index.php">
                    HOME
                </a>
            </li>

            <li>
                <a href="shopping.php">
                    PRODUCTS
                </a>
            </li>

            <li>
                <a href="forum.php">
                    FORUM
                </a>
            </li>

            <li>
                <a href="cart.php">
                    VIEW CART (<?= $cartCount ?>)
                </a>
            </li>

        </ul>


        <!-- Login / Logout -->

        <div class="navbar-account">

            <?php if ($isLoggedIn): ?>

                <a href="login.php?logout=1">
                    Logout
                </a>

            <?php else: ?>

                <a href="login.php">
                    Login
                </a>

            <?php endif; ?>

        </div>

    </div>

</nav>


<!-- =========================================================
     HEADER
========================================================= -->

<header class="header">

    <div class="top-bar">


        <!-- Welcome -->

        <div>

            <?php if ($isLoggedIn): ?>

                Welcome, <?= $username ?>

            <?php else: ?>

                Welcome, Guest

            <?php endif; ?>

        </div>


        <!-- Account links -->

        <div>

            <?php if (!$isLoggedIn): ?>

                <a href="login.php">
                    Login
                </a>

                /

                <a href="register.php">
                    Register
                </a>

            <?php else: ?>

                <a href="cart.php">
                    Cart (<?= $cartCount ?>)
                </a>

            <?php endif; ?>

        </div>

    </div>

</header>


<!-- =========================================================
     MAIN CONTENT
========================================================= -->

<main>


    <!-- =====================================================
         FORUM HERO
    ====================================================== -->

    <section
        id="forum-hero"
        class="page-hero"
    >

        <div class="container">

            <h2>
                The Entrepreneur Hub
            </h2>

            <p>
                Connect with peers, share sales tips,
                and grow your fragrance business.
            </p>


            <?php if ($isLoggedIn): ?>

                <a
                    href="#new-post"
                    class="btn btn-primary"
                >
                    Start a New Discussion
                </a>

            <?php else: ?>

                <a
                    href="login.php"
                    class="btn btn-primary"
                >
                    Login to Start a Discussion
                </a>

            <?php endif; ?>

        </div>

    </section>


    <!-- =====================================================
         FORUM TOPICS
    ====================================================== -->

    <section
        id="forum-topics"
        class="page-content container"
    >

        <h3>
            Recent Discussions
        </h3>


        <div class="forum-list">


            <!-- Topic 1 -->

            <article class="forum-topic">

                <div class="topic-header">

                    <h4>
                        Effective Social Media Marketing
                        for Fragrances
                    </h4>

                    <span class="topic-meta">

                        Posted by: Alex |
                        56 Replies |
                        Last Activity: 2h ago

                    </span>

                </div>


                <div class="topic-excerpt">

                    <p>
                        I've been struggling to generate
                        interest on Instagram. What strategies
                        are working for everyone?
                    </p>

                </div>

            </article>


            <!-- Topic 2 -->

            <article class="forum-topic">

                <div class="topic-header">

                    <h4>
                        Success Story: My First Bulk Order Profit
                    </h4>

                    <span class="topic-meta">

                        Posted by: Sarah M. |
                        12 Replies |
                        Last Activity: 1 day ago

                    </span>

                </div>


                <div class="topic-excerpt">

                    <p>
                        Just wanted to share my first
                        profitable month! Key lesson:
                        focus on niche scents.
                    </p>

                </div>

            </article>

        </div>

    </section>


    <!-- =====================================================
         NEW DISCUSSION
    ====================================================== -->

    <?php if ($isLoggedIn): ?>

        <section
            id="new-post"
            class="page-content container"
        >

            <h3>
                Start a New Discussion
            </h3>


            <form
                action="create_post.php"
                method="POST"
            >

                <div class="input-group">

                    <label for="title">
                        Discussion Title
                    </label>

                    <input
                        type="text"
                        id="title"
                        name="title"
                        placeholder="Enter discussion title"
                        required
                    >

                </div>


                <div class="input-group">

                    <label for="content">
                        Message
                    </label>

                    <textarea
                        id="content"
                        name="content"
                        placeholder="Write your discussion..."
                        rows="6"
                        required
                    ></textarea>

                </div>


                <button
                    type="submit"
                    name="create_post"
                    class="btn btn-primary"
                >
                    Post Discussion
                </button>

            </form>

        </section>

    <?php endif; ?>


    <!-- =====================================================
         REVIEWS SLIDER
    ====================================================== -->

    <section class="slider">

        <div class="reviews">

            <h2>
                <span>/</span>
                reviews
            </h2>

        </div>


        <div class="slide-container">
            <!-- Reviews will be loaded here -->
        </div>


        <!-- Previous button -->

        <button
            class="btn prev-btn"
            type="button"
            aria-label="Previous review"
        >

            <i class="fas fa-chevron-left"></i>

        </button>


        <!-- Next button -->

        <button
            class="btn next-btn"
            type="button"
            aria-label="Next review"
        >

            <i class="fas fa-chevron-right"></i>

        </button>

    </section>

</main>


<!-- =========================================================
     FOOTER
========================================================= -->

<footer>

    <h1 class="footer-brand">
        MST PERFUMES
    </h1>

    <p>
        &copy; <?= date("Y") ?>
        MST Perfumes. All rights reserved.
    </p>

</footer>


<!-- =========================================================
     JAVASCRIPT
========================================================= -->

<script src="js/Script.js"></script>

</body>

</html>
