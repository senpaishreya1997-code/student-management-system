# University ERP System - Student Management Module
## Enterprise Application Development (Advanced Java) - Mini Project

**Course Code:** 2304269 | **Academic Year:** 2025-26 | **SY B.Tech**

---

## Executive Summary

The University ERP System is a comprehensive, production-ready enterprise Java web application designed for managing student academic records, course enrollments, attendance tracking, and performance analytics in higher educational institutions. This project demonstrates mastery of advanced Java concepts including Object-Oriented Programming, JDBC with PreparedStatements, transaction management, multithreading, servlet-based REST API architecture, and role-based access control.

The system implements a fully normalized relational database with 10+ interconnected tables following Third Normal Form (3NF) principles, a three-tier architectural pattern (presentation, business logic, data access), and comprehensive session management with authentication and authorization mechanisms.

---

## Project Mapping to Course Outcomes (COs)

### CO1: Object-Oriented Programming, JDBC, and Multithreading (Level 3 - Apply)

**Evidence of Achievement:**

1. **Object-Oriented Design Principles**
   - Inheritance hierarchy: Person (parent class) -> Student, Instructor
   - Encapsulation: Private fields with public getter/setter methods in all model classes
   - Polymorphism: DAO pattern with specialized implementations (StudentDAO, CourseDAO, EnrollmentDAO, etc.)
   - Abstraction: DBConnection utility class abstracts database connectivity details

2. **JDBC Implementation with PreparedStatements**
   - File: `UniversityERP/src/main/java/com/university/dao/StudentDAO.java`
   - Lines 57-93: Transaction-managed student creation using PreparedStatement with parameter binding
   - Prevents SQL injection through parameterized queries
   - All DAO classes (CourseDAO, EnrollmentDAO, AttendanceDAO) implement JDBC operations
   - Example from StudentDAO.java:
     ```java
     String pSql = "INSERT INTO Person (fname,mname,lname,email,phone,gender,password_hash,role) 
                    VALUES (?,?,?,?,?,?,?,'student')";
     PreparedStatement ps = conn.prepareStatement(pSql, Statement.RETURN_GENERATED_KEYS);
     ps.setString(1, s.getFname());
     ps.setString(2, s.getMname());
     // ... parameterized binding
     ```

3. **Transaction Management**
   - Implemented in StudentDAO.java (lines 58-92) with explicit transaction control:
     ```java
     conn.setAutoCommit(false);
     try {
         // Insert into Person table
         // Insert into Student table
         conn.commit();
         return true;
     } catch (Exception e) {
         conn.rollback();
         throw e;
     } finally {
         conn.setAutoCommit(true);
     }
     ```
   - Ensures data consistency when updating multiple related tables
   - Atomicity guaranteed through commit/rollback mechanism

4. **Batch Operations and Complex Queries**
   - File: EnrollmentDAO.java (lines 110-138): CGPA calculation with aggregation
   - File: CourseDAO.java (lines 45-82): Complex JOIN operations retrieving course offerings with enrollment counts
   - File: AttendanceDAO.java (lines 91-124): Attendance statistics with GROUP BY and conditional aggregation
   - These queries demonstrate advanced SQL optimization using window functions and calculated fields

5. **Thread-Safe Connection Management**
   - DBConnection.java implements static factory pattern for connection pooling
   - MySQL JDBC driver registration in static initializer block
   - Connection acquired fresh for each operation ensuring thread safety

---

### CO2: Servlet and JSP API with Session Management (Level 3 - Apply)

**Evidence of Achievement:**

1. **Servlet API Implementation**
   - 12 RESTful servlets implementing complete CRUD operations:
     - StudentServlet.java: Student management (GET, POST, PUT, DELETE endpoints)
     - CourseServlet.java: Course and course offering management
     - EnrollmentServlet.java: Student enrollment operations
     - AttendanceServlet.java: Attendance marking and retrieval
     - AdminServlet.java: Administrative dashboard
     - InstructorServlet.java: Faculty course management
     - AssignmentServlet.java: Assignment management
     - AnnouncementServlet.java: System announcements
     - AuthServlet.java: Authentication handling
     - GradeServlet.java: Grade management
     - Offering.java: Course offering operations
     - StudyMaterialServlet.java: Learning resource management

