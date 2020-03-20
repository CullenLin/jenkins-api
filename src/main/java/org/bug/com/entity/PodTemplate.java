package org.bug.com.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PodTemplate {
    private String name;
    private  String image;
    private boolean privileged;
    private String command;
    private String args;
    private String remoteFs;
    private int instanceCap;
    private String label;
    private String volumesString;
    private int poolSize;
    private String cpuLimit;
    private String memoryLimit;
    private String cpuRequest;
    private String memoryRequest;
}
