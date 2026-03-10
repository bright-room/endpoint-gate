package net.brightroom.example.condition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point for the WebFlux condition example application. */
@SpringBootApplication
public class ConditionExampleApplication {

  /**
   * Starts the application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(ConditionExampleApplication.class, args);
  }
}
