# 💰 Multi- Tenancy Leisure Management System



A full-stack web application for tracking with multi-user support, real-time analytics, and data visualization.



## ✨ Features



### 🔐 Authentication & Security

- User registration and login system

- Password encryption using BCrypt

- Multi-tenancy architecture (each user has private data)

- Secure session management with JWT tokens



### 📊 Dashboard

- Interactive charts showing monthly leisure spending trends

- Category-wise leisure expense breakdown (pie chart)

- Year-over-year spending comparison

- Calendar view with highlights

- Real-time KPI metrics



### 💵 Leisure Expense Management

- Add, edit, and delete expenses

- Categorize expenses (Food, Transport, Entertainment, etc.)

- Filter expenses by month and category

- Pagination support for large datasets



### 📈 Analytics & Reports

- **Summary Page**: Year-wise and month-wise expense breakdown

- Collapsible yearly sections with detailed monthly data

- Visual comparison charts across years

- Total spending calculations



### 🎯 Financial Planning

- Monthly income tracking

- Remaining budget calculation

- To-Do list with completion tracking

- Month-over-month financial leisure overview



### 🤖 AI Chatbot (Rule-Based NLP)

- **Intelligent query understanding** using weighted keyword matching + regex pattern detection

- **11 supported intents**: monthly totals, category analysis, month comparison, saving suggestions, top expenses, daily trends, spending summary, income status, greeting, help, and fallback

- **Context-aware responses** using real user data from the database

- **Dynamic suggestion pills** that update after each response

- **Chat history** (last 5 exchanges per user, in-memory)

- **Confidence scoring** — shows detected intent and match confidence

- **Zero external APIs** — no OpenAI, no paid services, pure Java logic

- Beautiful floating chat widget with typing indicator animation



## 🛠️ Tech Stack



### Backend

- **Java 17**

- **Spring Boot 4.0.1**

  - Spring Data JPA

  - Spring Security

  - Spring Web MVC

  - Spring Validation

- **MySQL** (Aiven Cloud Database)

- **Hibernate ORM**

- **Maven** for dependency management



### Frontend

- **HTML5** / **CSS3** / **JavaScript (Vanilla)**

- **Chart.js** for data visualization

- Responsive design with CSS Grid and Flexbox

- Dark/Light theme toggle



## 📦 Installation & Setup



### Prerequisites

- Java 17 or higher

- Maven 3.6+

- MySQL database (or Aiven account)



### Steps



1. **Clone the repository**

```bash

   git clone https://github.com/pragyaasharmaa/expense-tracker.git

   cd expense-tracker

```



2. **Configure Database**

   

   Copy the example properties file:

```bash

   cp src/main/resources/application.properties.example src/main/resources/application.properties

```

   

   Update `application.properties` with your database credentials:

```properties

   spring.datasource.url=jdbc:mysql://YOUR_HOST:PORT/defaultdb?sslMode=REQUIRED

   spring.datasource.username=YOUR_USERNAME

   spring.datasource.password=YOUR_PASSWORD

```



3. **Build the project**

```bash

   mvn clean install

```



4. **Run the application**

```bash

   mvn spring-boot:run

```



5. **Access the application**

   

   Open your browser and navigate to:

```

   http://localhost:8081

```



## 🚀 Usage



### First Time Setup

1. Click **"Sign Up"** to create a new account

2. Enter username, email, and password

3. Login with your credentials



### Managing Leisure Expenditures

1. Navigate to **Expenses** tab

2. Fill in expense details (title, amount, category, date)

3. Click **Add** to save

4. View all expenses in the table below

5. Filter by month/year or delete unwanted entries



### Tracking Income

1. In the **Dashboard**, find the "Monthly Income" KPI box

2. Enter your monthly income

3. The system automatically calculates remaining budget



### Viewing Summary

1. Go to **Summary** tab

2. See year-wise breakdown with collapsible sections

3. View year-over-year comparison chart



## 📁 Project Structure

