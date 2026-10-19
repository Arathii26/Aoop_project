# Lost & Found Management System

This project is a complete Java OOP application for a university lost and found desk. It allows students to report lost or found items, matches them automatically, creates claims, and lets an admin approve or reject those claims.

## Features
- Student and admin user hierarchy
- Lost/found item tracking
- Automatic match scoring using category, location, time, and keyword overlap
- Notification support with console and email-style adapters
- SQL-based persistence with MySQL and JDBC
- File-based backup repository
- REST APIs with Spring Boot
- JUnit tests covering key behavior

## Technology Stack
- Java 21+
- Maven
- Spring Boot 3
- MySQL 8
- JDBC PreparedStatement
- JUnit 5

## OOP concepts used
- Encapsulation in model classes
- Inheritance: Student/Admin extends User, LostItem/FoundItem extends Item
- Abstraction through abstract base classes and methods
- Polymorphism through item and notifier implementations
- Method overriding in summaries and role methods
- Constructor overloading in models and services

## Design patterns used
- Singleton: DataStore
- Factory: NotifierFactory
- Adapter: EmailNotifier adapts a mock mail sender interface

## Matching score formula
The system calculates a total out of 100:
- Category match: 40
- Location match: 25
- Time proximity: 20
- Keyword overlap: 15

Time score is based on the difference between lost and found time reports and gives higher scores to closer reports. Keyword overlap uses normalized tokens and a HashSet intersection to keep the logic simple and explainable.

## Database setup
1. Create a MySQL database named lost_found_db.
2. Run the SQL in schema.sql.
3. Set environment variables:
   - DB_URL
   - DB_USERNAME
   - DB_PASSWORD
4. Confirm the application.properties file reads the values correctly.

Example:
```
set DB_URL=jdbc:mysql://localhost:3306/lost_found_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set DB_USERNAME=root
set DB_PASSWORD=your_password
```

## Java and Maven setup
Install Java 21 or newer and Maven.

Check versions:
```
java -version
mvn -version
```

Run the project:
```
mvn spring-boot:run
```

## How to run tests
```
mvn test
```

## API endpoints
- POST /lost
- POST /found
- GET /matches/{id}
- POST /claims
- PUT /claims/{id}/approve
- PUT /claims/{id}/reject

## Postman demo
Import the collection at postman/Lost-Found-API.postman_collection.json and follow the steps below:
1. Create Student A and Student B
2. Report a lost calculator by Student A in Block A at 2 PM
3. Report a found calculator by Student B in Block A at 3 PM
4. Retrieve matches
5. Create a claim
6. Approve the claim as Admin
7. Check that the claim is approved and both items are resolved

## Known limitations
- This is a classroom project and not production-grade authentication
- MySQL database access depends on a running local database
- Some repository methods are simplified for a college-level implementation

## Four-member contribution division
- Member 1: Models, enums, exceptions, repositories, schema
- Member 2: MatchResult, MatchEngine, ItemService
- Member 3: Notifications, ClaimService, async logging
- Member 4: Spring Boot, controllers, API integration, README, tests

## Viva notes
This project demonstrates a clean separation between model, repository, matching, notification, and controller layers. The logic is intentionally readable so it can be explained clearly during a viva or classroom demonstration.
