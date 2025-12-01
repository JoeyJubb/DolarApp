import com.joe.dolarApp.TestDispatcherProvider
import com.joe.dolarApp.util.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule

/** https://github.com/marcinOz/TestCoroutineRule **/
@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineTestRule private constructor(
  val testCoroutineDispatcher: TestDispatcher,
) : TestRule {

  val testScheduler: TestCoroutineScheduler = testCoroutineDispatcher.scheduler
  val testScope: TestScope = TestScope(testCoroutineDispatcher)
  val testDispatcherProvider: DispatcherProvider = object : DispatcherProvider{
    override val main: CoroutineDispatcher = testCoroutineDispatcher
    override val io: CoroutineDispatcher = testCoroutineDispatcher
    override val unconfined: CoroutineDispatcher = testCoroutineDispatcher
  }

  override fun apply(base: org.junit.runners.model.Statement, description: org.junit.runner.Description?) = object : org.junit.runners.model.Statement() {
    override fun evaluate() {
      try {
        initCoroutines()

        base.evaluate()
      } finally {
        teardownCoroutines()
      }
    }
  }

  private fun initCoroutines() {
    Dispatchers.setMain(testCoroutineDispatcher)
  }

  private fun teardownCoroutines() {
    Dispatchers.resetMain()
  }

  /**
   * Executes [kotlinx.coroutines.test.runTest] on [testScope]
   */
  fun runTest(block: suspend TestScope.() -> Unit) = testScope.runTest(testBody = block)

  fun advanceTimeBy(delayTimeMs: Long) = testScheduler.advanceTimeBy(delayTimeMs)

  fun advanceUntilIdle() = testScheduler.advanceUntilIdle()

  fun runCurrent() = testScheduler.runCurrent()

  companion object {
    private val pausesAsyncOperations get() = StandardTestDispatcher()
    private val executesAsyncOperations get() = UnconfinedTestDispatcher()

    /**
     *  [kotlinx.coroutines.launch] and [kotlinx.coroutines.async] will not be entered immediately (unless they are
     *  parameterized with [kotlinx.coroutines.CoroutineStart.UNDISPATCHED]), and one should either call [runCurrent]
     *  to run these pending tasks, which will block until there are no more tasks scheduled at this point in time,
     *  or, when inside runTest, call [kotlinx.coroutines.yield] to yield the (only) thread used by [TestScope.runTest]
     *  to the newly-launched coroutines.
     */
    fun asyncOperationsPaused(): CoroutineTestRule {
      return CoroutineTestRule(pausesAsyncOperations)
    }

    /**
     * [kotlinx.coroutines.launch] and [kotlinx.coroutines.async] blocks at the top level of
     * [TestScope.runTest] are entered eagerly.
     *
     * This allows launching child coroutines and not calling [runCurrent] for them to start executing.
     */
    fun asyncOperationsExecuted(): CoroutineTestRule {
      return CoroutineTestRule(executesAsyncOperations)
    }
  }
}