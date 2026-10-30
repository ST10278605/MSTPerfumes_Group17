// Firebase Version 10+ browser SDKs loaded from CDN
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import {
  getAuth,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signOut,
  GoogleAuthProvider,
  signInWithPopup
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";
import {
  getFirestore,
  doc,
  setDoc,
  collection,
  getDocs
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";

// Your exact MSTPerfumes Firebase Credentials
const firebaseConfig = {
  apiKey: "AIzaSyAjh5nTdHyrBHxPxpNbDRhx7h7XIDfj-sM",
  authDomain: "mstperfumes.firebaseapp.com",
  projectId: "mstperfumes",
  storageBucket: "mstperfumes.firebasestorage.app",
  messagingSenderId: "903484852623",
  appId: "1:903484852623:web:f13d9faa835e017d24e3fa"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);

// ==========================================
// 1. AUTHENTICATION FUNCTIONS FOR WEBSITE
// ==========================================

// Register User on Website
export async function registerUser(email, password, fullName) {
  try {
    const userCredential = await createUserWithEmailAndPassword(auth, email, password);
    const user = userCredential.user;

    // Sync User Profile to shared Firestore 'users' collection
    await setDoc(doc(db, "users", user.email || user.uid), {
      id: Date.now(),
      name: fullName,
      email: email,
      status: "Verified",
      registeredDate: new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
    }, { merge: true });

    return { success: true, user };
  } catch (error) {
    return { success: false, error: error.message };
  }
}

// Login User on Website
export async function loginUser(email, password) {
  try {
    const userCredential = await signInWithEmailAndPassword(auth, email, password);
    return { success: true, user: userCredential.user };
  } catch (error) {
    return { success: false, error: error.message };
  }
}

// Google Single Sign-On for Website
export async function loginWithGoogle() {
  try {
    const provider = new GoogleAuthProvider();
    const result = await signInWithPopup(auth, provider);
    const user = result.user;

    // Sync Google user profile to shared Firestore 'users' collection
    const userKey = user.email || user.uid;
    await setDoc(doc(db, "users", userKey), {
      id: Date.now(),
      name: user.displayName || "Google Member",
      email: user.email || "",
      status: "Verified",
      registeredDate: new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
    }, { merge: true });

    return { success: true, user };
  } catch (error) {
    return { success: false, error: error.message };
  }
}

export async function registerWithGoogle() {
  try {
    const provider = new GoogleAuthProvider();
    const result = await signInWithPopup(auth, provider);
    const user = result.user;
    const userKey = user.email || user.uid;

    await setDoc(doc(db, "users", userKey), {
      id: Date.now(),
      name: user.displayName || "Google User",
      email: user.email || "",
      status: "Verified",
      registeredDate: new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
    }, { merge: true });

    return { success: true, user };
  } catch (error) {
    return { success: false, error: error.message };
  }
}

// Sign Out
export async function logoutUser() {
  await signOut(auth);
}

// ==========================================
// 2. FIRESTORE DATABASE HELPERS FOR WEBSITE
// ==========================================

// Fetch Shared Products
export async function getProducts() {
  const querySnapshot = await getDocs(collection(db, "products"));
  return querySnapshot.docs.map(doc => doc.data());
}

// Fetch Shared Customer Orders
export async function getOrders() {
  const querySnapshot = await getDocs(collection(db, "orders"));
  return querySnapshot.docs.map(doc => doc.data());
}

// Create Order on Website (Syncs to Mobile App!)
export async function placeOrder(orderData) {
  const orderCode = "B" + Math.floor(10000000 + Math.random() * 90000000) + "-01";
  const newOrder = {
    ...orderData,
    orderCode,
    status: "In Transit",
    orderDate: new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
  };
  await setDoc(doc(db, "orders", orderCode), newOrder);
  return newOrder;
}