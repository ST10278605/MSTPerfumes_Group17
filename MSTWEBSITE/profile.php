<?php
session_start();
error_reporting(E_ALL);
ini_set('display_errors', 1);
include "MSTDBConn.php"; // This file should create $conn = new mysqli(...)

// Only allow admins
if (!isset($_SESSION["user_id"]) || ($_SESSION["role"] ?? '') !== 'admin') {
    header("Location: login.php");
    exit();
}

$message = "";

// DELETE USER
if (isset($_GET["delete_user"])) {
    $delete_id = (int)$_GET["delete_user"];
    if ($delete_id != $_SESSION["user_id"]) {
        $stmt = $conn->prepare("DELETE FROM users WHERE user_id = ?");
        $stmt->bind_param("i", $delete_id);
        $stmt->execute();
        $stmt->close();
        $message = "User deleted";
    } else {
        $message = "Cannot delete your own account";
    }
}

// ADD PRODUCT
if (isset($_POST["add_product"])) {
    $name = trim($_POST["name"]);
    $description = trim($_POST["description"]);
    $size = trim($_POST["size"]);
    $category = $_POST["category"];
    
    if ($name && $category) {
        $stmt = $conn->prepare("INSERT INTO Product (name, description, size, Category) VALUES (?, ?, ?, ?)");
        $stmt->bind_param("ssss", $name, $description, $size, $category);
        $stmt->execute();
        $stmt->close();
        $message = "Product added";
    } else {
        $message = "Name and category required";
    }
}

// UPDATE PRODUCT
if (isset($_POST["update_product"])) {
    $product_id = $_POST["product_id"];
    $name = trim($_POST["name"]);
    $description = trim($_POST["description"]);
    $size = trim($_POST["size"]);
    $category = $_POST["category"];
    
    $stmt = $conn->prepare("UPDATE Product SET name = ?, description = ?, size = ?, Category = ? WHERE id = ?");
    $stmt->bind_param("ssssi", $name, $description, $size, $category, $product_id);
    if ($stmt->execute()) {
        $message = "Product updated";
    } else {
        $message = "Update failed: " . $conn->error;
    }
    $stmt->close();
}

// DELETE PRODUCT
if (isset($_GET["delete_product"])) {
    $delete_id = (int)$_GET["delete_product"];
    $stmt = $conn->prepare("DELETE FROM Product WHERE id = ?");
    $stmt->bind_param("i", $delete_id);
    $stmt->execute();
    $stmt->close();
    $message = "Product deleted";
}

// GET ALL USERS
$users = [];
$result = $conn->query("SELECT user_id, name, email, role, created_at FROM users ORDER BY created_at DESC");
if ($result) {
    while ($row = $result->fetch_assoc()) {
        $users[] = $row;
    }
}

// GET ALL PRODUCTS
$products = [];
$result = $conn->query("SELECT id, name, description, size, Category FROM Product ORDER BY id DESC");
if ($result) {
    while ($row = $result->fetch_assoc()) {
        $products[] = $row;
    }
}

