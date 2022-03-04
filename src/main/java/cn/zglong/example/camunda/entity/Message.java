package cn.zglong.example.camunda.entity;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import springfox.documentation.annotations.ApiIgnore;

@Data
@ApiIgnore
public class Message {
    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    private String name;
    /**
     * 密码
     */
    private String password;

    public Message() {
    }

    public Message(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
