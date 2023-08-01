# CoStudy - University Management System - README

## Description
The University Management System is a web application designed to facilitate administrative tasks in academic institutions. The application is built using Java, Spring, HTML, CSS, JavaScript, Maven, Thymeleaf, and Bootstrap.

Features of the application include:
- Adding and managing subjects
- Registering for subjects
- Scheduling individual classes
- Generating timetables
- Creating and conducting online tests
- Providing two distinct dashboards for students and professors

The application also includes a superuser or admin account, which has full control over users.

## Getting Started

### Prerequisites
Ensure that you have the following installed on your local machine:
- Java 8 or above
- Maven

### Installation

1. Clone the repo:
```
git clone git@github.com:Blazej-Jendrzejewski/CoStudy.git
```
2. Navigate to the project directory and run the following command to compile the project:
```
mvn clean install
```
3. After a successful build, you can run the application with:
```
java -jar target/CoStudy.jar
```

### Usage

To start using the application:
1. Launch your web browser and go to `http://localhost:8080`
2. You will be presented with a login screen. Log in as a student, professor, or admin to access the different dashboards. Current admin credencial are Login: admin Pass: admin

