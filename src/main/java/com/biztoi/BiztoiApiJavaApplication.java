package com.biztoi;

import com.biztoi.web.config.DefaultProfileUtil;
import com.biztoi.web.feign.WeatherClient;
import com.github.jknack.handlebars.internal.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@EnableFeignClients
@Slf4j
public class BiztoiApiJavaApplication implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(BiztoiApiJavaApplication.class);

	private final Environment env;

	public BiztoiApiJavaApplication(Environment env) {
		this.env = env;
	}

	@Autowired
	private WeatherClient weatherClient;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(BiztoiApiJavaApplication.class);
		DefaultProfileUtil.addDefaultProfile(app);
		Environment env = app.run(args).getEnvironment();
		logApplicationStartup(env);
	}

	private static void logApplicationStartup(Environment env) {
		String protocol = "http";
		if (env.getProperty("server.ssl.key-store") != null) {
			protocol = "https";
		}
		String serverPort = env.getProperty("server.port");
		String contextPath = env.getProperty("server.servlet.context-path");
		if (StringUtils.isBlank(contextPath)) {
			contextPath = "/";
		}
		String hostAddress = "localhost";
		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.warn("The host name could not be determined, using `localhost` as fallback");
		}
		log.info("\n----------------------------------------------------------\n\t" +
						"Application '{}' is running! Access URLs:\n\t" +
						"Local: \t\t{}://localhost:{}{}\n\t" +
						"External: \t{}://{}:{}{}\n\t" +
						"Profile(s): \t{}\n----------------------------------------------------------",
				"BizToi API",
				protocol,
				serverPort,
				contextPath,
				protocol,
				hostAddress,
				serverPort,
				contextPath,
				env.getActiveProfiles());
	}

	@Override
	public void run(String... args) {
		ResponseEntity res = this.weatherClient.getWeatherInfo(130010L);
		log.info(res.getBody().toString());
	}
}
