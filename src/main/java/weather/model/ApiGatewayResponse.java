package weather.model;

import lombok.Data;

@Data
public class ApiGatewayResponse {
   private int  statusCode;
    private String body;
}
