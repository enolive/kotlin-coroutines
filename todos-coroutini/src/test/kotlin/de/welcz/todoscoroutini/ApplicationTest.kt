package de.welcz.todoscoroutini

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment

@SpringBootTest
class ApplicationTest(private val environment: Environment) :DescribeSpec({
  describe("Smoke Tests") {
    it("starts with a valid environment") {
      environment.shouldNotBeNull()
    }
  }
})