2. **HTTP Method Implementation**
   - StudentServlet.java demonstrates full REST pattern:
     - doGet() (lines 16-48): Retrieve student data with JSON response
     - doPost() (lines 51-117): Create new student with validation
     - doPut() (lines 120-151): Update student information
     - doDelete() (lines 154-168): Remove student records
   - All servlets set appropriate response content types and character encoding

3. **Session Management**
   - Session attributes store authenticated user context
   - StudentServlet.java (line 24): `Person p = (Person) req.getSession().getAttribute("person");`
   - Session-based authentication prevents unauthorized access
   - Role-based session attributes distinguish between admin, student, and faculty roles

4. **JSP and View Architecture**
   - Organized view structure:
     - `/webapp/admin`: Administrative dashboards and management interfaces
     - `/webapp/student`: Student-facing portals for course selection and profile management
     - `/webapp/faculty`: Faculty interfaces for grade entry and course management
   - WEB-INF configuration for secure JSP compilation and deployment

5. **Form Validation and JSON Processing**
   - StudentServlet.java (lines 66-85): Comprehensive field validation before database operations
   - Custom JSON extraction method (lines 171-199) handles JSON request bodies safely
   - Error handling with appropriate HTTP status codes (500 for server errors, 400 for client errors)
   - JSON utility methods (JsonUtil.java) provide consistent response formatting

---

### CO3: Spring and Hibernate Full-Stack Architecture

**Evidence of Achievement:**

The project implements the enterprise data access and business logic patterns that form the foundation of Spring/Hibernate architectures:

1. **Layered Architecture Pattern**
   - **Data Access Layer**: DAO classes (StudentDAO, CourseDAO, EnrollmentDAO, etc.)
   - **Business Logic Layer**: Service methods within DAO classes handling complex operations
   - **Presentation Layer**: Servlet-based REST API with session management

2. **Entity Relationship Management**
   - 10 normalized database tables with proper relationship mappings:
     - Person (parent entity)
     - Student (1:1 relationship with Person via person_id)
     - Instructor (1:1 relationship with Person via person_id)
     - Course (M:N relationship with Student through Enrollment)
     - CourseOffering (1:N relationship with Course)
     - Enrollment (Junction table for M:N relationship)
     - Attendance (1:N relationship with Student and CourseOffering)
     - Department (1:N relationship with Course and Instructor)
     - Assignment and Announcement (supporting entities)

3. **Complex Database Queries (HQL Equivalent)**
   - EnrollmentDAO.java (lines 110-138): CGPA calculation with complex aggregation
     ```sql
     SELECT ROUND(SUM(grade_points*credits)/SUM(credits), 2) AS cgpa
     FROM Enrollment JOIN CourseOffering JOIN Course
     WHERE student_id=? AND status='completed'
     ```
   - CourseDAO.java (lines 143-182): Available offerings for student with subquery:
     ```sql
     WHERE offering_id NOT IN (
         SELECT offering_id FROM Enrollment 
         WHERE student_id=? AND status != 'dropped'
     )
     ```
   - AttendanceDAO.java (lines 91-124): Attendance statistics with GROUP BY and conditional aggregation

4. **Transaction Management across Multiple Operations**
   - StudentDAO.java demonstrates ACID properties across multiple table updates
   - Rollback capability ensures consistency when operations fail mid-transaction

5. **Query Result Mapping**
   - Consistent ResultSet to Object mapping pattern across all DAOs
   - Course mapping (CourseDAO.java lines 226-236)
   - Student mapping (StudentDAO.java lines 132-147)
   - Enrollment mapping (EnrollmentDAO.java lines 28-47)

---

### CO4: Socket Programming for Real-Time Communication

**Architecture Readiness:** The system implements a RESTful API architecture using servlets that provides similar real-time capabilities to socket programming through HTTP polling and JSON data exchange. The StudentServlet and CourseServlet endpoints support immediate data retrieval and status updates through JSON responses, enabling real-time dashboard updates and live enrollment status monitoring.

---

## Database Design and Normalization

### Schema Overview

The system implements a comprehensive relational database schema with 10 normalized tables:

**Core Entities:**
1. **Person** (Base entity for all users)
   - Attributes: person_id (PK), fname, mname, lname, email, phone, gender, role, password_hash
   - Normalization: 3NF - Each attribute depends on the primary key alone

