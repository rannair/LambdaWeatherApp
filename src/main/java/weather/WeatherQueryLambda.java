package weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import weather.model.ApiGatewayRequest;
import weather.model.ApiGatewayResponse;
import weather.model.WeatherEvent;

import java.util.List;
import java.util.stream.Collectors;


public class WeatherQueryLambda {

    private final Region region = Region.US_EAST_1;
    private final DynamoDbClient dynamoDBClient = DynamoDbClient.builder().region(region).build();
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);
    private final String tableName = System.getenv("LOCATIONS_TABLE");

    private final static String DEFAULT_LIMIT = "50";

    public ApiGatewayResponse handleRequest(final ApiGatewayRequest request) throws JsonProcessingException {
        final String limitParam = request.getQueryStringParameters() == null
                ? DEFAULT_LIMIT : request.getQueryStringParameters().getOrDefault("Limit", DEFAULT_LIMIT);
        Integer limit = Integer.parseInt(limitParam);
        ScanRequest scanRequest = ScanRequest.builder().tableName(tableName).limit(limit).build();
        ApiGatewayResponse response = new ApiGatewayResponse();
        try {
            ScanResponse scanResponse = dynamoDBClient.scan(scanRequest);
            List<WeatherEvent> weatherEvents = scanResponse.items().stream().map(
                    event -> {
                        WeatherEvent weatherEvent = new WeatherEvent();
                        weatherEvent.setLocationName(event.get("locationName").s());
                        weatherEvent.setTemperature(Double.parseDouble(event.get("temperature").n()));
                        weatherEvent.setLatitude(Double.parseDouble(event.get("latitude").n()));
                        weatherEvent.setLongitude(Double.parseDouble(event.get("longitude").n()));
                        weatherEvent.setTimestamp(Long.parseLong(event.get("timestamp").n()));
                        return weatherEvent;
                    }
            ).collect(Collectors.toList());
            response.setStatusCode(200);
            response.setBody(objectMapper.writeValueAsString(weatherEvents));
        } catch (ResourceNotFoundException resourceNotFoundException) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
            response.setStatusCode(500);
            response.setBody("The Amazon DynamoDB table".concat(tableName).concat("can't be found."));

        } catch (DynamoDbException dynamoDbException) {
            System.err.println(dynamoDbException.getMessage());
            response.setStatusCode(500);
            response.setBody(dynamoDbException.getMessage());
        }
        return response;
    }
}
