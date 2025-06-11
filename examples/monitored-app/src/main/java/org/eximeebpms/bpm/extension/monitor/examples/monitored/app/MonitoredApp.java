package org.eximeebpms.bpm.extension.monitor.examples.monitored.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.eximeebpms.bpm.extension.monitor.EnableMonitoring
public class MonitoredApp {

	public static void main(String[] args) {

		SpringApplication.run(MonitoredApp.class, args);
	}

}
