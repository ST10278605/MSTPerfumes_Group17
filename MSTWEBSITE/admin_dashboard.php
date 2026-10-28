<?php
declare(strict_types=1);
require_once __DIR__ . '/account_shared.php';
account_require_admin();
function safe_count(mysqli $conn, string $table): int {
    if (!account_table_exists($conn,$table)) return -1;
    $allowed=['users','products','orders','reviews'];
    if(!in_array($table,$allowed,true)) return -1;
    $result=$conn->query("SELECT COUNT(*) AS n FROM `$table`");
    if(!$result)return -1;
    return (int)($result->fetch_assoc()['n']??0);
}
$products=safe_count($conn,'products'); $users=safe_count($conn,'users'); $orders=safe_count($conn,'orders'); $reviews=safe_count($conn,'reviews');
account_header('Admin Dashboard','admin_dashboard.php');
?>
<div class="cards">
<div class="stat-card"><span>Products in catalogue</span><strong><?=$products<0?'—':number_format($products)?></strong></div>
<div class="stat-card"><span>Registered accounts</span><strong><?=$users<0?'—':number_format($users)?></strong></div>
<div class="stat-card"><span>Orders recorded</span><strong><?=$orders<0?'—':number_format($orders)?></strong></div>
</div>
<section class="panel"><h2>Store management</h2><p class="muted">Manage your fragrance catalogue, review customer orders, and keep your account information current.</p>
<div class="cards">
<div><h3>Product catalogue</h3><p class="muted">Add new perfumes, update prices and stock, or remove discontinued products.</p><a class="btn" href="admin_products.php">Manage products</a></div>
<div><h3>Customer orders</h3><p class="muted">Review orders and update their fulfilment status.</p><a class="btn" href="admin_orders.php">Manage orders</a></div>
<div><h3>Store reviews</h3><p class="muted"><?=$reviews<0?'Review table not found.':number_format($reviews).' review(s) recorded.'?></p><a class="btn btn-outline" href="review.php">Open reviews</a></div>
</div>
<?php if($products<0||$orders<0||$reviews<0): ?><p class="small-note">A dash means that the relevant table has not been created yet. See README.md in the supplied package for the expected database structure.</p><?php endif; ?>
</section>
<section class="panel"><h2>Quick links</h2><div class="form-actions"><a class="btn btn-outline" href="profile.php">Administrator profile</a><a class="btn btn-outline" href="index.php">View storefront</a></div></section>
<?php account_footer(); ?>
