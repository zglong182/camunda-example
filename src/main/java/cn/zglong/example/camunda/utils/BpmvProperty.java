package cn.zglong.example.camunda.utils;

import lombok.Data;
import org.camunda.bpm.engine.identity.User;

import java.util.List;

@Data
public class BpmvProperty {

    private String code;
    private String name;
    private String value;
    private List<User> userList;
}

