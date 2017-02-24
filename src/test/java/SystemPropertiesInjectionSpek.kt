import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

/**
 * Created by David on 24/02/2017.
 */
object SystemPropertiesInjectionSpek : Spek({
    describe("a String injection") {
        it ("should not return null or empty Strings") {
            System.getProperty("DNI").run {
                assertNotNull(this)
                assertNotEquals("", this)
            }
            System.getProperty("PIN").run {
                assertNotNull(this)
                assertNotEquals("", this)
            }
        }
    }
}) {
}