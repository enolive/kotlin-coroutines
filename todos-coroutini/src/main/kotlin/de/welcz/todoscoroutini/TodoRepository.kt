package de.welcz.todoscoroutini

import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TodoRepository : CoroutineCrudRepository<Todo, ObjectId>