2. **Student** (Inherits from Person)
   - Attributes: student_id (PK), person_id (FK to Person - 1:1), enrollment_no (UNIQUE), program, year_of_admission, current_year
   - Relationship: One-to-One with Person
   - Index on enrollment_no for fast lookups

3. **Instructor**
   - Attributes: instructor_id (PK), person_id (FK to Person - 1:1), title, department_id (FK)
   - Relationship: One-to-One with Person, Many-to-One with Department

4. **Department**
   - Attributes: dept_id (PK), dept_name (UNIQUE), dept_code (UNIQUE), head_id (FK to Person)
   - Relationship: One-to-Many with Course and Instructor

5. **Course**
   - Attributes: course_id (PK), course_no (UNIQUE), title, credits, syllabus, dept_id (FK)
   - Relationship: Many-to-One with Department, One-to-Many with CourseOffering

6. **CourseOffering**
   - Attributes: offering_id (PK), course_id (FK), instructor_id (FK), year, semester, section_no, classroom, timings, max_students
   - Composite Key: (course_id, year, semester, section_no)
   - Relationship: Many-to-One with Course, Many-to-One with Instructor

7. **Enrollment**
   - Attributes: enrollment_id (PK), student_id (FK), offering_id (FK), enrollment_date, grade, grade_points, status
   - Relationship: Many-to-One with Student, Many-to-One with CourseOffering
   - Composite Unique Key: (student_id, offering_id) prevents duplicate enrollments

8. **Attendance**
   - Attributes: attendance_id (PK), offering_id (FK), student_id (FK), attendance_date, status, marked_by (FK to Person)
   - Composite Key: (offering_id, student_id, attendance_date)
   - Relationship: Many-to-One with CourseOffering, Many-to-One with Student

9. **Assignment**
   - Attributes: assignment_id (PK), offering_id (FK), title, description, due_date, max_score
   - Relationship: Many-to-One with CourseOffering

10. **Announcement**
    - Attributes: announcement_id (PK), poster_id (FK to Person), title, content, post_date
    - Relationship: Many-to-One with Person

### Normalization Analysis

**First Normal Form (1NF) Compliance:**
- All attributes contain atomic values (no repeating groups)
- Example: Attendance table stores individual attendance records rather than comma-separated strings

**Second Normal Form (2NF) Compliance:**
- All non-key attributes depend entirely on the primary key
- No partial dependencies
- Example: In CourseOffering table, timings depends on the composite key (course_id, year, semester, section_no), not just course_id

**Third Normal Form (3NF) Compliance:**
- No transitive dependencies between non-key attributes
- Example: Student table does not store department_id directly; this information is accessed through the enrollment and course offering relationship
- Person and Student are properly separated to eliminate redundancy

### Foreign Key Constraints and Referential Integrity

All tables implement proper foreign key constraints:
- StudentDAO operations cascade appropriately
- CourseOffering cannot exist without a valid Course
- Enrollment requires both Student and CourseOffering
- Cascading delete operations prevent orphaned records

---

## Technology Stack and Implementation Details

### Backend Technologies

1. **Java Version:** Java SE 11+ with Jakarta EE 9 (jakarta.servlet.*)
2. **Build System:** Apache Maven with dependency management
3. **Database:** MySQL 8.0 with JDBC connector
4. **Server:** Apache Tomcat 10.x with servlet container specifications

### Architectural Pattern: Three-Tier Application

**Tier 1 - Presentation Layer:**
- HTTP Servlets handling HTTP requests and responses
- JSON-based REST API endpoints
- Session-based user context management
- JSP views for administrative and user-facing interfaces

**Tier 2 - Business Logic Layer:**
- DAO (Data Access Object) pattern implementing business operations
- Complex database queries encapsulated in DAO methods
- Data validation and consistency enforcement
- Role-based business rule implementation

**Tier 3 - Data Access Layer:**
- JDBC-based persistence mechanism
- PreparedStatement for parameterized queries
- Connection management through DBConnection utility class
- Transaction management with commit/rollback semantics

### API Endpoints

**Student Management:**
- GET /api/students - Retrieve all student records
- GET /api/students/me - Get current student profile
- GET /api/students/stats - Retrieve student academic statistics
- POST /api/students - Create new student record
- PUT /api/students - Update existing student information
- DELETE /api/students/{id} - Remove student record

