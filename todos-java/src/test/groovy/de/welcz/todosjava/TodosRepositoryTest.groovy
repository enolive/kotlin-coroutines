package de.welcz.todosjava

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import spock.lang.Specification

@DataMongoTest
class TodosRepositoryTest extends Specification {
  @Autowired
  private TodosRepository sut

  def "todo can be created"() {
    given: "a todo to be saved"
    def toSave = new Todo(title: "Plz save me")

    when: "todo is saved"
    def result = sut.save(toSave).block()

    then: "result has expected properties"
    result != null
    result.id != null
    result.title == toSave.title
    and: "todo can be queried"
    sut.findById(result.id).block() == result
  }

  def "todo can be modified"() {
    given: "an existing todo"
    def toSave = new Todo(title: "Plz save me")
    def result = sut.save(toSave).block()
    and: "a modified todo"
    def toModify = new Todo(id: result.id, title: "Modify me")

    when: "todo is saved"
    def modified = sut.save(toModify).block()

    then: "result has expected properties"
    modified == toModify
    and: "todo can be queried"
    sut.findById(result.id).block() == modified
  }
}
