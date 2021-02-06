package de.welcz.todosjava;

import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/todos")
public class Controller {
  private final TodosRepository repository;

  public Controller(TodosRepository repository) {
    this.repository = repository;
  }

  @GetMapping("{id}")
  public Mono<EntityModel<Todo>> getTodo(@PathVariable ObjectId id) {
    return repository.findById(id)
                     .map(this::withHateoas)
                     .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NO_CONTENT)));
  }

  @PostMapping
  public Mono<ResponseEntity<EntityModel<Todo>>> createTodo(@RequestBody Todo toSave) {
    return repository.save(toSave)
                     .map(this::withHateoas)
                     .map(this::wrapInCreatedResponse);
  }

  @PutMapping("{id}")
  public Mono<EntityModel<Todo>> updateTodo(@PathVariable ObjectId id, @RequestBody Todo toSave) {
    return repository.findById(id)
                     .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NO_CONTENT)))
                     .then(Mono.fromCallable(() -> toSave.withId(id)))
                     .flatMap(repository::save)
                     .map(this::withHateoas);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("{id}")
  public Mono<Void> deleteTodo(@PathVariable ObjectId id) {
    return repository.deleteById(id);
  }

  @GetMapping
  public Flux<EntityModel<Todo>> getAllTodos() {
    return repository.findAll()
                     .map(this::withHateoas);
  }

  private EntityModel<Todo> withHateoas(Todo todo) {
    return EntityModel.of(todo, Link.of(MessageFormat.format("/api/v1/todos/{0}", todo.getId()))
                                    .withSelfRel());
  }

  @NotNull
  private ResponseEntity<EntityModel<Todo>> wrapInCreatedResponse(EntityModel<Todo> todo) {
    return ResponseEntity.created(URI.create(MessageFormat.format("/api/v1/todos/{0}",
                                                                  Objects.requireNonNull(todo.getContent()).getId())))
                         .body(todo);
  }
}
