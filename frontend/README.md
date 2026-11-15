# ShopSphere React Frontend 🛒

Modern e-commerce UI built with **React**, **Vite**, **Tailwind CSS**, and **Lucide Icons**.

---

## 🚀 How to Run the Frontend Locally

```bash
cd frontend
npm install
npm run dev
```

The application will start at:
👉 **`http://localhost:5173`**

---

## 🏗️ Architecture for Teammates (Feature-Based)

All microservices are accessed through the **API Gateway** on `http://localhost:8080`.
The shared `src/api/axiosClient.js` automatically attaches the authenticated user's JWT token to every request.

### Where Should You Build Your Screens?

| Your Microservice | Your UI Feature Folder | Files to Work In |
| :--- | :--- | :--- |
| **`user-service`** | `src/features/auth/` | `Login.jsx`, `Register.jsx`, `Profile.jsx` |
| **`product-service`** | `src/features/products/` | `ProductCatalog.jsx`, `ProductCard.jsx`, `ProductDetails.jsx` |
| **`cart-service`** | `src/features/cart/` | `CartDrawer.jsx`, `CartItem.jsx` |
| **`order-service`** | `src/features/orders/` | `Checkout.jsx`, `OrderHistory.jsx` |
| **`payment-service`** | `src/features/payments/` | `PaymentModal.jsx`, `PaymentSuccess.jsx` |

> **Team Rule**: To prevent git merge conflicts, only modify files inside **your** feature folder!
