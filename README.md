# taktx-worker-quarkus-example

Example Quarkus application containing workers for TaktX engine

## Prerequisites

- Java 17 or higher
- Gradle (or use the included Gradle wrapper)

## Building the Application

To build the application, run:

```bash
./gradlew build
```

## Running the Application

### Development Mode

To run the application in development mode with live reload:

```bash
./gradlew quarkusDev
```

### Production Mode

To run the application in production mode:

```bash
./gradlew build
java -jar build/quarkus-app/quarkus-run.jar
```

## Testing

To run the tests:

```bash
./gradlew test
```

## API Endpoints

- `GET /hello` - Returns a greeting message

## Project Structure

```
src/
├── main/
│   ├── java/io/taktx/     # Application source code
│   └── resources/          # Application configuration
└── test/
    └── java/io/taktx/     # Test source code
```
