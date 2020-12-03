package de.welcz.todosjava

import org.bson.types.ObjectId
import org.intellij.lang.annotations.Language
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

@WebFluxTest(controllers = Controller)
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
class ControllerTest extends Specification {
  @Autowired
  private WebTestClient webTestClient
  @SpringBean
  private TodosRepository repository = Mock()

  def "GET /todos/{id} succeeds"() {
    given: "an id"
    def id = new ObjectId("5fbab3f8d5189026901ffb78")
    and: "an existing entity"
    def todo = new Todo(id: id, title: "Learn Kotlin")
    and: "an expected response"
    @Language("JSON")
    def expected = """{"title":"Learn Kotlin","_links":{"self":{"href":"/api/v1/todos/5fbab3f8d5189026901ffb78"}}}"""

    when: "API is called"
    def response = webTestClient.get()
                                .uri("/api/v1/todos/$id")
                                .exchange()

    then: "status is ok"
    response.expectStatus().isOk()
    and: "expected response is retrieved"
    response.expectBody().json(expected)
    and: "repository returned saved todo"
    1 * repository.findById(id) >> Mono.just(todo)
  }

  def "GET /todos/{id} returns not found"() {
    given: "an id"
    def id = ObjectId.get()

    when: "API is called"
    def response = webTestClient.get()
                                .uri("/api/v1/todos/$id")
                                .exchange()

    then: "status is not found"
    response.expectStatus().isNotFound()
    and: "repository returned empty response"
    1 * repository.findById(id) >> Mono.empty()
  }

  def "POST /todos"() {
    given: "a todo to insert"
    def toInsert = new Todo(title: "Plz save this")
    and: "a saved todo"
    def saved = new Todo(id: new ObjectId("5fbab3f8d5189026901ffb78"), title: toInsert.title)
    and: "an expected response"
    @Language("JSON")
    def expected = """{"title":"Plz save this","_links":{"self":{"href":"/api/v1/todos/5fbab3f8d5189026901ffb78"}}}"""

    when: "API is called"
    def response = webTestClient.post()
                                .uri("/api/v1/todos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(toInsert)
                                .exchange()

    then: "status is created"
    response.expectStatus().isCreated()
    and: "location points to created response"
    response.expectHeader().location("/api/v1/todos/${saved.id}")
    and: "created entity was returned"
    response.expectBody().json(expected)
    and: "entity was saved"
    1 * repository.save(toInsert) >> Mono.just(saved)
  }

  def "PUT /todos/{id}"() {
    given: "a todo to modify"
    def toModify = new Todo(title: "Plz modify this")
    and: "an id"
    def id = new ObjectId("5fbab3f8d5189026901ffb78")
    and: "an existing todo"
    def existing = new Todo(id: id, title: "Existing")
    and: "a modified todo"
    def modified = new Todo(id: existing.id, title: toModify.title)
    and: "an expected response"
    @Language("JSON")
    def expected = """{"title":"Plz modify this","_links":{"self":{"href":"/api/v1/todos/5fbab3f8d5189026901ffb78"}}}"""

    when: "API is called"
    def response = webTestClient.put()
                                .uri("/api/v1/todos/$id")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(toModify)
                                .exchange()

    then: "status is ok"
    response.expectStatus().isOk()
    and: "created entity was returned"
    response.expectBody().json(expected)
    and: "entity exists"
    1 * repository.findById(id) >> Mono.just(existing)
    and: "entity was saved"
    1 * repository.save(modified) >> Mono.just(modified)
  }

  def "PUT /todos/{id} ignores missing entity"() {
    given: "a todo to modify"
    def toModify = new Todo(title: "Plz modify this")
    and: "an id"
    def id = new ObjectId("5fbab3f8d5189026901ffb78")

    when: "API is called"
    def response = webTestClient.put()
                                .uri("/api/v1/todos/$id")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(toModify)
                                .exchange()

    then: "status is not found"
    response.expectStatus().isNotFound()
    and: "entity does not exist"
    1 * repository.findById(id) >> Mono.empty()
    and: "entity was not saved"
    0 * repository.save(*_)
  }

  def "DELETE /todos/{id}"() {
    given: "an id"
    def id = ObjectId.get()

    when: "API is called"
    def response = webTestClient.delete()
                                .uri("/api/v1/todos/$id")
                                .exchange()

    then: "status is no content"
    response.expectStatus().isNoContent()
    and: "entity was deleted"
    1 * repository.deleteById(id) >> Mono.empty()
  }

  def "GET /todos"() {
    given: "existing entities"
    def todos = [
        new Todo(id: new ObjectId("5fbab5d2f420c43153a207ef"), title: "First"),
        new Todo(id: new ObjectId("5fbab5d2f420c43153a207f0"), title: "Second"),
        new Todo(id: new ObjectId("5fbab5d2f420c43153a207f1"), title: "Third"),
    ]
    and: "an expected response"
    @Language("JSON")
    def expected = """[
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

    when: "API is called"
    def response = webTestClient.get()
                                .uri("/api/v1/todos")
                                .exchange()

    then: "status is ok"
    response.expectStatus().isOk()
    and: "expected response was received"
    response.expectBody().json(expected)
    and: "entities were found in repository"
    1 * repository.findAll() >> Flux.fromIterable(todos)
  }

  private static EntityModel<Todo> enrichWithHateoas(Todo todo) {
    EntityModel.of(new Todo(title: todo.title), Link.of("/api/v1/todos/${todo.id}").withSelfRel())
  }
}
