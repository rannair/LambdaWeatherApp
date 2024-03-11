package weather;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import weather.model.ApiGatewayRequest;
import weather.model.ApiGatewayResponse;
import weather.model.WeatherEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */
public class WeatherEventLambda {
    private final Region region = Region.US_EAST_1;
    private final DynamoDbClient dynamoDBClient = DynamoDbClient.builder().region(region).build();
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);
    private final String tableName = System.getenv("LOCATIONS_TABLE");

    public ApiGatewayResponse handleRequest(final ApiGatewayRequest request) throws IOException {

        final WeatherEvent weatherEvent = objectMapper.readValue(request.getBody(), WeatherEvent.class);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("locationName", AttributeValue.builder().s(weatherEvent.getLocationName()).build());
        item.put("latitude", AttributeValue.builder().n(String.valueOf(weatherEvent.getLatitude())).build());
        item.put("longitude", AttributeValue.builder().n(String.valueOf(weatherEvent.getLongitude())).build());
        item.put("temperature", AttributeValue.builder().n(String.valueOf(weatherEvent.getTemperature())).build());
        item.put("timestamp", AttributeValue.builder().n(String.valueOf(weatherEvent.getTimestamp())).build());

        PutItemRequest putRequest = PutItemRequest.builder().tableName(tableName).item(item).build();
        ApiGatewayResponse response = new ApiGatewayResponse();
        try {
            PutItemResponse putItemResponse = dynamoDBClient.putItem(putRequest);
            response.setStatusCode(200);
            response.setBody(objectMapper.writeValueAsString(putItemResponse.attributes()));
        } catch (ResourceNotFoundException resourceNotFoundException) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
            response.setStatusCode(500);
            response.setBody("The Amazon DynamoDB table".concat(tableName).concat("can't be found."));


        } catch (DynamoDbException dynamoDbException) {
            System.err.println(dynamoDbException.getMessage());
            response.setStatusCode(500);
            response.setBody(dynamoDbException.getMessage());
        }
        response.setBody(weatherEvent.getLocationName());
        return response;
    }
}
