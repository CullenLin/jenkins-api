package org.bug.com.server;


import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.bug.com.entity.JenkinsNode;
import org.bug.com.entity.JenkinsNodeState;
import org.bug.com.entity.PodTemplate;
import org.bug.com.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JenkinsExtServer {
    private static final Logger logger = LoggerFactory.getLogger(JenkinsExtServer.class);

    private static final String CT_FORM_URL_ENCODEED = "application/x-www-form-urlencoded";

    private static final String COMPUTER = "/computer";
    private static final String NODE_URL = COMPUTER + "/doCreateItem";
    private static final String SECRET_PATTERN = "<argument>([^<]*)<\\/argument>";
    private static final String DELETE_NODE = "/doDelete";
    private static final String GET_NODE = COMPUTER + "/%s" + "/api/json";
    private static final String SCRIPT_TEMPLATE = "if (Jenkins.instance.clouds.size() == 0) {\n" +
            "  println(\"{}\")\n" +
            "  return\n" +
            "}\n" +
            "for(t in Jenkins.instance.clouds.get(0).templates) {\n" +
            "  if (t.label.equals(\"%s\")) {\n" +
            "    podTemplate = '{\"label\": \"' + t.label + '\", \"image\": \"' + t.image + '\", \"volumesString\": \"' + t.volumesString.replace('\\n', \";\") + '\", \"cpuRequest\": \"' + t.cpuRequest + '\", \"memoryRequest\": \"' + t.memoryRequest + '\"}'\n" +
            "    println(podTemplate)\n" +
            "    break;\n" +
            "   \n" +
            "  }\n" +
            "}";
    private String serverUrl;
    private String userName;
    private String password;


    private Pattern pattern;
    private JsonUtils jsonUtils;

    public JenkinsExtServer(String serverUrl, String userName, String password) {
        this.serverUrl = serverUrl;
        this.userName = userName;
        this.password = password;

        pattern = Pattern.compile(SECRET_PATTERN);
        jsonUtils = new JsonUtils();
        jsonUtils.init();
    }


    public void createNode(String name, String nodeDescription, String labelString) {
        JenkinsNode node = new JenkinsNode(name, nodeDescription, labelString);
        String path = new StringBuilder(this.serverUrl).append(NODE_URL).toString();
        String body = jsonUtils.toJsonStr(node);

        logger.info("jenkins url={}, form={}", path, body);

        HttpResponse response = HttpRequest.post(path)
                .contentType(CT_FORM_URL_ENCODEED)
                .query("name", name)
                .query("type", "hudson.slaves.DumbSlave")
                .basicAuthentication(userName, password)
                .form("json", body).send();

        if (response.statusCode() != 302) {
            throw new RuntimeException(response.body());
        }
    }

    public JenkinsNodeState getNode(String name) {
        String path = serverUrl + String.format(GET_NODE, name);

        HttpResponse response = HttpRequest.get(path)
                .basicAuthentication(userName, password)
                .send();
        if (response.statusCode() == 404) {
            logger.info("cannot find a node with name {} in jenkins {}", name, serverUrl);
            return null;
        } else if (response.statusCode() == 200) {

            String body = response.bodyText();
            if (!StringUtils.isEmpty(body)) {
                return jsonUtils.fromJsonStr(body, JenkinsNodeState.class);
            }
        }

        return null;
    }

    public String getSecret(String name) {
        String path = new StringBuilder(this.serverUrl).append(COMPUTER)
                .append("/").append(name).append("/slave-agent.jnlp").toString();

        logger.info("jenkins url={}", path);

        HttpResponse response = HttpRequest.get(path)
                .basicAuthentication(userName, password)
                .send();
        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }

        String body = response.bodyText();
        if (body.contains("application-desc")) {
            Matcher matcher = pattern.matcher(body);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return response.bodyText();
    }

    public void deleteNode(String nodeName) {
       String path = String.format("%s%s/%s%s", serverUrl, COMPUTER, nodeName, DELETE_NODE);
       logger.info("jenkins url={}", path);

       HttpResponse response = HttpRequest.post(path)
               .basicAuthentication(userName, password)
               .send();

       if (response.statusCode() == 404) {
           logger.info("Node '{}' doesn't exists in jenkins server {}", nodeName, serverUrl);
           return;
       }

       if (response.statusCode() != 302) {
           throw new RuntimeException(response.body());
       }
       logger.info("Node '{}' has been deleted from jenkins server {}", nodeName, serverUrl);
    }

    public PodTemplate getPodTemplate(String label) {
        String script = String.format(SCRIPT_TEMPLATE, label);
        String body = runScript(script);
        if (StringUtils.isNotEmpty(body)) {
            return jsonUtils.fromJsonStr(body, PodTemplate.class);
        }
        return new PodTemplate();
    }

    public String runScript(String script) {
        String path = String.format("%s/scriptText", serverUrl);

        logger.info("executing jenkins script: {}", script);

        HttpResponse response = HttpRequest.post(path)
                .basicAuthentication(userName, password)
                .form("script", script)
                .send();

        return response.bodyText();
    }
}
