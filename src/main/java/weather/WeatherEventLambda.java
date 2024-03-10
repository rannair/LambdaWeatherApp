package weather;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import weather.model.ApiGatewayRequest;
import weather.model.ApiGatewayResponse;
import weather.model.WeatherEvent;

import java.io.IOException;

/**
 * Handler for requests to Lambda function.
 */
public class WeatherEventLambda {

    private final DynamoDB dynamoDBClient = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);
    private final String tableName = System.getenv("LOCATIONS_TABLE");

    public ApiGatewayResponse handleRequest(final ApiGatewayRequest request) throws IOException {
        final WeatherEvent weatherEvent = objectMapper.readValue(request.getBody(), WeatherEvent.class);
        final Table table = dynamoDBClient.getTable(tableName);
        Item item = new Item().withPrimaryKey("locationName", weatherEvent.getLocationName())
                .withDouble("latitude", weatherEvent.getLatitude())
                .withDouble("longitude", weatherEvent.getLongitude())
                .withDouble("temperature", weatherEvent.getTemperature())
                .withLong("timestamp", weatherEvent.getTimestamp());
        table.putItem(item);
        ApiGatewayResponse response = new ApiGatewayResponse();
        response.setStatusCode(200);
        response.setBody(weatherEvent.getLocationName());
        return response;
    }

}
