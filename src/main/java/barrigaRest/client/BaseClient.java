package barrigaRest.client;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class BaseClient {
    public static RequestSpecification requestSpec;
    public static String TOKEN;

    static{
        requestSpec = new RequestSpecBuilder()
                .setBaseUri("https://barrigarest.wcaquino.me")
                .setContentType(ContentType.JSON)
                .build()
                ;
    }

    public static RequestSpecification requestLogado(){
        return new RequestSpecBuilder()
                .addRequestSpecification(requestSpec)
                .addHeader("Authorization", "JWT " + TOKEN)
                .build();

    }
}
