<?php
session_start();
include 'MSTDBConn.php';

// Require admin
if (!isset($_SESSION['user_id']) || ($_SESSION['role'] ?? '') !== 'admin') {
    $redirect = 'admin.php';
    header('Location: adminlogin.php?redirect=' . urlencode($redirect));
    exit();
}

$message = '';
// create products table if missing (safe to run)
$conn->query("CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(50) UNIQUE,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    stock INT NOT NULL DEFAULT 0,
    img VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

// Handle add
if (isset($_POST['action']) && $_POST['action'] === 'add') {
    $sku = trim($_POST['sku']);
    $name = trim($_POST['name']);
    $price = (float)$_POST['price'];
    $stock = (int)$_POST['stock'];
    $img = trim($_POST['img']);
    $stmt = $conn->prepare("INSERT INTO products (sku, name, price, stock, img) VALUES (?, ?, ?, ?, ?)");
    if ($stmt) {
        $stmt->bind_param('ssdiss', $sku, $name, $price, $stock, $img);
        // bind_param types corrected below if needed
        $stmt->close();
    }
    // use simpler query to avoid bind type issues
    $q = $conn->prepare("INSERT INTO products (sku, name, price, stock, img) VALUES (?, ?, ?, ?, ?)");
    if ($q) {
        $q->bind_param('ssdis', $sku, $name, $price, $stock, $img);
        if ($q->execute()) $message = 'Product added'; else $message = 'Add failed: '.$conn->error;
        $q->close();
    } else {
        $message = 'Add prepare failed: '.$conn->error;
    }
}

// Handle delete
if (isset($_GET['delete'])) {
    $id = (int)$_GET['delete'];
    $d = $conn->prepare("DELETE FROM products WHERE id = ?");
    if ($d) {
        $d->bind_param('i', $id);
        if ($d->execute()) $message = 'Product deleted'; else $message = 'Delete failed: '.$conn->error;
        $d->close();
    }
    header('Location: admin.php');
    exit();
}

// Handle edit
if (isset($_POST['action']) && $_POST['action'] === 'edit') {
    $id = (int)$_POST['id'];
    $sku = trim($_POST['sku']);
    $name = trim($_POST['name']);
    $price = (float)$_POST['price'];
    $stock = (int)$_POST['stock'];
    $img = trim($_POST['img']);
    $u = $conn->prepare("UPDATE products SET sku = ?, name = ?, price = ?, stock = ?, img = ? WHERE id = ?");
    if ($u) {
        $u->bind_param('ssdisi', $sku, $name, $price, $stock, $img, $id);
        if ($u->execute()) $message = 'Product updated'; else $message = 'Update failed: '.$conn->error;
        $u->close();
    }
    header('Location: admin.php');
    exit();
}

// fetch products
$products = [];
$r = $conn->query("SELECT * FROM products ORDER BY id DESC");
if ($r) {
    while ($row = $r->fetch_assoc()) $products[] = $row;
}

?>
<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <title>Admin - Products</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <header>
        <h1>Admin — Products</h1>
        <p><a href="index.php">Home</a> | <a href="adminlogout.php">Logout</a></p>
    </header>
    <main>
        <?php if ($message): ?><div class="message"><?=htmlspecialchars($message)?></div><?php endif; ?>

        <section>
            <h2>Add Product</h2>
            <form method="post">
                <input type="hidden" name="action" value="add">
                <label>SKU <input name="sku" required></label><br>
                <label>Name <input name="name" required></label><br>
                <label>Price <input name="price" type="number" step="0.01" required></label><br>
                <label>Stock <input name="stock" type="number" required></label><br>
                <label>Image <input name="img"></label><br>
                <button type="submit">Add</button>
            </form>
        </section>

        <section>
            <h2>Existing Products</h2>
            <table border="1" cellpadding="6" cellspacing="0">
                <thead><tr><th>ID</th><th>SKU</th><th>Name</th><th>Price</th><th>Stock</th><th>Img</th><th>Actions</th></tr></thead>
                <tbody>
                <?php foreach ($products as $p): ?>
                    <tr>
                        <td><?=htmlspecialchars($p['id'])?></td>
                        <td><?=htmlspecialchars($p['sku'])?></td>
                        <td><?=htmlspecialchars($p['name'])?></td>
                        <td>R<?=number_format($p['price'],2)?></td>
                        <td><?=htmlspecialchars($p['stock'])?></td>
                        <td><?php if ($p['img']): ?><img src="<?=htmlspecialchars($p['img'])?>" style="height:40px"><?php endif; ?></td>
                        <td>
                            <a href="admin_edit.php?id=<?=$p['id']?>">Edit</a> |
                            <a href="admin.php?delete=<?=$p['id']?>" onclick="return confirm('Delete?')">Delete</a>
                        </td>
                    </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
        </section>
    </main>
</body>
</html>
