package de.welcz.todoscoroutini

import kotlinx.coroutines.flow.map
import org.bson.types.ObjectId
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.net.URI

const val root = "/api/v1/todos"

@RestController
@RequestMapping(root)
class Controller(private val repository: TodoRepository) {
  @GetMapping("{id}")
  suspend fun getTodo(@PathVariable id: ObjectId) =
    repository.findById(id)?.withSelfLink()
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PostMapping
  suspend fun createTodo(@RequestBody toSave: Todo) =
    repository.save(toSave).withSelfLink().wrappedInCreatedResponse()

  @PutMapping("{id}")
  suspend fun updateTodo(@PathVariable id: ObjectId, @RequestBody toModify: Todo) =
    repository.findById(id)
      ?.let { repository.save(toModify.copy(id = id)) }
      ?.withSelfLink()
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("{id}")
  suspend fun deleteTodo(@PathVariable id: ObjectId) =
    repository.deleteById(id)

  @GetMapping
  fun getAllTodos() =
    repository.findAll().map { it.withSelfLink() }

  private fun Todo.withSelfLink() =
    EntityModel.of(this, Link.of("$root/$id").withSelfRel())

  private fun EntityModel<Todo>.wrappedInCreatedResponse() =
    ResponseEntity.created(URI("$root/${content!!.id}")).body(this)
}