```

Expense Tracker/

├── src/

│   ├── main/

│   │   ├── java/com/pragya/expensetracker/

│   │   │   ├── ai/               # 🤖 Chatbot NLP engine

│   │   │   │   ├── Intent.java           # Intent enum (11 intents)

│   │   │   │   ├── IntentResult.java      # Detection result DTO

│   │   │   │   ├── IntentDetector.java    # Keyword + regex scoring

│   │   │   │   ├── ResponseBuilder.java   # Template response generator

│   │   │   │   └── ChatHistoryManager.java # Per-user chat history

│   │   │   ├── config/           # Security configuration

│   │   │   ├── controller/       # REST API endpoints

│   │   │   ├── dto/              # Data Transfer Objects

│   │   │   ├── entity/           # JPA entities

│   │   │   ├── repository/       # Database repositories

│   │   │   ├── service/          # Business logic

│   │   │   └── exception/        # Error handling

│   │   └── resources/

│   │       ├── static/           # Frontend files (HTML, CSS, JS)

│   │       └── application.properties  # Config (not in Git)

│   └── test/                     # Unit tests

├── pom.xml                       # Maven dependencies

└── README.md

```



## 🔒 Security Features



- **Password Hashing**: BCrypt encryption for all passwords

- **Data Isolation**: Each user can only access their own data

- **SQL Injection Prevention**: JPA/Hibernate with prepared statements

- **CSRF Protection**: Disabled for REST API (can be enabled for production)

- **Environment Variables**: Sensitive data excluded from version control



## 🌐 API Endpoints



### Authentication

- `POST /auth/register` - Register new user

- `POST /auth/login` - User login



### Expenses

- `GET /expenses` - Get all expenses (paginated, filtered)

- `POST /expenses` - Add new expense

- `PUT /expenses/{id}` - Update expense

- `DELETE /expenses/{id}` - Delete expense

- `GET /expenses/summary` - Get expense summary



### KPIs

- `GET /kpi/income` - Get monthly income

- `POST /kpi/income` - Save monthly income

- `GET /kpi/todos` - Get planned expenses

- `POST /kpi/todos` - Add planned expense

- `PUT /kpi/todos/{id}/toggle` - Toggle completion status



### 🤖 AI Chatbot

- `POST /api/chat` - Send a message and get AI-like response

- `POST /api/chat/reset?username={username}` - Clear chat history

- `GET /api/chat/history?username={username}` - Get chat history



**Example Request:**

```json

POST /api/chat

{

  "username": "pragya",

  "message": "How much did I spend this month?"

}

```



**Example Response:**

```json

{

  "reply": "💰 In May 2026, you spent ₹7,099.00 across 5 transactions...",

  "detectedIntent": "Monthly Total Spending",

  "confidence": 0.7,

  "suggestions": ["Show my category breakdown", "Compare with last month"]

}

```



## 🎨 Features Highlights



### Multi-User Support

Each user has completely isolated data - no user can see another user's expenses, income, or planned expenses.



### Real-Time Analytics

Charts and KPIs update instantly when you add/delete expenses.



### Responsive Calendar

Click on any date in the calendar to see expenses for that day in a modal popup.



### Theme Toggle

Switch between dark and light modes for comfortable viewing.



### Planned Features

- [x] AI Chatbot with NLP-based intent detection

- [ ] Export data to Excel/PDF

- [ ] Mobile responsive design

- [ ] Budget alerts and notifications

- [ ] Recurring expenses

- [ ] Multi-currency support

- [ ] Profile page with password reset

- [ ] Email verification

- [ ] Forgot password functionality



## 🤝 Contributing



Contributions are welcome! Please feel free to submit a Pull Request.



## 📄 License



This project is open source and available under the [MIT License](LICENSE).



## 👤 Author



**Pragya Sharma**

- GitHub: [@pragyaasharmaa](https://github.com/pragyaasharmaa)



## 🙏 Acknowledgments



- Spring Boot documentation

- Chart.js for beautiful charts

- Aiven for cloud database hosting

- Claude AI for development assistance



---



**⭐ If you found this project helpful, please consider giving it a star!**
