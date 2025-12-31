package com.arctel.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

@Slf4j
public class StartupHelper {

    public static void printStartInfo(ConfigurableApplicationContext context) {
        Environment env = context.getEnvironment();
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = Optional.ofNullable(env.getProperty("server.servlet.context-path"))
                .filter(path -> !path.isEmpty())
                .orElse("/");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }

        String version = env.getProperty("info.project.version", "unknown");
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        log.info("""
                        
                        ----------------------------------------------------------
                         _______   ______  _   _\s
                        |  ___\\\\ \\\\ / /  _ \\\\| \\\\ | |
                        | |_   \\\\ V /| |_) |  \\\\| |
                        |  _|   | | |  _ <| |\\\\  |
                        |_|     |_| |_| \\\\_\\\\_| \\\\_|
                        
                        Application '{}' is running! Access URLs:
                        \t\
                        Local: \t\t{}://localhost:{}{}
                        \t\
                        External: \t{}://{}:{}{}
                        \t\
                        Profile(s): \t{}
                        \t\
                        Version: \t{}
                        \t\
                        Java Version: \t{}
                        \t\
                        OS: \t\t{} ({})
                        ----------------------------------------------------------""",
                env.getProperty("spring.application.name", "Fyrn"),
                protocol, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath,
                env.getActiveProfiles(),
                version,
                javaVersion,
                osName, osArch);

    }
}
