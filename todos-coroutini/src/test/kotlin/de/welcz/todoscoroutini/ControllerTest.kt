package de.welcz.todoscoroutini;

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.coEvery
import io.mockk.coJustRun
import kotlinx.coroutines.flow.asFlow
import org.bson.types.ObjectId
import org.intellij.lang.annotations.Language
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@WebFluxTest(controllers = [Controller::class])
@EnableHypermediaSupport(type = [EnableHypermediaSupport.HypermediaType.HAL])
class ControllerTest(private val webTestClient: WebTestClient,
                     @MockkBean private val repository: TodoRepository) : DescribeSpec({
  val root = "/api/v1/todos"

  describe("API tests for /todos") {
    describe("GET /todos/{id} endpoint") {
      it("finds existing entity") {
        val id = ObjectId("5fbab3f8d5189026901ffb78")
        val todo = Todo(id, "Learn Kotlin")

        @Language("JSON")
        val expected = """{"title":"Learn Kotlin","_links":{"self":{"href":"/api/v1/todos/5fbab3f8d5189026901ffb78"}}}"""
        coEvery { repository.findById(id) } returns todo

        val response = webTestClient
          .get()
          .uri("$root/$id")
          .exchange()

        response.expectStatus().isOk
        response.expectBody().json(expected)
      }
      it("returns not found for not existing entities") {
        val id = ObjectId("5fbab5d2f420c43153a207f1")
        coEvery { repository.findById(id) } returns null

        val response = webTestClient
          .get()
          .uri("$root/$id")
          .exchange()

        response.expectStatus().isNotFound
      }
    }

    describe("POST /todos") {
      it("saves new entity") {
        val toInsert = Todo(title = "Plz save me")
        val saved = toInsert.copy(id = ObjectId("5fbab3f8d5189026901ffb78"))
        @Language("JSON")
        val expected = """{"title":"Plz save me","_links":{"self":{"href":"/api/v1/todos/5fbab3f8d5189026901ffb78"}}}"""
        coEvery { repository.save(toInsert) } returns saved

        val response = webTestClient
          .post()
          .uri(root)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(toInsert)
          .exchange()

        response.expectStatus().isCreated
        response.expectBody().json(expected)
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
        val existing = listOf(
            Todo(ObjectId("5fbab5d2f420c43153a207ef"), "First"),
            Todo(ObjectId("5fbab5d2f420c43153a207f0"), "Second"),
            Todo(ObjectId("5fbab5d2f420c43153a207f1"), "Third"),
        )
        @Language("JSON")
        val expected = """[
          {
            "title": "First",
            "_links": {
              "self": {
                "href": "/api/v1/todos/5fbab5d2f420c43153a207ef"
              }
            }
          },
          {
            "title": "Second",
            "_links": {
              "self": {
                "href": "/api/v1/todos/5fbab5d2f420c43153a207f0"
              }
            }
          },
          {
            "title": "Third",
            "_links": {
              "self": {
                "href": "/api/v1/todos/5fbab5d2f420c43153a207f1"
              }
            }
          }
        ]"""
        coEvery { repository.findAll() } returns existing.asFlow()

        val response = webTestClient
          .get()
          .uri(root)
          .exchange()

        response.expectStatus().isOk
        response.expectBody().json(expected)
      }
    }
  }
})
