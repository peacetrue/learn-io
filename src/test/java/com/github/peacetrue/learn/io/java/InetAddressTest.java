package com.github.peacetrue.learn.io.java;

import com.github.peacetrue.spring.beans.BeanUtils;
import com.github.peacetrue.util.MapUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * 测试 {@link InetAddress}。
 *
 * @author peace
 **/
@Slf4j
public class InetAddressTest {

    @Test
    @SneakyThrows
    void getLoopbackAddress() {
        InetAddress loopbackAddress = InetAddress.getLoopbackAddress();
        Map<String, Object> properties = BeanUtils.getPropertyValues(loopbackAddress);
        log.info("properties.size(): {}", properties.size());
        log.info("loopbackAddress: {}", MapUtils.prettify(properties));
    }

    @Test
    @SneakyThrows
    void getLocalHost() {
        InetAddress localHost = InetAddress.getLocalHost();
        Map<String, Object> properties = BeanUtils.getPropertyValues(localHost);
        log.info("properties.size(): {}", properties.size());
        log.info("localHost: {}", MapUtils.prettify(properties));
    }

    @Test
    @SneakyThrows
    void properties() {
        // 任意地址
        Assertions.assertTrue(InetAddress.getByName("0.0.0.0").isAnyLocalAddress());
        Assertions.assertTrue(InetAddress.getByName("::").isAnyLocalAddress());
        // 环回地址
        Assertions.assertTrue(InetAddress.getByName("127.0.0.1").isLoopbackAddress());
        Assertions.assertTrue(InetAddress.getByName("::1").isLoopbackAddress());
        // 链路本地地址
        Assertions.assertTrue(InetAddress.getByName("169.254.0.0").isLinkLocalAddress());
        Assertions.assertTrue(InetAddress.getByName("FE80::").isLinkLocalAddress());
        // 站点本地地址，已废除，由 UniqueLocalAddress 取代
        Assertions.assertTrue(InetAddress.getByName("FEC0::").isSiteLocalAddress());
        // 组播地址
        Assertions.assertTrue(InetAddress.getByName("224.0.0.0").isMulticastAddress());
        Assertions.assertTrue(InetAddress.getByName("FF01::").isMCNodeLocal());
        Assertions.assertTrue(InetAddress.getByName("224.0.0.0").isMCLinkLocal());
        Assertions.assertTrue(InetAddress.getByName("FF02::").isMCLinkLocal());
        Assertions.assertTrue(InetAddress.getByName("FF05::").isMCSiteLocal());
        Assertions.assertTrue(InetAddress.getByName("FF08::").isMCOrgLocal());
        Assertions.assertTrue(InetAddress.getByName("224.0.1.0").isMCGlobal());
        Assertions.assertTrue(InetAddress.getByName("FF0E::").isMCGlobal());

        InetAddress address = InetAddress.getByName("127.0.0.1");
        Assertions.assertTrue(address.isReachable(1_000));
        Assertions.assertEquals("127.0.0.1", address.getHostAddress());
        Assertions.assertArrayEquals(new byte[]{127, 0, 0, 1}, address.getAddress());
        Assertions.assertEquals("localhost", address.getHostName());
        // CNAME: https://superuser.com/questions/394816/what-is-the-difference-and-relation-between-host-name-and-canonical-name
        Assertions.assertEquals("localhost", address.getCanonicalHostName());
    }

    @Test
    @SneakyThrows
    void getByName() {
        InetAddress.getByName("localhost");
        Assertions.assertThrows(
                UnknownHostException.class,
                () -> InetAddress.getByName("localhost1"),
                "指定一个不存在的主机"
        );
    }

    @Test
    @SneakyThrows
    void getByAddress() {
        InetAddress localhost = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        log.info("{}: {}", localhost.getClass().getName(), localhost);
        Assertions.assertTrue(localhost.isReachable(1_000));

        // byte 范围从 -128 ~ 127，如何指定 192.168.0.1 呢？
        // https://stackoverflow.com/questions/9850199/java-network-programming-inetaddress-objects
        InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, 1, 5});
        Assertions.assertThrows(
                UnknownHostException.class,
                () -> InetAddress.getByAddress(new byte[]{127, 0, 0, 1, 1}),
                "指定一个错误的长度"
        );
    }

    @Test
    void byteOverflow() {
        IntStream.range(127, 130).forEach(i -> log.debug("{}: {}", i, (byte) i));
        // 127: 127
        // 128: -128
        // 129: -127

        // 192: 192 - 127 - 1 + (-128) = 192 + 2 * -128
        int num = 192;
        Assertions.assertEquals((byte) num, num + 2 * Byte.MIN_VALUE);
    }
}
