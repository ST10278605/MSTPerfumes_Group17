<?php
declare(strict_types=1);
require_once __DIR__ . '/account_shared.php';
account_require_admin();
$message=''; $error='';
if(!account_table_exists($conn,'products')) { account_header('Manage Products','admin_products.php'); account_flash('The products table does not exist yet. Create it using the schema in README.md.','error'); account_footer(); exit; }
if($_SERVER['REQUEST_METHOD']==='POST') {
    account_check_csrf(); $action=(string)($_POST['action']??'');
    if($action==='delete') {
        $id=filter_input(INPUT_POST,'product_id',FILTER_VALIDATE_INT);
        if($id){$stmt=$conn->prepare('DELETE FROM products WHERE product_id=?');if($stmt){$stmt->bind_param('i',$id);$stmt->execute();$message=$stmt->affected_rows?'Product deleted.':'No product was deleted.';$stmt->close();}}
    } elseif($action==='save') {
        $id=(int)($_POST['product_id']??0); $name=trim((string)($_POST['name']??'')); $description=trim((string)($_POST['description']??'')); $price=(float)($_POST['price']??0); $image=trim((string)($_POST['image']??'')); $category=trim((string)($_POST['category']??'Unisex')); $stock=(int)($_POST['stock_quantity']??0);
        if($name===''||$price<0||$stock<0){$error='Enter a product name and valid non-negative price and stock.';}
        elseif($id>0){$stmt=$conn->prepare('UPDATE products SET name=?, description=?, price=?, image=?, category=?, stock_quantity=? WHERE product_id=?');if($stmt){$stmt->bind_param('ssdssii',$name,$description,$price,$image,$category,$stock,$id);if($stmt->execute())$message='Product updated.';else$error='Could not update product.';$stmt->close();}}
        else {$stmt=$conn->prepare('INSERT INTO products (name,description,price,image,category,stock_quantity) VALUES (?,?,?,?,?,?)');if($stmt){$stmt->bind_param('ssdssi',$name,$description,$price,$image,$category,$stock);if($stmt->execute())$message='Product added to the catalogue.';else$error='Could not add product.';$stmt->close();}}
    }
}
$edit=null;
$editId=filter_input(INPUT_GET,'edit',FILTER_VALIDATE_INT);
if($editId){$stmt=$conn->prepare('SELECT product_id,name,description,price,image,category,stock_quantity FROM products WHERE product_id=?');if($stmt){$stmt->bind_param('i',$editId);$stmt->execute();$edit=$stmt->get_result()->fetch_assoc();$stmt->close();}}
$result=$conn->query('SELECT product_id,name,price,image,category,stock_quantity FROM products ORDER BY name ASC');
account_header('Manage Products','admin_products.php'); if($message)account_flash($message); if($error)account_flash($error,'error');
?>
<section class="panel"><h2><?=$edit?'Edit perfume':'Add a perfume'?></h2><form method="post"><input type="hidden" name="csrf_token" value="<?=account_e(account_csrf_token())?>"><input type="hidden" name="action" value="save"><input type="hidden" name="product_id" value="<?=account_e($edit['product_id']??0)?>">
<div class="form-grid"><div class="field"><label for="name">Product name</label><input id="name" name="name" required value="<?=account_e($edit['name']??'')?>"></div>
<div class="field"><label for="price">Price (R)</label><input id="price" name="price" type="number" min="0" step="0.01" required value="<?=account_e($edit['price']??'')?>"></div>
<div class="field"><label for="category">Category</label><select id="category" name="category"><?php foreach(['Male','Female','Unisex'] as $c): ?><option value="<?=$c?>" <?=($edit['category']??'Unisex')===$c?'selected':''?>><?=$c?></option><?php endforeach; ?></select></div>
<div class="field"><label for="stock_quantity">Stock quantity</label><input id="stock_quantity" name="stock_quantity" type="number" min="0" required value="<?=account_e($edit['stock_quantity']??'0')?>"></div>
<div class="field"><label for="image">Image path</label><input id="image" name="image" placeholder="pictures/perfume.jpeg" value="<?=account_e($edit['image']??'')?>"></div>
<div class="field full"><label for="description">Description</label><textarea id="description" name="description"><?=account_e($edit['description']??'')?></textarea></div></div>
<div class="form-actions"><button class="btn" type="submit"><?=$edit?'Save changes':'Add product'?></button><?php if($edit): ?><a class="btn btn-outline" href="admin_products.php">Cancel edit</a><?php endif; ?></div></form></section>
<section class="panel"><div class="panel-heading"><h2>Current catalogue</h2><span class="badge"><?=$result?$result->num_rows:0?> products</span></div>
<?php if(!$result||$result->num_rows===0): ?><div class="empty-state"><h3>No products yet</h3><p>Add a perfume using the form above.</p></div><?php else: ?><div class="table-wrap"><table><thead><tr><th>Product</th><th>Category</th><th>Price</th><th>Stock</th><th>Actions</th></tr></thead><tbody>
<?php while($p=$result->fetch_assoc()): ?><tr><td><strong><?=account_e($p['name'])?></strong><br><span class="small-note"><?=account_e($p['image']??'')?></span></td><td><?=account_e($p['category']??'')?></td><td>R<?=number_format((float)$p['price'],2)?></td><td><?=$p['stock_quantity']<10?'<strong>'.(int)$p['stock_quantity'].' left</strong>':(int)$p['stock_quantity']?></td><td><div class="inline-form"><a class="btn btn-outline btn-small" href="admin_products.php?edit=<?=(int)$p['product_id']?>">Edit</a><form method="post" onsubmit="return confirm('Delete this perfume? This cannot be undone.');"><input type="hidden" name="csrf_token" value="<?=account_e(account_csrf_token())?>"><input type="hidden" name="action" value="delete"><input type="hidden" name="product_id" value="<?=(int)$p['product_id']?>"><button class="btn btn-danger btn-small" type="submit">Delete</button></form></div></td></tr><?php endwhile; ?>
</tbody></table></div><?php endif; ?></section>
<?php account_footer(); ?>
