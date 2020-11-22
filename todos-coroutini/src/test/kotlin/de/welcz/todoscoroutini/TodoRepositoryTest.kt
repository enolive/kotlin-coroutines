package de.welcz.todoscoroutini

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest

@DataMongoTest
class TodoRepositoryTest(private val sut: TodoRepository) : DescribeSpec({
  val aTodo = Arb.string().map { Todo(title = it) }

  describe("Persistence tests for todos") {
    it("can be saved") {
      checkAll(aTodo) { toSave ->
        val saved = sut.save(toSave)

        saved.id.shouldNotBeNull()
        saved.title shouldBe toSave.title
        sut.findById(saved.id!!) shouldBe saved
      }
    }
    it("can be modified") {
      val aTitle = Arb.string()
      checkAll(aTodo, aTitle) { toSave, modifiedTitle ->
        val saved = sut.save(toSave)
        val toModify = saved.copy(title = modifiedTitle)

        val modified = sut.save(toModify)

        modified shouldBe toModify
        sut.findById(saved.id!!) shouldBe modified
      }
    }
  }
})