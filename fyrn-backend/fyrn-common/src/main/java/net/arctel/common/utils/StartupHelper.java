/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.arctel.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import lombok.extern.slf4j.Slf4j;

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
