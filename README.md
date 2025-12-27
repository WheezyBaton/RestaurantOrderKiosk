# 🍔 Restaurant Order Kiosk System

A comprehensive self-service kiosk solution for restaurants, built with **Java** and **Spring Boot**. The system includes a customer-facing ordering interface, a Kitchen Display System (KDS) for staff, and a robust Administration Dashboard for menu and sales management.

![License](https://img.shields.io/badge/License-MIT-green.svg)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-green)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)

## 📖 Overview

The **Restaurant Order Kiosk** allows customers to place orders (Eat-in or Take-away) via a touch-friendly interface. It supports complex product configurations (adding/removing ingredients), cart management, and order tracking.

The system is designed to streamline restaurant operations by connecting the front-of-house (Kiosk) directly with the back-of-house (Kitchen) and providing management with real-time insights (Admin).

## ✨ Key Features

### 🧑‍💻 Customer Kiosk
* **Order Type Selection:** Choose between "Eat In" 🍽️ or "Take Away" 🥡 (adds packaging fee).
* **Visual Menu:** Browse products by categories (Burgers, Sides, Drinks).
* **Product Customization:** Add or remove ingredients (e.g., extra bacon, no onions) with dynamic price calculation.
* **Smart Cart:** Manage items, quantities, and review order summaries.
* **Real-time Status Board:** Monitor order status (In Progress / Ready) on a dedicated public display.

### 👨‍🍳 Kitchen Display System (KDS)
* **Live Order View:** View incoming orders instantly with detailed modification lists.
* **Workflow Management:** Move orders from "New" → "Ready" → "Completed".
* **Separation:** Separate views for "To Prepare" and "Ready for Pickup".

### ⚙️ Admin Dashboard
* **Sales Analytics:** View daily/monthly revenue and order counts.
* **Menu Management:** full CRUD for Products and Ingredients.
* **Image Management:** Upload product images directly from the dashboard.
* **Availability Toggle:** Quickly hide/show products or ingredients if out of stock.
* **Reports:** Export sales data to CSV.

## 🛠️ Tech Stack

* **Backend:** Java 17, Spring Boot 3 (Web, Data JPA, Security, Validation).
* **Frontend:** Thymeleaf, Bootstrap 5.
* **Database:** PostgreSQL (Production/Docker), H2 (Testing).
* **Containerization:** Docker & Docker Compose.
* **API Documentation:** OpenAPI / Swagger UI.
* **Tools:** Maven, Lombok.

---

## 🚀 Getting Started

### Prerequisites
* Docker & Docker Compose (Recommended)
* **OR** Java JDK 17+ and Maven (for local manual run)

### Run with Docker
This method sets up both the application and the PostgreSQL database automatically.

1.  Clone the repository:
    ```bash
    git clone https://github.com/your-username/restaurant-order-kiosk.git
    cd restaurant-order-kiosk/kiosk-system
    ```

2.  Build and run containers:
    ```bash
    docker-compose up --build
    ```

3.  Access the application at `http://localhost:8080`.

---

## 🔐 Access & Credentials

The application comes with pre-configured users for different roles (defined in `SecurityConfig.java`):

| Role | URL | Username | Password | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | `/admin` | `admin` | `admin` | Full access to dashboard and settings. |
| **Kitchen** | `/kitchen` | `kitchen` | `kitchen` | Access to KDS panel only. |
| **Customer** | `/` | *(No Auth)* | - | Public kiosk interface. |
| **Board** | `/board` | *(No Auth)* | - | Public order status display. |

---

## 📂 Project Structure

```text
src
├── main
│   ├── java/com/wheezybaton/kiosk_system
│   │   ├── KioskSystemApplication.java      # Main entry point
│   │   ├── config/                          # Configuration classes
│   │   │   ├── DataInitializer.java         # Initial data loader
│   │   │   ├── OpenApiConfig.java           # Swagger/OpenAPI setup
│   │   │   ├── SecurityConfig.java          # Spring Security roles & access
│   │   │   └── WebConfig.java               # Web MVC config (resource handlers)
│   │   ├── controller/                      # Request handlers
│   │   │   ├── AdminController.java         # Back-office management
│   │   │   ├── IngredientController.java    # Ingredient management
│   │   │   ├── KioskController.java         # Main customer interface
│   │   │   ├── KitchenController.java       # Kitchen display system
│   │   │   └── ProductRestController.java   # REST API for products
│   │   ├── dto/                             # Data Transfer Objects
│   │   │   ├── CartItemDto.java
│   │   │   ├── CreateProductRequest.java
│   │   │   ├── ProductDto.java
│   │   │   ├── ProductIngredientDto.java
│   │   │   └── SalesStatDto.java
│   │   ├── exception/                       # Global exception handling
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── model/                           # Database Entities (JPA)
│   │   │   ├── CartSession.java             # Session-scoped cart bean
│   │   │   ├── Category.java
│   │   │   ├── Ingredient.java
│   │   │   ├── ModifierAction.java          # Enum (ADDED/REMOVED)
│   │   │   ├── Order.java
│   │   │   ├── OrderItem.java
│   │   │   ├── OrderItemModifier.java
│   │   │   ├── OrderStatus.java             # Enum
│   │   │   ├── OrderType.java               # Enum (EAT_IN/TAKE_AWAY)
│   │   │   ├── Product.java
│   │   │   └── ProductIngredient.java       # Many-to-Many link with config
│   │   ├── repository/                      # Data Access Layer
│   │   │   ├── CategoryRepository.java
│   │   │   ├── IngredientRepository.java
│   │   │   ├── OrderRepository.java
│   │   │   ├── ProductIngredientRepository.java
│   │   │   └── ProductRepository.java
│   │   └── service/                         # Business Logic
│   │       ├── CartService.java             # Cart logic
│   │       ├── OrderService.java            # Order processing
│   │       ├── ProductService.java          # Product management
│   │       └── StatsService.java            # Reporting & Statistics
│   └── resources
│       ├── application.yml                  # App configuration
│       ├── data-h2.sql                      # Seed data for dev
│       ├── data-postgresql.sql              # Seed data for prod
│       └── templates/                       # Thymeleaf views
│           ├── admin/                       # Admin pages
│           │   ├── dashboard.html
│           │   ├── ingredient-form.html
│           │   ├── ingredients.html
│           │   └── product-form.html
│           ├── fragments/                   # Reusable layouts
│           │   └── layout.html
│           ├── board.html                   # Public order status board
│           ├── checkout.html
│           ├── configure.html               # Product customizer
│           ├── kitchen.html                 # KDS view
│           ├── menu.html
│           ├── success.html
│           └── welcome.html
└── test/java/com/wheezybaton/kiosk_system   # Unit & Integration Tests
    ├── KioskSystemApplicationTests.java     # Context load test
    ├── SimplePojoTest.java                  # Getter/Setter POJO tests
    ├── config/
    │   └── DataInitializerTest.java
    ├── controller/
    │   ├── AdminControllerTest.java
    │   ├── IngredientControllerTest.java
    │   ├── KioskControllerTest.java
    │   ├── KitchenControllerTest.java
    │   └── ProductRestControllerTest.java
    ├── model/
    │   └── CartSessionTest.java
    ├── repository/
    │   ├── CategoryRepositoryTest.java
    │   ├── IngredientRepositoryTest.java
    │   ├── OrderRepositoryTest.java
    │   └── ProductRepositoryTest.java
    └── service/
        ├── CartServiceTest.java
        ├── OrderServiceTest.java
        ├── ProductServiceTest.java
        ├── StatsServiceIntegrationTest.java
        └── StatsServiceTest.java	
```
---

## 📡 API Documentation

The project exposes a REST API for product management.
Once the application is running, you can access the Swagger UI documentation here:

👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

## 📄 License

Distributed under the MIT License. See [MIT License](https://opensource.org/licenses/MIT) for more information.
