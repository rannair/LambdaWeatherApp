package weather;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import weather.model.ApiGatewayRequest;
import weather.model.ApiGatewayResponse;
import weather.model.WeatherEvent;

import java.util.List;
import java.util.stream.Collectors;


public class WeatherQueryLambda {

    private final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);
    private final String tableName = System.getenv("LOCATIONS_TABLE");

    private static String DEFAULT_LIMIT = "50";

    public ApiGatewayResponse handleRequest(final ApiGatewayRequest request) throws JsonProcessingException {
        final String limitParam = request.getQueryStringParameters()== null
                ? DEFAULT_LIMIT : request.getQueryStringParameters().getOrDefault("Limit", DEFAULT_LIMIT);
        Integer limit = Integer.parseInt(limitParam);
        ScanRequest scanRequest = new ScanRequest().withTableName(tableName).withLimit(limit);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        List<WeatherEvent> weatherEvents = scanResult.getItems().stream().map(
                event -> {
                    WeatherEvent weatherEvent = new WeatherEvent();
                    weatherEvent.setLocationName(event.get("locationName").getS());
                    weatherEvent.setTemperature(Double.parseDouble(event.get("temperature").getN()));
                    weatherEvent.setLatitude(Double.parseDouble(event.get("latitude").getN()));
                    weatherEvent.setLongitude(Double.parseDouble(event.get("longitude").getN()));
                    weatherEvent.setTimestamp(Long.parseLong(event.get("timestamp").getN()));
                    return weatherEvent;
                }
        ).collect(Collectors.toList());

        ApiGatewayResponse response = new ApiGatewayResponse();
        response.setStatusCode(200);
        response.setBody(objectMapper.writeValueAsString(weatherEvents));

        return response;
    }

}
