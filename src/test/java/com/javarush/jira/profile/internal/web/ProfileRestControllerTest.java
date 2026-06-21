package com.javarush.jira.profile.internal.web;

import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.profile.ContactTo;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import com.javarush.jira.profile.internal.model.Contact;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Set;

import static com.javarush.jira.common.util.JsonUtil.writeValue;
import static com.javarush.jira.login.internal.web.UserTestData.*;
import static com.javarush.jira.profile.internal.web.ProfileRestController.REST_URL;
import static com.javarush.jira.profile.internal.web.ProfileTestData.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileRestControllerTest extends AbstractControllerTest {

    @Autowired
    private ProfileRepository profileRepository;

    // --- GET ---

    @Test
    @WithUserDetails(value = USER_MAIL)
    void get() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(PROFILE_TO_MATCHER.contentJson(withUserId(USER_ID, USER_PROFILE_TO)));
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void getAdmin() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(PROFILE_TO_MATCHER.contentJson(withUserId(ADMIN_ID, ADMIN_PROFILE_TO)));
    }

    @Test
    @WithUserDetails(value = GUEST_MAIL)
    void getGuestWithoutProfile() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(PROFILE_TO_MATCHER.contentJson(withUserId(GUEST_ID, GUEST_PROFILE_EMPTY_TO)));
    }

    @Test
    void getUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isUnauthorized());
    }

    // --- PUT ---

    @Test
    @WithUserDetails(value = USER_MAIL)
    void update() throws Exception {
        ProfileTo updatedTo = getUpdatedTo();
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(updatedTo)))
                .andDo(print())
                .andExpect(status().isNoContent());

        Profile dbProfile = profileRepository.getExisted(USER_ID);
        PROFILE_MATCHER.assertMatch(dbProfile, getUpdated(USER_ID));
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void updateAdmin() throws Exception {
        ProfileTo updatedTo = new ProfileTo(null,
                Set.of("assigned", "deadline"),
                Set.of(new ContactTo("github", "newAdminGitHub"),
                        new ContactTo("linkedin", "newAdminLinkedin")));
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(updatedTo)))
                .andDo(print())
                .andExpect(status().isNoContent());

        Profile expected = new Profile();
        expected.setId(ADMIN_ID);
        expected.setMailNotifications(17);
        expected.setContacts(Set.of(
                new Contact(ADMIN_ID, "github", "newAdminGitHub"),
                new Contact(ADMIN_ID, "linkedin", "newAdminLinkedin")));
        PROFILE_MATCHER.assertMatch(profileRepository.getExisted(ADMIN_ID), expected);
    }

    @Test
    @WithUserDetails(value = GUEST_MAIL)
    void updateCreateProfile() throws Exception {
        ProfileTo newTo = getNewTo();
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newTo)))
                .andDo(print())
                .andExpect(status().isNoContent());

        PROFILE_MATCHER.assertMatch(profileRepository.getExisted(GUEST_ID), getNew(GUEST_ID));
    }

    @Test
    void updateUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getUpdatedTo())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateInvalid() throws Exception {
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getInvalidTo())))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateUnknownNotification() throws Exception {
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getWithUnknownNotificationTo())))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString("WrongNotification")));
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateUnknownContact() throws Exception {
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getWithUnknownContactTo())))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString("WrongContactCode")));
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateContactHtmlUnsafe() throws Exception {
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getWithContactHtmlUnsafeTo())))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateIdConflict() throws Exception {
        ProfileTo conflict = getUpdatedTo();
        conflict.setId(ADMIN_ID);
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(conflict)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString("ProfileTo must has id=" + USER_ID)));
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateContactIdConflict() throws Exception {
        String json = """
                {"mailNotifications":["assigned"],"contacts":[{"id":%d,"code":"skype","value":"newSkype"}]}
                """.formatted(ADMIN_ID);
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString("ContactTo must has id=" + USER_ID)));
    }
}
