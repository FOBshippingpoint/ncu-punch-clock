package com.sdovan1.ncupunchclock.ifttt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest
class IftttWebhooksPublisherTest {
    @MockBean
    private IftttKeyRepository iftttKeyRepository;

//    @Autowired
//    private MockRestServiceServer server;


//    @Test
//    void testTrigger() {
//        MockRestServiceServer.createServer(restTemplate)
//                .expect(requestTo("https://maker.ifttt.com/trigger/ncu_punch_clock/with/key/12345"))
//                .andRespond(withSuccess());
//
//        var publisher = new IftttWebhooksPublisher(iftttKeyRepository);
//        assertThatNoException().isThrownBy(() -> {
//            publisher.trigger("someKey", "someMessage");
//        });
//    }
}