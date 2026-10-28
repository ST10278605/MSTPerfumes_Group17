# MST Perfumes account and admin pages

This package contains the pages linked from the profile dropdown, styled to match the MST Perfumes burgundy, gold, and cream look.

## Files
- `account_shared.php` — shared session, access-control, escaping, CSRF, navigation, and layout helpers.
- `css/account.css` — account-page styling.
- `profile.php` — administrator's profile.
- `usersprofile.php` — customer profile.
- `admin_dashboard.php` — store overview and quick links.
- `admin_products.php` — create, edit, and delete products.
- `admin_orders.php` — view orders and update order status.
- `order.php` — show only the signed-in customer's orders.
- `review.php` — submit and view the signed-in customer's reviews.

## Install
1. Back up your project.
2. Copy all PHP files into the same folder as `index.php` and `MSTDBConn.php`.
3. Copy `css/account.css` into your existing `css` folder. Keep your existing `css/style.css`.
4. Check that `MSTDBConn.php` creates a `mysqli` variable named `$conn`.
5. Ensure your login code sets `$_SESSION['user_id']`, `$_SESSION['name']`, `$_SESSION['email']`, and `$_SESSION['role']` (role must be `admin` or `user`).
6. Update the profile dropdown links to the matching filenames in the prompt. The supplied page names match those links.
7. Test with one customer account and one admin account. The pages enforce login and admin access on the server, not just through the menu.

## Existing database assumptions
The pages assume the following existing tables/columns:
- `users`: `user_id`, `name`, `email`, `phone_number`, `address`, `delivery_address`, `role`.
- `products`: `product_id`, `name`, `description`, `price`, `image`, `category`, `stock_quantity`.
- Database connection is selected in `MSTDBConn.php`.

The profile pages update the user's name, email, phone, home address, and delivery address. They do not display or change passwords.

## Optional tables needed for orders and reviews
Your existing checkout system may use different table names or columns. These pages cannot display real orders until your checkout code saves them to an `orders` table. The included order pages assume this structure:

```sql
CREATE TABLE IF NOT EXISTS orders (
  order_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(30) NOT NULL DEFAULT 'Pending',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX (user_id),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

If your checkout has order line items, a suggested structure is:

```sql
CREATE TABLE IF NOT EXISTS order_items (
  order_item_id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT NOT NULL,
  product_id INT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  INDEX (order_id),
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
  CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

Reviews page assumes:

```sql
CREATE TABLE IF NOT EXISTS reviews (
  review_id INT AUTO_INCREMENT PRIMARY KEY,
  product_id INT NOT NULL,
  user_id INT NOT NULL,
  rating TINYINT NOT NULL,
  comment TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX (product_id),
  INDEX (user_id),
  CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5),
  CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(product_id),
  CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Important:** Run the optional table SQL only if you do not already have equivalent tables. If your existing `orders` or `reviews` schema is different, adapt the PHP queries to your actual columns instead of creating duplicate tables. The admin order page can only update `orders.status` if that column exists.

## Notes
- Prices and quantities are displayed from the database; no product data is fabricated.
- `admin_products.php` supports product management using the `products` columns above.
- Deleting a product may be blocked by foreign-key constraints if the product is already referenced by order items or reviews. Prefer deactivating products instead of deleting in a production store.
- `login.php?logout=1` is linked as in the current dropdown. Make sure your login page actually implements logout securely.
- The profile pages assume the `users` columns listed above exist. If your actual schema differs, share `DESCRIBE users;`, `DESCRIBE products;`, `DESCRIBE orders;`, and `DESCRIBE reviews;` and the pages can be adapted precisely.