// GET ALL ORDERS (for admin report)
$orders = [];
$res = $conn->query("SELECT o.id, o.user_id, o.order_ref, o.total, o.created_at, u.name AS user_name
    FROM orders o
    LEFT JOIN users u ON u.user_id = o.user_id
    ORDER BY o.created_at DESC");
if ($res) {
    while ($row = $res->fetch_assoc()) {
        $orders[$row['id']] = $row;
        $orders[$row['id']]['items'] = [];
    }
}

// fetch items for each order
if (!empty($orders)) {
    $ids = implode(',', array_keys($orders));
    $itRes = $conn->query("SELECT order_id, product_id, product_name, qty, price FROM order_items WHERE order_id IN ($ids) ORDER BY id ASC");
    if ($itRes) {
        while ($it = $itRes->fetch_assoc()) {
            $orders[$it['order_id']]['items'][] = $it;
        }
    }
}

// summary totals
$summary = $conn->query("SELECT COUNT(*) as total_orders, IFNULL(SUM(total),0) as total_revenue FROM orders")->fetch_assoc();

?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard | MST PERFUMES</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        .admin-container { max-width: 1200px; margin: 40px auto; padding: 0 20px; }
        .admin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
        .admin-table { width: 100%; border-collapse: collapse; background: #fff; box-shadow: 0 2px 5px rgba(0,0,0,0.1); margin-bottom: 40px; }
        .admin-table th, .admin-table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
        .admin-table th { background: #333; color: #fff; }
        .admin-table tr:hover { background: #f9f9f9; }
        .btn-small { padding: 6px 12px; font-size: 0.9rem; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; display: inline-block; margin: 2px; }
        .btn-edit { background: gold; color: #333; }
        .btn-delete { background: #dc3545; color: #fff; }
        .btn-add { background: #28a745; color: #fff; }
        .btn-logout { background: #333; color: #fff; padding: 10px 20px; }
        .edit-form, .add-form { background: #f9f9f9; padding: 15px; margin: 10px 0; border-radius: 5px; }
        .edit-form input, .edit-form select, .edit-form textarea,
        .add-form input, .add-form select, .add-form textarea {
            padding: 8px; margin: 5px; border: 1px solid #ddd; border-radius: 4px; width: 200px;
        }
        .edit-form textarea, .add-form textarea { width: 300px; height: 60px; }
        .message { padding: 15px; margin: 20px 0; background: #d4edda; color: #155724; border-radius: 5px; }
        .tab-buttons { margin-bottom: 20px; }
        .tab-btn { padding: 10px 20px; background: #eee; border: none; cursor: pointer; margin-right: 10px; }
        .tab-btn.active { background: gold; }
        .tab-content { display: none; }
        .tab-content.active { display: block; }
    </style>
</head>
<body>
    <header class="header">
        <nav class="navbar">
            <div class="navbar-container">
                <div class="navbar-brand">
                    <a href="index.php"><img src="pictures/logo.png" alt="MST Perfumes Logo"></a>
                </div>
                <div class="navbar-user">
                    <span>Admin: <?= htmlspecialchars($_SESSION["name"]) ?></span>
                    <a href="login.php?logout=1" class="btn-small btn-logout">Logout</a>
                </div>
            </div>
        </nav>
    </header>

    <main class="admin-container">
        <div class="admin-header">
            <h1>Admin Dashboard</h1>
        </div>

        <?php if ($message): ?>
            <div class="message"><?= htmlspecialchars($message) ?></div>
        <?php endif; ?>

        <div class="tab-buttons">
            <button class="tab-btn active" onclick="showTab('users', this)">Users</button>
            <button class="tab-btn" onclick="showTab('products', this)">Products</button>
            <button class="tab-btn" onclick="showTab('orders', this)">Orders</button>
        </div>

        <!-- USERS TAB -->
        <div id="users-tab" class="tab-content active">
            <h2>All Users</h2>
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Joined</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($users as $user): ?>
                    <tr>
                        <td><?= $user["user_id"] ?></td>
                        <td><?= htmlspecialchars($user["name"]) ?></td>
                        <td><?= htmlspecialchars($user["email"]) ?></td>
                        <td><?= htmlspecialchars($user["role"]) ?></td>
                        <td><?= date('Y-m-d', strtotime($user["created_at"])) ?></td>
                        <td>
                            <?php if ($user["user_id"] != $_SESSION["user_id"]): ?>
                                <a href="?delete_user=<?= $user["user_id"] ?>" 
                                   class="btn-small btn-delete" 
                                   onclick="return confirm('Delete this user?')">Delete</a>
                            <?php else: ?>
                                <span style="color: #999;">You</span>
                            <?php endif; ?>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>

        <!-- PRODUCTS TAB -->
        <div id="products-tab" class="tab-content">
            <h2>All Products</h2>
            
            <form method="post" class="add-form">
                <h3>Add New Product</h3>
                <input type="text" name="name" placeholder="Product Name" required>
                <input type="text" name="size" placeholder="Size e.g. 50ml">
                <select name="category" required>
                    <option value="">Category</option>
                    <option value="men">Men</option>
                    <option value="women">Women</option>
                </select>
                <br>
                <textarea name="description" placeholder="Description"></textarea>
                <br>
                <button type="submit" name="add_product" class="btn-small btn-add">Add Product</button>
            </form>

            <table class="admin-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Size</th>
                        <th>Category</th>
                        <th>Description</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($products as $product): ?>
                    <tr>
                        <td><?= $product["id"] ?></td>
                        <td><?= htmlspecialchars($product["name"]) ?></td>
                        <td><?= htmlspecialchars($product["size"]) ?></td>
                        <td><?= htmlspecialchars($product["Category"]) ?></td>
                        <td><?= htmlspecialchars(substr($product["description"], 0, 50)) ?>...</td>
                        <td>
                            <button class="btn-small btn-edit" onclick="toggleEdit(<?= $product["id"] ?>)">Edit</button>
                            <a href="?delete_product=<?= $product["id"] ?>" 
                               class="btn-small btn-delete" 
                               onclick="return confirm('Delete this product?')">Delete</a>
                        </td>
                    </tr>
                    <tr id="edit-<?= $product["id"] ?>" style="display:none;">
                        <td colspan="6">
                            <form method="post" class="edit-form">
                                <input type="hidden" name="product_id" value="<?= $product["id"] ?>">
                                <input type="text" name="name" value="<?= htmlspecialchars($product["name"]) ?>" placeholder="Name" required>
                                <input type="text" name="size" value="<?= htmlspecialchars($product["size"]) ?>" placeholder="Size">
                                <select name="category" required>
                                    <option value="men" <?= $product["Category"] == 'men' ? 'selected' : '' ?>>Men</option>
                                    <option value="women" <?= $product["Category"] == 'women' ? 'selected' : '' ?>>Women</option>
                                </select>
                                <br>
                                <textarea name="description" placeholder="Description"><?= htmlspecialchars($product["description"]) ?></textarea>
                                <br>
                                <button type="submit" name="update_product" class="btn-small btn-edit">Save</button>
                                <button type="button" class="btn-small" onclick="toggleEdit(<?= $product["id"] ?>)">Cancel</button>
                            </form>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>

        <!-- ORDERS TAB -->
        <div id="orders-tab" class="tab-content">
            <h2>Purchase History</h2>
            <p><strong>Total Orders:</strong> <?= htmlspecialchars($summary['total_orders'] ?? 0) ?> &nbsp; <strong>Total Revenue:</strong> R<?= number_format($summary['total_revenue'] ?? 0, 2) ?></p>
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>Order ID</th>
                        <th>Reference</th>
                        <th>Customer</th>
                        <th>Items</th>
                        <th>Total</th>
                        <th>Date</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($orders as $order): ?>
                    <tr>
                        <td><?= $order['id'] ?></td>
                        <td><?= htmlspecialchars($order['order_ref']) ?></td>
                        <td><?= htmlspecialchars($order['user_name'] ?? 'Guest') ?></td>
                        <td>
                            <?php foreach ($order['items'] as $it): ?>
                                <div><?= htmlspecialchars($it['product_name']) ?> x <?= (int)$it['qty'] ?> (R<?= number_format($it['price'],2) ?>)</div>
                            <?php endforeach; ?>
                        </td>
                        <td>R<?= number_format($order['total'], 2) ?></td>
                        <td><?= htmlspecialchars($order['created_at']) ?></td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </main>

    <script>
        function showTab(tab, el) {
            document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
            document.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));
            document.getElementById(tab + '-tab').classList.add('active');
            if (el) el.classList.add('active');
        }
        
        function toggleEdit(id) {
            const row = document.getElementById('edit-' + id);
            row.style.display = row.style.display === 'none' ? 'table-row' : 'none';
        }
    </script>
</body>
</html>