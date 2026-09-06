# Student Result Management System

A secure, enterprise-oriented **Student Result Management System** built
with Java, Jakarta Servlet/JSP, PostgreSQL, and Apache Tomcat. The
system manages the complete academic result lifecycle from student and
subject management through marks entry, verification, approval, and
publication.

The project focuses specifically on **student result management**, while
preserving the existing working functionality and adding stronger role
management, authentication, two-factor authentication, auditability,
academic structure, approval workflows, and secure database access.

## 🎯 Objectives

-   Centralize academic result management.
-   Reduce manual result-processing errors.
-   Provide role-based access control.
-   Implement a controlled result approval lifecycle.
-   Improve authentication and application security.
-   Maintain an audit trail for important actions.
-   Provide students with access to published results.
-   Maintain structured relationships between departments, programs,
    semesters, subjects, faculty, and students.
-   Support cloud deployment using AWS.

## 🏗️ Architecture

``` text
┌───────────────────────────────────────────────┐
│              Presentation Layer               │
│                 JSP / HTML / CSS              │
└───────────────────────┬───────────────────────┘
                        │
┌───────────────────────▼───────────────────────┐
│                Controller Layer               │
│             Jakarta Servlets                 │
└───────────────────────┬───────────────────────┘
                        │
┌───────────────────────▼───────────────────────┐
│                  Service Layer                │
│       Business Logic / Validation             │
└───────────────────────┬───────────────────────┘
                        │
┌───────────────────────▼───────────────────────┐
│                    DAO Layer                  │
│       JDBC / PreparedStatement                │
└───────────────────────┬───────────────────────┘
                        │
┌───────────────────────▼───────────────────────┐
│                 PostgreSQL DB                 │
│              enterprise schema               │
└───────────────────────────────────────────────┘
```

The application uses a layered architecture consisting of the
presentation, controller, service, DAO, and database layers.

## 👥 User Roles

  Role            Responsibility
  --------------- ----------------------------------------------------
  `SUPER_ADMIN`   System-level administrative role
  `ADMIN`         Creates and manages privileged accounts
  `HOD`           Manages department activities and verifies results
  `FACULTY`       Manages students, subjects, marks, and submissions
  `EXAM_CELL`     Analyzes, approves, and publishes results
  `STUDENT`       Views published academic results

Role-based access prevents users from accessing functionality outside
their assigned responsibilities.

## 🔄 Result Lifecycle

``` text
Faculty enters marks
        │
        ▼
      DRAFT
        │
        │ Submit
        ▼
    SUBMITTED
        │
        │ HOD verifies
        ▼
  HOD_VERIFIED
        │
        │ Exam Cell approves
        ▼
EXAM_CELL_APPROVED
        │
        │ Publish
        ▼
    PUBLISHED
        │
        ▼
Student views result
```

### Rejection Flow

``` text
SUBMITTED
    │
    │ HOD rejects
    ▼
  DRAFT
    │
    │ Faculty edits
    ▼
SUBMITTED
```

Rejection remarks are maintained as part of the workflow.

## 📚 Academic Structure

The system maintains a structured academic model:

``` text
Department
    │
    └── Program
          │
          └── Semester
                │
                └── Subject Offering
                      │
                      ├── Subject
                      └── Academic Year

Student
    │
    └── Academic Details

Faculty
    │
    └── Faculty Subject Assignment
          │
          └── Subject Offering

Subject Offering
    │
    └── Marks
          │
          └── Result
```

## 🗄️ Database

The system uses **PostgreSQL** with the primary schema:

``` text
enterprise
```

Major tables include:

-   `users`
-   `roles`
-   `permissions`
-   `user_roles`
-   `role_permissions`
-   `students`
-   `student_address`
-   `faculty`
-   `departments`
-   `programs`
-   `academic_years`
-   `semesters`
-   `academic_details`
-   `subjects`
-   `subject_offerings`
-   `faculty_subject`
-   `marks`
-   `result`
-   `result_approval`
-   `examinations`
-   `audit_logs`

The database design emphasizes relational integrity, academic
relationships, workflow tracking, faculty allocation, and auditability.

## 🔐 Security

### Password Security

Passwords are stored using **BCrypt hashing** rather than plaintext
passwords.

``` text
Password
   │
   ▼
BCrypt Hash
   │
   ▼
Database
```

### Two-Factor Authentication

TOTP-based two-factor authentication is used for privileged accounts:

