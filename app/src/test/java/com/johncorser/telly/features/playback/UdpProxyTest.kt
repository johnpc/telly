package com.johncorser.telly.features.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class UdpProxyTest {
    @Test
    fun `a blank proxy - the captured default - never rewrites`() {
        assertEquals("udp://@239.1.2.3:1234", UdpProxy.resolve("", "udp://@239.1.2.3:1234"))
        assertEquals("udp://@239.1.2.3:1234", UdpProxy.resolve("   ", "udp://@239.1.2.3:1234"))
    }

    @Test
    fun `udp multicast rewrites to the udpxy http form`() {
        assertEquals(
            "http://192.168.1.10:4022/udp/239.1.2.3:1234",
            UdpProxy.resolve("192.168.1.10:4022", "udp://@239.1.2.3:1234"),
        )
    }

    @Test
    fun `rtp maps to the rtp udpxy path`() {
        assertEquals(
            "http://proxy:4022/rtp/239.1.2.3:5500",
            UdpProxy.resolve("proxy:4022", "rtp://239.1.2.3:5500"),
        )
    }

    @Test
    fun `the at-sign after the scheme is optional and case is normalized`() {
        assertEquals(
            "http://proxy:4022/udp/239.1.2.3:1234",
            UdpProxy.resolve("proxy:4022", "UDP://239.1.2.3:1234"),
        )
    }

    @Test
    fun `http and hls streams pass through untouched`() {
        assertEquals("http://s/1.ts", UdpProxy.resolve("proxy:4022", "http://s/1.ts"))
        assertEquals("https://s/live.m3u8", UdpProxy.resolve("proxy:4022", "https://s/live.m3u8"))
    }

    @Test
    fun `a trailing slash on the proxy does not double up`() {
        assertEquals(
            "http://proxy:4022/udp/239.1.2.3:1234",
            UdpProxy.resolve("proxy:4022/", "udp://@239.1.2.3:1234"),
        )
    }
}
