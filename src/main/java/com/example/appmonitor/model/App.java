package com.example.appmonitor.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class App {
    private String appName;
    private short instanceCount;
    private List<Instance> instanceList = new ArrayList<>();
}
