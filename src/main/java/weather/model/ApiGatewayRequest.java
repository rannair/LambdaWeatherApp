package weather.model;

import jdk.jfr.DataAmount;
import lombok.Data;

import java.util.Map;
@Data
public class ApiGatewayRequest {
    private String body;
    private Map<String, String> queryStringParameters;
}
