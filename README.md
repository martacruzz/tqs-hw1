# Requirements
To build, run, and test this application, you’ll need the following tools installed on your system:

## Backend (Spring Boot)
Java 17 (or compatible JDK)
Maven 3.6+
Docker Engine 20.10+
Docker Compose v2+

## Frontend (React)
Node.js 18+
npm 8+

## Testing
Maven
Cucumber
Playwright
Gatling
Scala

## Optional (for development & testing)
Postman or curl

# How to run
## Backend
For this project, I decided to have the backend running on a docker container with the Spring boot application and the database server.
To run the spring boot application:
```
mvn clean package -DskipTests 
docker compose up --build
```
## Frontend
The frontend should run locally through npm:
```
npm run dev
```
## Tests
To run the tests make sure the api is running by:
```
docker compose up --build
```
Run the frontend locally:
```
npm run dev
```
Run the tests locally:
```
mvn test -Dtest=[test_file]
```
For performance tests:
```
mvn gatling:test
```
Note: when running the performace tests don't forget to build the docker container without the volums (if any prior tests were made)
Github actions:
When pushing don't forget to have the frontend web server running locally