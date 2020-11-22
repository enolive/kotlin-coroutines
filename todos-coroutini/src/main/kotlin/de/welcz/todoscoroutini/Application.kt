package de.welcz.todoscoroutini

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.hateoas.config.EnableHypermediaSupport

@SpringBootApplication
@EnableHypermediaSupport(type = [EnableHypermediaSupport.HypermediaType.HAL])
class Application

fun main(args: Array<String>) {
  runApplication<Application>(*args)
}
