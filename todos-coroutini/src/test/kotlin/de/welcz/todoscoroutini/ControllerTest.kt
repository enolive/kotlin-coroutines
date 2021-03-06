package de.welcz.todoscoroutini

import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.json.shouldMatchJson
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import kotlinx.coroutines.flow.flow
import org.bson.types.ObjectId
import org.intellij.lang.annotations.Language
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@Suppress("JsonStandardCompliance")
private const val root = "/api/v1/todos"

@WebFluxTest(controllers = [Controller::class])
@EnableHypermediaSupport(type = [EnableHypermediaSupport.HypermediaType.HAL])
class ControllerTest(
  private val webTestClient: WebTestClient,
  @MockkBean private val repository: TodoRepository
) : DescribeSpec({

  beforeAny { clearMocks(repository) }

  describe("API tests for /todos") {
    describe("GET /todos/{id} endpoint") {
      it("finds existing entity") {
        val id = ObjectId.get()
        val todo = Todo(id, "Learn Kotlin")
        val expected = todo.toJson()
        coEvery { repository.findById(id) } returns todo

        val response = webTestClient
          .get()
          .uri("$root/$id")
          .exchange()

        response.expectStatus().isOk
        response.shouldHaveJsonBody(expected)
      }
      it("returns not found for not existing entities") {
        val id = ObjectId.get()
        coEvery { repository.findById(id) } returns null

        val response = webTestClient
          .get()
          .uri("$root/$id")
          .exchange()

        response.expectStatus().isNoContent
      }
    }

    describe("POST /todos") {
      it("saves new entity") {
        val toInsert = Todo(title = "Plz save me")
        val saved = toInsert.copy(id = ObjectId.get())
        val expected = saved.toJson()
        coEvery { repository.save(toInsert) } returns saved

        val response = webTestClient
          .post()
          .uri(root)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(toInsert)
          .exchange()

        response.expectStatus().isCreated
        response.shouldHaveJsonBody(expected)
      }
    }

    describe("PUT /todos/{id}") {
      it("updates existing entity") {
        val id = ObjectId.get()
        val toModify = Todo(title = "Plz update me")
        val existing = Todo(title = "Existing", id = id)
        val modified = toModify.copy(id = existing.id)
        val expected = modified.toJson()
        coEvery { repository.findById(id) } returns existing
        coEvery { repository.save(any()) } returns modified

        val response = webTestClient
          .put()
          .uri("$root/${existing.id}")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(toModify)
          .exchange()

        response.expectStatus().isOk
        response.shouldHaveJsonBody(expected)
        coVerify { repository.save(modified) }
      }
      it("ignores missing entity") {
        val toModify = Todo(title = "Plz update me")
        val id = ObjectId.get()
        coEvery { repository.findById(id) } returns null

        val response = webTestClient
          .put()
          .uri("$root/${id}")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(toModify)
          .exchange()

        response.expectStatus().isNoContent
        coVerify(exactly = 0) { repository.save(any()) }
      }
    }

    describe("DELETE /todos/{id}") {
      it("deletes existing entity") {
        val id = ObjectId.get()
        coJustRun { repository.deleteById(id) }

        val response = webTestClient
          .delete()
          .uri("$root/$id")
          .exchange()

        response.expectStatus().isNoContent
      }
    }

    describe("GET /todos") {
      it("finds existing entities") {
        val existing1 = Todo(ObjectId.get(), "First")
        val existing2 = Todo(ObjectId.get(), "Second")
        val existing3 = Todo(ObjectId.get(), "Third")
        @Language("json")
        val expected = """{
          "_embedded": {
            "todoList": [
              ${existing1.toJson()},
              ${existing2.toJson()},
              ${existing3.toJson()}
            ]
          },
          "_links": {
            "self": {"href": "$root"}
          }
        }
        """.trimIndent()
        coEvery { repository.findAll() } coAnswers {
          flow {
            emit(existing1)
            emit(existing2)
            emit(existing3)
          }
        }

        val response = webTestClient
          .get()
          .uri(root)
          .exchange()

        response.expectStatus().isOk
        response.shouldHaveJsonBody(expected)
      }
    }
  }
})

@Language("JSON")
private fun Todo.toJson() =
  """{
  "title": "$title",
  "_links": {
    "self": {
      "href": "$root/$id"
    }
  }
}"""

private fun WebTestClient.ResponseSpec.shouldHaveJsonBody(@Language("JSON") expected: String) {
  expectBody<String>().consumeWith {
    it.responseBody shouldMatchJson expected
  }
}
