package cn.zglong.example.camunda;

import cn.zglong.example.camunda.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.spring.annotations.ProcessEngineComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@SpringBootApplication
@ProcessEngineComponent
@RequestMapping
@Slf4j
@RestController
@EnableDiscoveryClient
public class CamundaExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamundaExampleApplication.class, args);
    }

    @PostMapping("/http-connect")
    public String httpConnect(@RequestBody MessageDTO messageDto, HttpServletRequest request) {
        log.info("我被请求了, 用户token: {}", request.getHeader("token"));
        return "名称" + messageDto.getName();
    }
}