-   ADMIN
-   FACULTY
-   HOD
-   EXAM_CELL

Students do not use the privileged-user 2FA flow.

### TOTP Secret Encryption

TOTP secrets are encrypted at rest using **AES-256-GCM**. The encryption
key is supplied through an environment variable.

The existing encryption key must be preserved when migrating a database
containing encrypted TOTP secrets.

### Additional Security Controls

-   Role-based access control
-   Session timeout
-   Secure session handling
-   Login attempt protection
-   Account lockout handling
-   Account status control
-   CSRF protection
-   PreparedStatement
-   Input validation
-   Audit logging
-   Access-control checks
-   Environment-based database configuration

## 🔑 Authentication Flow

``` text
Login Page
    │
    ▼
Username + Password
    │
    ▼
User Lookup
    │
    ▼
Account Status Check
    │
    ▼
BCrypt Verification
    │
    ├── Invalid → Login Error
    │
    ▼
Role / First Login / 2FA Check
    │
    ▼
Role-Specific Dashboard
```

### First Login

``` text
Username + Password
        │
        ▼
Password Reset
        │
        ▼
2FA Setup
        │
        ▼
Authenticator OTP
        │
        ▼
2FA Enabled
        │
        ▼
Dashboard
```

## 👨‍🏫 Faculty Features

Faculty functionality includes:

-   Add Student
-   Add Subject
-   Add Marks
-   Result Management
-   View assigned students
-   View assigned subjects
-   Submit results for verification
-   Edit results returned to draft status
-   Logout

Faculty can work with students and subjects associated with their active
assignments.

## 🧑‍💼 HOD Features

HOD functionality includes:

-   HOD profile
-   Department summary
-   Department faculty
-   Faculty subject assignment
-   Faculty subject allocation
-   Department performance
-   Pending result verification
-   Verify result
-   Reject result with remarks
-   Logout

## 🏢 Admin Features

The Admin portal provides controlled creation and management of
privileged accounts.

Admin can create:

-   Faculty accounts
-   HOD accounts
-   Exam Cell accounts

The account-management functionality supports:

-   Viewing managed accounts
-   Deactivating Faculty accounts
-   Deactivating HOD accounts
-   Deactivating Exam Cell accounts
-   TOTP verification before deactivation
-   Audit logging

Deactivation preserves historical data instead of deleting the account's
academic history.

## 📝 Subject and Faculty Allocation

The subject workflow connects subjects, subject offerings, academic
structure, and faculty assignments.

``` text
Create Subject
     │
     ▼
Create Subject Offering
     │
     ▼
Assign Faculty
     │
     ▼
Faculty sees Subject
     │
     ▼
Faculty enters Marks
```

Related database operations are handled transactionally where required
to maintain consistency.

## 📊 Result Processing

The Exam Cell can analyze department results using the configured
result-processing workflow, including Bell Curve-based analysis where
applicable.

``` text
Marks
  │
  ▼
Result Processing
  │
  ▼
Department Analysis
  │
  ▼
Exam Cell Approval
  │
  ▼
Publication
```

Only published results are intended to be available to students.

## 🧾 Audit Logging

Important system actions are recorded in `audit_logs`.

Audit information can include:

-   User ID
-   Action
-   Entity type
-   Entity ID
-   Previous value
-   New value
-   Remarks
-   Timestamp

Examples include subject assignment, result verification, result
rejection, and account deactivation.

## 🛠️ Technology Stack

  Technology              Purpose
  ----------------------- ---------------------------
  Java 21                 Application development
  Jakarta Servlet         Web controllers
  JSP                     Server-side presentation
  HTML5                   User interface
  CSS3                    Styling
  JavaScript              Client-side functionality
  PostgreSQL              Relational database
  JDBC                    Database connectivity
  BCrypt                  Password hashing
  Apache Tomcat 10        Application server
  Eclipse IDE             Development
  AWS Elastic Beanstalk   Cloud deployment
  AWS RDS PostgreSQL      Cloud database

## 📁 Project Structure

``` text
StudentResultManagement/
│
├── src/
│   └── main/
│       ├── java/
│       │   ├── dao/
│       │   ├── filter/
│       │   ├── model/
│       │   ├── service/
│       │   ├── servlet/
│       │   └── util/
│       │
│       └── webapp/
│           ├── WEB-INF/
│           ├── css/
│           ├── js/
│           ├── images/
│           ├── login.jsp
│           ├── adminLogin.jsp
│           ├── adminDashboard.jsp
│           ├── teacherDashboard.jsp
│           ├── hodDashboard.jsp
│           ├── studentDashboard.jsp
│           └── ...
│
└── WEB-INF/lib/
    ├── jbcrypt-0.4.jar
    └── postgresql JDBC driver
```

