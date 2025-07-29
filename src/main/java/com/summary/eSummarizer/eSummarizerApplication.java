package com.summary.eSummarizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetAddress;

@SpringBootApplication
public class eSummarizerApplication {

    @Value("${server.port:8080}")
    private int port;

	public static void main(String[] args) {
		SpringApplication.run(eSummarizerApplication.class, args);
	}

    @EventListener(ApplicationReadyEvent.class)
    public void printApplicationUrls() {
        try {
            String localhost = "localhost";
            String externalIp = InetAddress.getLocalHost().getHostAddress();

            String localUrl = String.format("http://%s:%d", localhost, port);
            String externalUrl = String.format("http://%s:%d", externalIp, port);

            System.out.println("Local:      " + localUrl);
            System.out.println("External:   " + externalUrl);
        } catch (Exception e) {
            System.err.println("Failed to determine external IP address: " + e.getMessage());
        }
    }
}