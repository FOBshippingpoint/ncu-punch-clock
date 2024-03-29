package com.sdovan1.ncupunchclock.bot;

import com.sdovan1.ncupunchclock.schedule.LoginFailedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PlaywrightAgentTest {
    static boolean isUnderNcuNetwork;

    @BeforeAll
    static void setUp() {
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            var ip = socket.getLocalAddress().getHostAddress();
            isUnderNcuNetwork = ip.startsWith("140.115");
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testLoginCheck() {
        var agent = new PlaywrightAgent(null, "username", "password", null, true);
        if (isUnderNcuNetwork){
            assertThatExceptionOfType(LoginFailedException.class).isThrownBy(agent::loginCheck).withMessage(LoginFailedException.WRONG_USERNAME_OR_PASSWORD);
        } else {
            assertThatExceptionOfType(LoginFailedException.class).isThrownBy(agent::loginCheck).withMessage(LoginFailedException.FAILED_TO_PASS_CAPTCHA);
        }
    }
}