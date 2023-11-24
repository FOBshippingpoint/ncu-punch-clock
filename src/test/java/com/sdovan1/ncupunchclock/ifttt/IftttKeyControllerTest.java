package com.sdovan1.ncupunchclock.ifttt;

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

//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = SecurityConfig.class)
//@WebAppConfiguration
//@WebMvcTest(IftttKeyController.class)
@SpringBootTest
@AutoConfigureMockMvc
class IftttKeyControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private IftttKeyRepository iftttKeyRepository;

    @MockBean
    private IftttWebhooksPublisher iftttWebhooksPublisher;

    @Autowired
    MockMvc mockMvc;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setup() {
        var user = new User();
        user.setUsername("george");
        var iftttKey = new IftttKey();
        iftttKey.setIftttWebhooksKey("someKey");
        iftttKey.setUser(user);

        given(this.iftttKeyRepository.findByUser(any(User.class))).willReturn(Optional.of(iftttKey));
        userDetails = new CustomUserDetails(user);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

//        Owner george = george();
//        given(this.owners.findByLastName(eq("Franklin"), any(Pageable.class)))
//                .willReturn(new PageImpl<Owner>(Lists.newArrayList(george)));
//
//        given(this.owners.findAll(any(Pageable.class))).willReturn(new PageImpl<Owner>(Lists.newArrayList(george)));
//
//        given(this.owners.findById(TEST_OWNER_ID)).willReturn(george);
//        Visit visit = new Visit();
//        visit.setDate(LocalDate.now());
//        george.getPet("Max").getVisits().add(visit);
    }


    @Test
    void testInitUpdateForm() throws Exception {
        mockMvc.perform(get("/change_ifttt_key").with(user("george").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("iftttKeyDTO"))
                .andExpect(view().name("change_ifttt_key"));
    }

    @Test
    void testAuthorizationRequired() throws Exception {
        mockMvc.perform(get("/change_ifttt_key"))
                .andExpect(status().isFound());
    }

    @Test
    void testProcessUpdateForm() throws Exception {
        mockMvc.perform(post("/change_ifttt_key").param("iftttWebhooksKey", "someKey").with(csrf()).with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("notification"))
                .andExpect(view().name("change_ifttt_key"));
    }

}