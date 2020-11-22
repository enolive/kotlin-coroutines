package de.welcz.todosjava;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "todos")
@Data
public class Todo {
  @Id
  @JsonIgnore
  private ObjectId id;
  private String title;
}