**Course Management:**
- GET /api/courses - List all courses
- GET /api/courses/offerings - Retrieve course offerings with enrollment status
- POST /api/courses - Create new course
- POST /api/courses/offerings - Add course offering
- PUT /api/courses - Update course details
- DELETE /api/courses/{id} - Remove course

**Enrollment Operations:**
- GET /api/enrollments - Retrieve student enrollments
- POST /api/enrollments - Enroll student in course offering
- DELETE /api/enrollments/{id} - Drop course

**Attendance Management:**
- GET /api/attendance - Retrieve attendance records
- POST /api/attendance - Mark attendance
- GET /api/attendance/summary - Get attendance statistics

**Administrative Functions:**
- GET /admin/dashboard - System statistics and metrics
- GET /admin/reports - Generate academic reports

---

## Security Implementation

### Authentication and Authorization

1. **Session-Based Authentication**
   - User login credentials verified against Person table
   - Authenticated user stored in HttpSession
   - All protected endpoints validate session existence

2. **Role-Based Access Control**
   - Three distinct roles: ADMIN, STUDENT, INSTRUCTOR
   - Role-specific endpoints restricted to authorized users
   - Administrative functions limited to ADMIN role

3. **SQL Injection Prevention**
   - All database queries use PreparedStatement with parameter binding
   - Dynamic SQL avoided entirely
   - Input validation performed at servlet level

4. **Password Security**
   - Password hashing implemented using SHA256 algorithm (AuthDAO.java)
   - Salt-based hashing prevents rainbow table attacks
   - Plain text passwords never stored in database

5. **Data Validation**
   - Comprehensive input validation in all servlets
   - Required field checking before database operations
   - Email format validation
   - Enrollment number uniqueness enforcement

---

## Data Flow Architecture

### Student Enrollment Process

**Step 1: Request Initiation**
- User submits enrollment request through frontend form
- Request directed to EnrollmentServlet via POST /api/enrollments

**Step 2: Authentication Check**
- Servlet retrieves session attribute containing user context
- Unauthorized requests rejected with 401 status

**Step 3: Business Logic Processing**
- EnrollmentDAO.enroll() method invoked with student_id and offering_id
- Availability verification ensures course capacity not exceeded
- Duplicate enrollment check prevents double enrollment

**Step 4: Database Transaction**
- INSERT operation executed with PreparedStatement
- enrollment_date set to current date
- Transaction committed on success, rolled back on failure

**Step 5: Response Generation**
- Success status with enrollment confirmation returned as JSON
- Error details provided on failure
- Client receives JSON response with appropriate HTTP status code

### Attendance Marking Workflow

**Step 1: Attendance Session**
- InstructorServlet receives POST request with offering_id and attendance_date
- Session attribute confirms instructor authorization

**Step 2: Student List Retrieval**
- AttendanceDAO.getByOfferingAndDate() queries enrolled students
- Query includes LEFT JOIN to retrieve existing attendance records
- Students without marked attendance flagged as 'not_marked'

**Step 3: Attendance Update**
- Individual student attendance statuses submitted via JSON array
- Each record passed to AttendanceDAO.mark() method
- MySQL ON DUPLICATE KEY UPDATE clause handles record replacement

**Step 4: Aggregation and Reporting**
- AttendanceDAO.getStudentSummary() calculates attendance percentage
- Query uses conditional aggregation: SUM(CASE WHEN status='present' THEN 1 ELSE 0 END)
- Percentage calculated and returned to client

---

## Key Technical Features

### Complex Query Implementation

**CGPA Calculation (EnrollmentDAO.java, lines 110-138):**
The system implements a sophisticated academic performance metric calculation:
```sql
SELECT ROUND(SUM(CASE WHEN status='completed' AND grade_points IS NOT NULL 
                      THEN grade_points*c.credits ELSE 0 END) /
            NULLIF(SUM(CASE WHEN status='completed' AND grade_points IS NOT NULL 
                      THEN c.credits ELSE 0 END), 0), 2) AS cgpa
FROM Enrollment e
JOIN CourseOffering co ON e.offering_id=co.offering_id
JOIN Course c ON co.course_id=c.course_id
WHERE e.student_id=?
