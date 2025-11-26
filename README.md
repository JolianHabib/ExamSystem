# Exam System (Java + PostgreSQL)

A complete Exam Management System built in Java using OOP, JDBC, and PostgreSQL.
The system manages teachers, subjects, questions, answers, and full exam creation through a menu-based console interface.

---

## 🚀 Features
- Manage **Teachers** (Add, Update, Delete, View)
- Manage **Subjects**
- Add and view **Open Questions** and **Selection Questions**
- Manage **Answers** for each question
- Link questions to subjects and answers to questions
- Create full **Exams** and attach questions to each exam
- PostgreSQL database integration (JDBC)
- Clean OOP architecture and modular design

---

## 🗂 Project Structure
src/
├─ Main.java
├─ DBConnection.java
├─ Exam.java
├─ ExamManager.java
├─ Teacher.java
├─ TeacherManager.java
├─ Subject.java
├─ SubjectPool.java
├─ QuestionPool.java
├─ AnswerPool.java
├─ OpenQuestion.java
├─ SelectionQuestion.java
├─ QuestionAnswerLinker.java
└─ module-info.java

EXAMDB.sql



---

## 🧩 Technologies Used
- Java 15
- PostgreSQL
- JDBC
- SQL (CRUD operations)
- OOP principles

---

## 🗄 Database Overview
Main tables include:

- **Teacher**
- **Subject**
- **Question**
- **Answer**
- **Exam**
- **ExamQuestion**
- **QuestionAnswer**

All tables use foreign keys to maintain relational consistency across the system.

---

## ▶️ How to Run
1. Import the project into **Eclipse** or **IntelliJ**.
2. Execute the `EXAMDB.sql` file in PostgreSQL.
3. Update DB credentials in:
DBConnection.java
4. Run the project from:
Main.java

---

## 📌 Notes
- Ensure the PostgreSQL JDBC driver is added to your project’s Build Path.
- The project is modular and scalable, suitable for academic or production-level development.

---

## 👤 Author
**Jolian Habib**  
Afeka College — Software Engineering  
GitHub: https://github.com/JolianHabib

