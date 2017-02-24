import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import seravifer.apipoliformat.model.ApiPoliformat
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Created by David on 23/02/2017.
 */
object ApiPoliformaTSpek : Spek({
    describe("a PoliformaT repository") {
        val poliformat = ApiPoliformat

        it ("should fail to login") {
            assertFalse { poliformat.login("9999", "9999") }
        }

        it ("should achieve to login") {
            val dni = System.getProperty("DNI")
            val pin = System.getProperty("PIN")
            assertTrue { poliformat.login(dni, pin) }
        }
    }
})