package de.welcz.todosjava;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodosRepository extends ReactiveMongoRepository<Todo, ObjectId> {
}
