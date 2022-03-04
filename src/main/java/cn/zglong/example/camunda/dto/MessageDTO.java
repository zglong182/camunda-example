package cn.zglong.example.camunda.dto;

import cn.zglong.example.camunda.entity.Message;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import springfox.documentation.annotations.ApiIgnore;

import java.io.Serializable;

@Data
@ApiModel("信息DTO")
public class MessageDTO extends Message {
    @ApiModelProperty("主题")
    private String topic;
}
