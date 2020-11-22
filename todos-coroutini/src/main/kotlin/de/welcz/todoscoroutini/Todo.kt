package de.welcz.todoscoroutini

import com.fasterxml.jackson.annotation.JsonIgnore
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "todos")
data class Todo(
    @Id
    @JsonIgnore
    val id: ObjectId? = null,
    val title: String,
)
