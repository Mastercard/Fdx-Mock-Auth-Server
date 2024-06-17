package com.mastercard.fdx.mock.oauth2.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
@Slf4j
public class FdxMockAuthServerApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(FdxMockAuthServerApplication.class, args);
		log.info("!----FDX Mock Authorization Server Application Started Successfully----!");
	}

}