## ⚙️ Requirements

### Local Development

-   Java JDK 21
-   Eclipse IDE
-   Apache Tomcat 10.1.x
-   PostgreSQL
-   PostgreSQL JDBC Driver
-   jBCrypt 0.4

### AWS Deployment

-   AWS account
-   AWS Elastic Beanstalk
-   AWS RDS PostgreSQL
-   Appropriate AWS security groups

## 🚀 Local Setup

### 1. Clone the Repository

``` bash
git clone https://github.com/<your-username>/StudentResultManagement.git
cd StudentResultManagement
```

### 2. Create the Database

``` sql
CREATE DATABASE Student_Result_DB;
```

Restore or create the required `enterprise` schema and tables.

### 3. Configure PostgreSQL

For local development, configure the application for your local
PostgreSQL instance.

Example:

``` text
jdbc:postgresql://localhost:5432/Student_Result_DB
```

Do not commit real database credentials to GitHub.

### 4. Configure Eclipse

1.  Import the project as a Dynamic Web Project.
2.  Configure JDK 21.
3.  Configure Apache Tomcat 10.1.
4.  Add the PostgreSQL JDBC driver.
5.  Add jBCrypt.
6.  Clean and build the project.
7.  Run the project on Tomcat.

### 5. Open the Application

``` text
http://localhost:8080/StudentResultManagement/login.jsp
```

## ☁️ AWS Deployment

The application is designed for deployment using AWS Elastic Beanstalk
and Amazon RDS PostgreSQL.

``` text
                    AWS
                     │
        ┌────────────┴────────────┐
        │                         │
Elastic Beanstalk             RDS PostgreSQL
        │                         │
   Tomcat 10                  Student_Result_DB
   Corretto 21                     │
        │                           │
        └──────── JDBC ─────────────┘
```

### Elastic Beanstalk Platform

``` text
Tomcat 10
Corretto 21
Amazon Linux 2023
64-bit
```

### Environment Variables

Configure the following environment properties in Elastic Beanstalk:

``` text
DB_URL
DB_USERNAME
DB_PASSWORD
TWO_FACTOR_ENCRYPTION_KEY
```

Example:

``` text
DB_URL=jdbc:postgresql://<RDS-ENDPOINT>:5432/Student_Result_DB?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=<RDS_PASSWORD>
TWO_FACTOR_ENCRYPTION_KEY=<EXISTING_ENCRYPTION_KEY>
```

Never commit these values to GitHub.

## 🔒 AWS Network Security

The RDS security group should allow PostgreSQL access from the Elastic
Beanstalk application security group.

Recommended rule:

``` text
Type: PostgreSQL
Protocol: TCP
Port: 5432
Source: Elastic Beanstalk Security Group
```

Do not expose PostgreSQL to the entire internet:

``` text
0.0.0.0/0
```

For production, keep database access restricted to trusted application
resources.

## 📦 WAR Deployment

The application is deployed to Elastic Beanstalk as a WAR file.

### Export from Eclipse

``` text
Right-click Project
        ↓
Export
        ↓
WAR file
        ↓
Select destination
        ↓
Finish
```

### Deploy

``` text
Elastic Beanstalk
      ↓
Application
      ↓
Environment
      ↓
Upload and deploy
      ↓
Select WAR
      ↓
Deploy
```

After deployment, verify:

-   Environment status is `Ready`
-   Health is `OK`
-   Login page loads
-   RDS connectivity works
-   Authentication works
-   2FA works for configured privileged accounts
-   Dashboards work according to role

## 🔧 Database Configuration Strategy

The application can use AWS environment variables while retaining a
local database fallback.

``` text
                    DBConnection
                         │
             ┌───────────┴───────────┐
             │                       │
       DB_URL exists?          DB_URL missing?
             │                       │
             ▼                       ▼
        AWS RDS Database       Local PostgreSQL
```

This allows the same project to be used during local development and AWS
deployment without changing the existing application workflow.

## 🧪 Testing Checklist

### Authentication

