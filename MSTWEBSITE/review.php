<?php
declare(strict_types=1);
require_once __DIR__ . '/account_shared.php';
account_require_login();
$userId=(int)$_SESSION['user_id']; $message='';$error='';
if(!account_table_exists($conn,'reviews')) { account_header('Reviews','review.php'); ?><div class="empty-state"><h3>Reviews are not set up yet</h3><p>Create the reviews table using the schema in README.md to let customers share fragrance feedback.</p></div><?php account_footer(); exit; }
if($_SERVER['REQUEST_METHOD']==='POST') {
    account_check_csrf(); $productId=filter_input(INPUT_POST,'product_id',FILTER_VALIDATE_INT); $rating=filter_input(INPUT_POST,'rating',FILTER_VALIDATE_INT); $comment=trim((string)($_POST['comment']??''));
    if(!$productId || !$rating || $rating<1 || $rating>5 || $comment==='') $error='Choose a perfume, select a rating from 1 to 5, and write a comment.';
    else {
        $stmt=$conn->prepare('SELECT product_id FROM products WHERE product_id=? LIMIT 1');
        $exists=false;if($stmt){$stmt->bind_param('i',$productId);$stmt->execute();$exists=(bool)$stmt->get_result()->fetch_assoc();$stmt->close();}
        if(!$exists)$error='That perfume could not be found.';
        else {
            $hasUser=account_column_exists($conn,'reviews','user_id'); $hasComment=account_column_exists($conn,'reviews','comment');
            if(!$hasUser || !$hasComment) $error='The reviews table needs user_id and comment columns. See README.md.';
            else {$stmt=$conn->prepare('INSERT INTO reviews (product_id,user_id,rating,comment) VALUES (?,?,?,?)');if($stmt){$stmt->bind_param('iiis',$productId,$userId,$rating,$comment);if($stmt->execute())$message='Thank you — your review has been submitted.';else$error='Could not save your review. Please try again.';$stmt->close();}}
        }
    }
}
$products=[];$r=$conn->query('SELECT product_id,name FROM products ORDER BY name');
if($r)while($row=$r->fetch_assoc())$products[]=$row;
account_header('Reviews','review.php');if($message)account_flash($message);if($error)account_flash($error,'error');
?>
<section class="panel"><h2>Share your fragrance experience</h2><p class="muted">Tell other customers what you thought of a scent.</p>
<?php if(!$products):?><div class="empty-state">No products are available to review yet.</div><?php else:?><form method="post"><input type="hidden" name="csrf_token" value="<?=account_e(account_csrf_token())?>"><div class="form-grid">
<div class="field"><label for="product_id">Perfume</label><select id="product_id" name="product_id" required><option value="">Choose a perfume</option><?php foreach($products as $p):?><option value="<?=(int)$p['product_id']?>"><?=account_e($p['name'])?></option><?php endforeach;?></select></div>
<div class="field"><label for="rating">Rating</label><select id="rating" name="rating" required><option value="">Choose a rating</option><option value="5">5 — Excellent</option><option value="4">4 — Very good</option><option value="3">3 — Good</option><option value="2">2 — Fair</option><option value="1">1 — Poor</option></select></div>
<div class="field full"><label for="comment">Your review</label><textarea id="comment" name="comment" required maxlength="2000" placeholder="What did you like about this fragrance?"></textarea></div>
</div><div class="form-actions"><button class="btn" type="submit">Submit review</button></div></form><?php endif;?></section>
<section class="panel"><h2>Your submitted reviews</h2>
<?php
$hasUser=account_column_exists($conn,'reviews','user_id');$hasComment=account_column_exists($conn,'reviews','comment');$hasCreated=account_column_exists($conn,'reviews','created_at');
if(!$hasUser||!$hasComment){echo '<p class="small-note">Your review history needs user_id and comment columns in the reviews table.</p>';}
else {$sql='SELECT r.rating,r.comment'.($hasCreated?',r.created_at':'').',p.name AS product_name FROM reviews r LEFT JOIN products p ON p.product_id=r.product_id WHERE r.user_id=? ORDER BY r.review_id DESC';$stmt=$conn->prepare($sql);
if($stmt){$stmt->bind_param('i',$userId);$stmt->execute();$result=$stmt->get_result();if(!$result->num_rows)echo '<div class="empty-state">You have not submitted any reviews yet.</div>';
while($row=$result->fetch_assoc()){echo '<article class="review-card"><h3>'.account_e($row['product_name']??'Perfume').'</h3><div class="stars">'.str_repeat('★',max(0,min(5,(int)$row['rating']))).str_repeat('☆',5-max(0,min(5,(int)$row['rating']))).'</div><p>'.nl2br(account_e($row['comment'])).'</p>';if($hasCreated)echo '<p class="small-note">'.account_e($row['created_at']??'').'</p>';echo '</article>';}
$stmt->close();}}
?>
</section>
<?php account_footer(); ?>
