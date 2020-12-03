package de.welcz.todosjava;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "todos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Todo {
  @Id
  @JsonIgnore
  @With(AccessLevel.PUBLIC)
  private ObjectId id;
  private String title;
}