-   [ ] Valid login
-   [ ] Invalid username
-   [ ] Invalid password
-   [ ] Inactive account
-   [ ] First-login password setup
-   [ ] 2FA setup
-   [ ] Valid OTP
-   [ ] Invalid OTP
-   [ ] Session timeout
-   [ ] Logout

### Faculty

-   [ ] Add student
-   [ ] Add subject
-   [ ] View assigned students
-   [ ] View assigned subjects
-   [ ] Add marks
-   [ ] Submit result
-   [ ] Edit rejected result

### HOD

-   [ ] View department information
-   [ ] View department faculty
-   [ ] Assign faculty to subject
-   [ ] View pending results
-   [ ] Verify result
-   [ ] Reject result
-   [ ] Add remarks

### Exam Cell

-   [ ] View results
-   [ ] Analyze department performance
-   [ ] Approve results
-   [ ] Publish results

### Student

-   [ ] Login
-   [ ] View published result
-   [ ] Prevent access to unpublished results

### Admin

-   [ ] Create Faculty
-   [ ] Create HOD
-   [ ] Create Exam Cell account
-   [ ] View managed accounts
-   [ ] Deactivate account
-   [ ] Verify target-account OTP
-   [ ] Record audit event

## 🛡️ Production Security Recommendations

1.  Never commit passwords to GitHub.
2.  Never commit AWS credentials or encryption keys.
3.  Use environment variables or AWS Secrets Manager for secrets.
4.  Restrict RDS security-group access.
5.  Keep PostgreSQL private whenever practical.
6.  Use HTTPS for production access.
7.  Keep Java, Tomcat, PostgreSQL, and dependencies updated.
8.  Maintain regular database backups.
9.  Monitor application and database logs.
10. Rotate credentials if they are exposed.
11. Use strong administrative passwords.
12. Protect the 2FA encryption key.
13. Keep authentication and authorization checks enabled.

## 📈 Enterprise-Oriented Capabilities

### Role-Based Access Control

Users receive functionality according to their assigned roles.

### Result Workflow

Results move through defined lifecycle states before publication.

### Auditability

Important operations are recorded for accountability.

### Data Integrity

Academic entities are connected through relational database
relationships.

### Transaction Management

Related database operations can be executed transactionally where
required.

### Secure Authentication

BCrypt password hashing, session management, login-attempt protection,
account status controls, and TOTP-based 2FA strengthen authentication.

### Cloud Deployment

The system supports AWS Elastic Beanstalk with PostgreSQL hosted on
Amazon RDS.

## 🗺️ Future Enhancements

Potential enhancements include:

-   HTTPS with a custom domain
-   AWS Secrets Manager integration
-   Centralized application logging
-   Advanced audit reporting
-   Email notifications
-   Result PDF generation
-   Transcript generation
-   Advanced analytics
-   Automated testing
-   CI/CD pipeline
-   CloudWatch monitoring and alerts
-   Backup and disaster recovery automation
-   Fine-grained permission management

These can be introduced while retaining the core result-management
workflow.

## 📜 License

Add the license selected by the repository owner.

For an academic repository, you may also include an academic-project
notice describing permitted use.

## 👨‍💻 Author

**Dharun Kumar S**

Student Result Management System

**Java \| Jakarta Servlet/JSP \| PostgreSQL \| Apache Tomcat \| AWS**

## ⭐ Project Highlights

``` text
✔ Enterprise-oriented Result Management
✔ Role-Based Access Control
✔ Faculty Subject Allocation
✔ Result Verification Workflow
✔ HOD Verification
✔ Exam Cell Approval
✔ Result Publication
✔ BCrypt Password Hashing
✔ TOTP-Based Two-Factor Authentication
✔ AES-256-GCM TOTP Secret Protection
✔ Login Attempt Protection
✔ Session Management
✔ Account Activation / Deactivation
✔ Audit Logging
✔ PostgreSQL Relational Database
✔ Layered Architecture
✔ AWS Elastic Beanstalk Deployment
✔ AWS RDS PostgreSQL Support
```

## 📌 Scope

This project focuses on **Student Result Management** and is not
intended to be a complete College ERP.

The primary lifecycle is:

``` text
Student Registration
        ↓
Academic Structure
        ↓
Subject Management
        ↓
Faculty Allocation
        ↓
Marks Entry
        ↓
Result Processing
        ↓
HOD Verification
        ↓
Exam Cell Approval
        ↓
Result Publication
        ↓
Student Result Viewing
```
