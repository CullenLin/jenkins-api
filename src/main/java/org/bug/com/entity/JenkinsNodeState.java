package org.bug.com.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JenkinsNodeState {
    private String displayName;
    private Boolean offline;
}
