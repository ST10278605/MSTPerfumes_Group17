<?php
session_start();
include 'MSTDBConn.php';
if (!isset($_SESSION['user_id']) || ($_SESSION['role'] ?? '') !== 'admin') {
    header('Location: adminlogin.php?redirect=admin_edit.php');
    exit();
}
$id = isset($_GET['id']) ? (int)$_GET['id'] : 0;
if (!$id) { header('Location: admin.php'); exit(); }
$stmt = $conn->prepare("SELECT * FROM products WHERE id = ?");
$stmt->bind_param('i', $id);
$stmt->execute();
$res = $stmt->get_result();
$product = $res->fetch_assoc();
$stmt->close();
if (!$product) { header('Location: admin.php'); exit(); }
?>
<!doctype html>
<html>
<head><meta charset="utf-8"><title>Edit Product</title></head>
<body>
    <h1>Edit Product #<?=htmlspecialchars($product['id'])?></h1>
    <form method="post" action="admin.php">
        <input type="hidden" name="action" value="edit">
        <input type="hidden" name="id" value="<?=htmlspecialchars($product['id'])?>">
        <label>SKU <input name="sku" value="<?=htmlspecialchars($product['sku'])?>"></label><br>
        <label>Name <input name="name" value="<?=htmlspecialchars($product['name'])?>"></label><br>
        <label>Price <input name="price" type="number" step="0.01" value="<?=htmlspecialchars($product['price'])?>"></label><br>
        <label>Stock <input name="stock" type="number" value="<?=htmlspecialchars($product['stock'])?>"></label><br>
        <label>Image <input name="img" value="<?=htmlspecialchars($product['img'])?>"></label><br>
        <button type="submit">Save</button>
    </form>
    <p><a href="admin.php">Back</a></p>
</body>
</html>