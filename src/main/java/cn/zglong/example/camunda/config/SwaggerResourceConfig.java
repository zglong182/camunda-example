package cn.zglong.example.camunda;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;


@Component
@Primary
@AllArgsConstructor
public class SwaggerResourceConfig implements SwaggerResourcesProvider {

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        SwaggerResource local = new SwaggerResource();
        local.setSwaggerVersion("V1");
        local.setUrl("http://localhost:11001/v2/api-docs");
        local.setLocation("http://localhost:11001/v2/api-docs");
        local.setName("Camunda演示");
        resources.add(local);

        SwaggerResource camunda = new SwaggerResource();
        camunda.setSwaggerVersion("V1");
        camunda.setUrl("http://localhost:11001/swagger.json");
        camunda.setLocation("http://localhost:11001/swagger.json");
        camunda.setName("Camunda Rest-AP)");
        resources.add(camunda);
        return resources;
    }
}
