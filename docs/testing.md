# MST Perfumes - Automated Testing & GitHub Actions CI/CD Strategy

> [!NOTE]
> This document outlines the automated testing infrastructure, unit test suites, and GitHub Actions CI/CD integration for the MST Perfumes Android application.

---

## 1. Automated Testing Strategy Overview

The testing suite ensures that all critical business logic, authentication flows, SQLite database operations, catalog management, support inquiry workflows, and notifications execute with 100% reliability both locally and on GitHub Actions CI.

| Test Suite Class | Test Engine | Key Business Functionality Verified |
| :--- | :--- | :--- |
| **`UserRepositoryTest`** | JUnit 4 | Active session management (`setActiveUser`, `getActiveUser`, `clearActiveSession`). |
| **`HelpRepositoryTest`** | JUnit 4 | FAQ categories, article retrieval, keyword search, and category filtering. |
| **`ProductRepositoryTest`** | JUnit 4 | Men's/women's fragrance filtering, adding new products, editing product details, and deleting inventory items. |
| **`DatabaseRepositoryTest`** | Robolectric | SQLite local user registration, password hashing verification, persistent user IDs (`getOrCreateUser`), order creation, order status updates, wishlist toggling, and support ticket lifecycle. |

---

## 2. Running Tests Locally

Run the following command in terminal to execute all automated unit tests:

```bash
./gradlew testDebugUnitTest
```

To build the debug APK:
```bash
./gradlew assembleDebug
```

---

## 3. GitHub Actions Continuous Integration (CI) Workflow

A GitHub Actions workflow configuration is established in `.github/workflows/android-ci.yml`.

### ⚡ Pipeline Execution Flow
1. **Trigger**: Automatically runs on every `push` or `pull_request` to GitHub.
2. **Environment**: Ubuntu Linux runner with **JDK 17**.
3. **Caching**: Caches Gradle dependencies for fast build times.
4. **Automated Testing**: Runs `./gradlew testDebugUnitTest --continue` to execute all 16 unit tests.
5. **APK Build**: Runs `./gradlew assembleDebug` to verify compilation.
6. **Artifact Storage**: Uploads unit test HTML reports and the compiled `app-debug.apk` to GitHub Actions for download.
