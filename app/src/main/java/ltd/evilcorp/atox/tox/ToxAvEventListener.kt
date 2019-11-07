package ltd.evilcorp.atox.tox

import im.tox.tox4j.av.callbacks.ToxAvEventListener
import im.tox.tox4j.av.enums.ToxavFriendCallState
import scala.Option
import scala.Tuple3
import java.util.*

class ToxAvEventListener : ToxAvEventListener<Unit> {
    override fun call(p0: Int, p1: Boolean, p2: Boolean, p3: Unit?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun videoFrameCachedYUV(
        p0: Int,
        p1: Int,
        p2: Int,
        p3: Int
    ): Option<Tuple3<ByteArray, ByteArray, ByteArray>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bitRateStatus(p0: Int, p1: Int, p2: Int, p3: Unit?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun videoReceiveFrame(
        p0: Int,
        p1: Int,
        p2: Int,
        p3: ByteArray,
        p4: ByteArray,
        p5: ByteArray,
        p6: Int,
        p7: Int,
        p8: Int,
        p9: Unit?
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun callState(p0: Int, p1: EnumSet<ToxavFriendCallState>, p2: Unit?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun audioReceiveFrame(p0: Int, p1: ShortArray, p2: Int, p3: Int, p4: Unit?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
