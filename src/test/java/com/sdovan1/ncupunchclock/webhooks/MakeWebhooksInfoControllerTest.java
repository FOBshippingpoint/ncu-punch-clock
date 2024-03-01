package com.sdovan1.ncupunchclock.webhooks;

import com.sdovan1.ncupunchclock.user.CustomUserDetails;
import com.sdovan1.ncupunchclock.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
class MakeWebhooksInfoControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private WebhooksInfoRepository webhooksKeyRepository;

    @Autowired
    MockMvc mockMvc;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setup() {
        var user = new User();
        user.setUsername("george");
        var webhooksKey = new WebhooksInfo();
        webhooksKey.setInfo("someKey");
        webhooksKey.setUser(user);

        given(this.webhooksKeyRepository.findByUser(any(User.class))).willReturn(Optional.of(webhooksKey));
        userDetails = new CustomUserDetails(user);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @Test
    void testInitUpdateForm() throws Exception {
        mockMvc.perform(get("/change_make_webhooks").with(user("george").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("makeWebhooksInfoDTO"))
                .andExpect(view().name("change_make_webhooks"));
    }

    @Test
    void testAuthorizationRequired() throws Exception {
        mockMvc.perform(get("/change_make_webhooks"))
                .andExpect(status().isFound());
    }

    @Test
    void testProcessUpdateForm() throws Exception {
        mockMvc.perform(post("/change_make_webhooks").param("url", "https://example.com").with(csrf()).with(user(userDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("notification", "Make Webhooks已更新，請確認您選擇的App是否收到通知測試。"))
                .andExpect(view().name("change_make_webhooks"));
    }

    @Test
    void testProcessUpdateFormFailed() throws Exception {
        mockMvc.perform(post("/change_make_webhooks").param("url", "").with(csrf()).with(user(userDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("notification", "請輸入Make Webhooks URL"))
                .andExpect(view().name("change_make_webhooks"));
    }
}