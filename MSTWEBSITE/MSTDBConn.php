<?php

$servername = "db";
$username = "websiteuser";
$password = "websitepassword";
$database = "mst_website";

$conn = new mysqli(
    $servername,
    $username,
    $password,
    $database
);

if ($conn->connect_error) {
    die("Database connection failed: " . $conn->connect_error);
}

$conn->set_charset("utf8mb4");
