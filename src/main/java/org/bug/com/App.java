package org.bug.com;

import org.bug.com.server.JenkinsExtServer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )    {
        String serverUrl = "http://localhost:8080";
        JenkinsExtServer server = new JenkinsExtServer(serverUrl, "user", "password");

        server.createNode("demo-slave", "demo create node", "2020-03-20");


    }
}
