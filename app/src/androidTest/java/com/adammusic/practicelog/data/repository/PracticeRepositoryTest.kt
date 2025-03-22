import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adammusic.practicelog.data.db.PracticeDatabase
import com.adammusic.practicelog.data.db.dao.CategoryDao
import com.adammusic.practicelog.data.db.dao.PracticeDao
import com.adammusic.practicelog.data.db.dao.SessionDao
import com.adammusic.practicelog.data.repository.PracticeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.google.common.truth.Truth.assertThat

@RunWith(AndroidJUnit4::class)
class PracticeRepositoryTest {
    private lateinit var practiceDao: PracticeDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var sessionDao: SessionDao
    private lateinit var database: PracticeDatabase
    private lateinit var repository: PracticeRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, PracticeDatabase::class.java
        ).allowMainThreadQueries().build()

        practiceDao = database.practiceDao()
        categoryDao = database.categoryDao()
        sessionDao = database.sessionDao()
        repository = PracticeRepository(database)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetCategory() = runBlocking {
        // Arrange
        val categoryName = "Test Category"
        val categoryColor = 0xFF2196F3

        // Act
        repository.addNewCategory(categoryName, categoryColor)
        val categories = repository.getAllCategories().first()

        // Assert
        assertThat(categories.size).isEqualTo(1)
        assertThat(categories[0].name).isEqualTo(categoryName)
        assertThat(categories[0].color).isEqualTo(categoryColor)
    }

    @Test
    fun insertAndGetPractice() = runBlocking {
        // Arrange
        val categoryId = repository.addNewCategory("Test Category", 0xFF2196F3)
        val practiceName = "Test Practice"
        val practiceDescription = "Description"

        // Act
        repository.addNewPractice(practiceName, practiceDescription, categoryId.toInt())
        val practices = repository.getAllPractices().first()

        // Assert
        assertThat(practices.size).isEqualTo(1)
        assertThat(practices[0].name).isEqualTo(practiceName)
        assertThat(practices[0].description).isEqualTo(practiceDescription)
        assertThat(practices[0].category.id).isEqualTo(categoryId.toInt())
    }

    @Test
    fun addAndGetPracticeSession() = runBlocking {
        // Arrange
        val categoryId = repository.addNewCategory("Test Category", 0xFF2196F3)
        val practiceId = repository.addNewPractice("Test Practice", "Description", categoryId.toInt())
        val duration = 30
        val startingBpm = 80
        val achievedBpm = 85

        // Act
        repository.addPracticeSession(practiceId.toInt(), duration, startingBpm, achievedBpm)
        val practice = repository.getPracticeById(practiceId.toInt()).first()

        // Assert
        assertThat(practice).isNotNull()
        assertThat(practice!!.sessions.size).isEqualTo(1)
        assertThat(practice.sessions[0].duration).isEqualTo(duration)
        assertThat(practice.sessions[0].startingBpm).isEqualTo(startingBpm)
        assertThat(practice.sessions[0].achievedBpm).isEqualTo(achievedBpm)
    }
}