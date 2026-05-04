# Temp For City App

This repository contains an AWS Lambda function written in Java that retrieves the current temperature for a specified city. It uses the Open-Meteo APIs for both geocoding (finding coordinates for a city) and fetching weather data.

## Features

*   Fetches the current temperature for any given city.
*   Categorizes the temperature (e.g., FREEZING, COLD, MILD, WARM, HOT).
*   Configurable temperature units (Celsius or Fahrenheit).
*   Built-in retry mechanism for external API calls to improve reliability.
*   Handles errors gracefully and returns informative error messages.
*   Designed to be deployed as an AWS Lambda function exposed via a Lambda Function URL.

## Architecture

The application is structured as a serverless function with a clear separation of concerns:

1.  **`WeatherHandler`**: The main entry point for the AWS Lambda function. It parses the incoming request, validates the input, and orchestrates the response. It contains no business logic — its only responsibility is to translate HTTP input/output and delegate to the service layer.
2.  **`WeatherService`**: The core business logic resides here. It uses the geocoding client to get coordinates for a city and then uses the weather client to fetch the temperature. Keeping this separate from the handler means the logic can be tested and reused independently of AWS.
3.  **Clients (`GeocodingClient`, `WeatherClient`)**: Defined as interfaces so that the rest of the application has no dependency on any specific weather provider. The concrete implementations (`OpenMeteoGeocodingClient`, `OpenMeteoWeatherClient`) handle HTTP communication, response parsing, and retry logic. This makes it trivial to swap or add providers without touching business logic.
4.  **`AppConfig`**: Centralizes all configuration loaded from environment variables, so no magic strings are scattered across the codebase.
5.  **Models & Enums**: Data transfer objects (`Temperature`, `Coordinates`, `ErrorDTO`) and enumerations (`TemperatureCategory`, `TemperatureUnit`) provide type-safe data structures. `TemperatureCategory.from()` encapsulates the classification logic in one place, keeping it isolated and easy to test.

## Key Design Decisions

- **Thin handler**: The `WeatherHandler` deliberately contains no business logic. This makes the Lambda entry point easy to read and keeps AWS-specific code isolated from the core application.
- **Interface-based clients**: `WeatherClient` and `GeocodingClient` are interfaces rather than concrete classes. This decouples the service layer from any specific API provider and is the foundation for both testability and extensibility.
- **Geocoding via Open-Meteo**: Rather than requiring the caller to provide coordinates, the function accepts a human-readable city name and resolves it to coordinates using the Open-Meteo Geocoding API. This keeps the public interface simple.
- **First result as most relevant**: The Geocoding API returns a list of locations matching the given city name, ordered by relevance (most popular first). The function always uses the first result, which in practice corresponds to the most well-known city with that name.
- **Retry logic in clients**: Transient failures are an external API concern, not a business concern. Both API clients implement their own configurable retry logic (max retries and delay are set via environment variables), keeping `WeatherService` simple and focused.
- **Environment-based configuration**: All tunable parameters (URLs, retry counts, delays, temperature unit) are environment variables, which is standard practice for Lambda functions and avoids hardcoded values.

## Unit Testing Without the Real API

Because `WeatherClient` and `GeocodingClient` are interfaces, their implementations can be replaced with mocks in unit tests. For example, using Mockito:

- A mock `WeatherClient` can be configured to return a fixed temperature value.
- A mock `GeocodingClient` can return a predefined set of coordinates.
- `WeatherService` can then be tested in complete isolation — no HTTP calls, no network dependency, no Open-Meteo account needed.

This also means `TemperatureCategory.from()` can be unit tested directly as a pure function with no dependencies at all.

## Getting Started

### Prerequisites

*   Java 25 JDK
*   Apache Maven
*   An AWS account for deployment

### Configuration

The application is configured using environment variables. Before running or deploying, you must set the following variables. You can create a `.env` file for local development based on the `.env.example` file.

| Variable                       | Description                                                     | Example                                            |
| ------------------------------ | --------------------------------------------------------------- | -------------------------------------------------- |
| `WEATHER_API_URL`              | The base URL for the weather API.                               | `https://api.open-meteo.com/v1/forecast`           |
| `WEATHER_API_MAX_RETRIES`      | The maximum number of retry attempts for weather API calls.     | `3`                                                |
| `WEATHER_API_RETRY_DELAY_MS`   | The delay in milliseconds between retry attempts.               | `1000`                                             |
| `TEMPERATURE_UNIT`             | The desired temperature unit. Can be `celsius` or `fahrenheit`. | `celsius`                                          |
| `GEOCODING_API_URL`            | The base URL for the geocoding API.                             | `https://geocoding-api.open-meteo.com/v1/search`   |
| `GEOCODING_API_MAX_RETRIES`    | The maximum number of retry attempts for geocoding API calls.   | `3`                                                |
| `GEOCODING_API_RETRY_DELAY_MS` | The delay in milliseconds between retry attempts.               | `1000`                                             |

### Build

This project uses the `maven-shade-plugin` to create a fat JAR containing all necessary dependencies. To build the project, run the following command from the root directory:

```bash
mvn clean package
```

This will generate a JAR file in the `target/` directory (e.g., `weather.app.assignment-1.0-SNAPSHOT.jar`). This is the file you will upload to AWS Lambda.

## Deployment

1.  **Create a Lambda Function**: In the AWS Management Console, create a new Lambda function.
    *   **Runtime**: Select a Java runtime (e.g., Java 21).
    *   **Architecture**: Select `x86_64`.
2.  **Upload Code**: Upload the fat JAR file generated by the Maven build.
3.  **Configure Handler**: In the function's "Code source" section, edit the "Runtime settings" to set the handler to:
    `com.sa.handlers.WeatherHandler::handleRequest`
4.  **Set Environment Variables**: In the "Configuration" > "Environment variables" section, add the key-value pairs listed in the [Configuration](#configuration) section.
5.  **Enable Function URL**: In the "Configuration" > "Function URL" section, click "Create function URL".
    *   Set auth type to `NONE` for public access.
    *   Copy the generated URL — this is your HTTP endpoint.

## Usage

Once deployed, you can invoke the function via its Lambda Function URL. The function expects a single query parameter: `cityName`.

**Example Request:**

```
GET https://<your-function-url-id>.lambda-url.<your-region>.on.aws/?cityName=Tokyo
```

**Example Success Response (200 OK):**

```json
{
    "temperature": 18.5,
    "unit": "CELSIUS",
    "category": "MILD"
}
```

**Example Error Response (400 Bad Request):**

If the `cityName` parameter is missing.

```json
{
    "status": 400,
    "message": "Missing query parameter: cityName",
    "timestamp": "2023-10-27 10:30:00"
}
```

**Example Error Response (502 Bad Gateway):**

If an external API fails or returns an unexpected response.

```json
{
    "status": 502,
    "message": "External API failure",
    "timestamp": "2023-10-27 10:31:15"
}
```

## Design Reflection — Adding a New Weather Provider

The current design supports adding a new weather provider with minimal changes. Because `WeatherClient` is an interface, a new provider (e.g. OpenWeatherMap) would only require a new implementation class — `WeatherService` and `WeatherHandler` would remain completely untouched. The same applies to the geocoding layer via `GeocodingClient`.

The main current limitation is that the provider is fixed at deployment time through environment variables. There is no runtime mechanism to select between multiple providers dynamically. Given more time, the most valuable improvement would be introducing a `WeatherClientFactory` that reads a `WEATHER_PROVIDER` environment variable and returns the appropriate implementation — making it straightforward to support multiple providers or fall back between them.
