package org.bug.com.entity;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class JenkinsNode {

    @Builder
    public JenkinsNode(String name, String nodeDescription, String labelString) {
        this.name = name;
        this.nodeDescription = nodeDescription;
        this.labelString = labelString;
        numExecutors = "1";
        remoteFS = "/home/jenkins";
        mode = "EXCLUSIVE";
        launcher = new HashMap<>();
        launcher.put("stapler-class", "hudson.slaves.JNLPLauncher");
        launcher.put("class", "hudson.slaves.JNLPLauncher");

        retentionStrategy = new HashMap<>();
        retentionStrategy.put("stapler-class", "hudson.slaves.RetentionStrategy$Always");
        retentionStrategy.put("class", "hudson.slaves.RetentionStrategy$Always");

        nodeProperties = new HashMap<>();
        nodeProperties.put("stapler-class-bag", "true");
        type = "hudson.slaves.DumbSlave";
    }

    private String name;
    private String nodeDescription;
    private String numExecutors;
    private String remoteFS;
    private String labelString;
    private String mode;
    private Map<String, String> launcher;
    Map<String, String> retentionStrategy;
    Map<String, String> nodeProperties;
    private String type;
}
