package ltd.evilcorp.atox

import ltd.evilcorp.atox.tox.PublicKey
import ltd.evilcorp.atox.tox.ToxID
import org.junit.Assert.assertEquals
import org.junit.Test

class ToxTypesTest {
    @Test
    fun converting_tox_ids_to_public_keys_works() {
        val id = ToxID("76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6")
        val publicKey = PublicKey("76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39")
        assertEquals(publicKey, id.toPublicKey())
    }
}
