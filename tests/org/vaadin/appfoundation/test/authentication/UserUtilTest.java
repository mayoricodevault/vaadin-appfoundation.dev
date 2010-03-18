package org.vaadin.appfoundation.test.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Properties;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.authentication.util.PasswordUtil;
import org.vaadin.appfoundation.authentication.util.UserUtil;
import org.vaadin.appfoundation.authentication.util.UserUtil.ProfileMsg;
import org.vaadin.appfoundation.authentication.util.UserUtil.RegistrationMsg;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class UserUtilTest {

    @Before
    public void setUp() throws InstantiationException, IllegalAccessException {
        try {
            Properties properties = new Properties();
            properties.setProperty("password.salt", "test");
            PasswordUtil.setProperties(properties);
        } catch (UnsupportedOperationException e) {
            // The properties for the PasswordUtil has already been set. Ignore
            // this.
        }

        FacadeFactory.registerFacade("default", true);
    }

    @After
    public void tearDown() {
        FacadeFactory.clear();
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPropertiesWithNull() {
        UserUtil.setProperties(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPropertiesMissingProperty() {
        UserUtil.setProperties(new Properties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPropertiesInvalidUsernameLenght() {
        Properties properties = new Properties();
        properties.setProperty("username.length.min", "test");
        properties.setProperty("password.length.min", "3");
        UserUtil.setProperties(properties);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPropertiesInvalidPasswordLenght() {
        Properties properties = new Properties();
        properties.setProperty("username.length.min", "3");
        properties.setProperty("password.length.min", "test");
        UserUtil.setProperties(properties);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPropertiesMissingUsername() {
        Properties properties = new Properties();
        properties.setProperty("password.length.min", "3");
        UserUtil.setProperties(properties);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPropertiesMissingPassword() {
        Properties properties = new Properties();
        properties.setProperty("username.length.min", "3");
        UserUtil.setProperties(properties);
    }

    @Test
    public void setProperties() {
        Properties properties = new Properties();
        properties.setProperty("username.length.min", "5");
        properties.setProperty("password.length.min", "3");
        UserUtil.setProperties(properties);

        assertEquals(5, UserUtil.getMinUsernameLength());
        assertEquals(3, UserUtil.getMinPasswordLength());
    }

    @Test
    public void setPropertiesTwice() {
        Properties properties = new Properties();
        properties.setProperty("username.length.min", "5");
        properties.setProperty("password.length.min", "3");
        UserUtil.setProperties(properties);

        // Properties is already set
        Properties properties2 = new Properties();
        properties2.setProperty("username.length.min", "3");
        properties2.setProperty("password.length.min", "5");
        UserUtil.setProperties(properties2);

        assertEquals(3, UserUtil.getMinUsernameLength());
        assertEquals(5, UserUtil.getMinPasswordLength());
    }

    @Test
    public void storeUser() {
        User user = new User();
        assertNull(user.getId());

        UserUtil.storeUser(user);
        assertNotNull(user.getId());
    }

    @Test
    public void registerUserNullUsername() {
        assertEquals(RegistrationMsg.TOO_SHORT_USERNAME, UserUtil.registerUser(
                null, "test1", "test1"));
    }

    @Test
    public void registerUserShortUsername() {
        assertEquals(RegistrationMsg.TOO_SHORT_USERNAME, UserUtil.registerUser(
                "a", "test1", "test1"));
    }

    @Test
    public void registerUserNullPassword() {
        assertEquals(RegistrationMsg.TOO_SHORT_PASSWORD, UserUtil.registerUser(
                "test", null, null));
    }

    @Test
    public void registerUserShortPassword() {
        assertEquals(RegistrationMsg.TOO_SHORT_PASSWORD, UserUtil.registerUser(
                "test", "a", "a"));
    }

    @Test
    public void registerUserIncompatiblePassword() {
        assertEquals(RegistrationMsg.PASSWORDS_DO_NOT_MATCH, UserUtil
                .registerUser("test", "test1", "test2"));
    }

    @Test
    public void registerUserUsernameTaken() {
        User user = new User();
        user.setUsername("test");
        FacadeFactory.getFacade().store(user);

        assertEquals(RegistrationMsg.USERNAME_TAKEN, UserUtil.registerUser(
                "test", "test1", "test1"));
    }

    @Test
    public void registerUser() {
        assertEquals(RegistrationMsg.REGISTRATION_COMPLETED, UserUtil
                .registerUser("test", "test1", "test1"));
    }

    @Test
    public void getUser() {
        User user = new User();
        user.setUsername(UUID.randomUUID().toString());
        FacadeFactory.getFacade().store(user);

        User user2 = UserUtil.getUser(user.getId());
        assertEquals(user.getUsername(), user2.getUsername());
    }

    @Test
    public void changePasswordIncorrectOld() {
        User user = new User();
        user.setUsername("test");
        // Hashed value of "foobar"+"test" (the salt value)
        user.setPassword("61e38e2b77827e10777ee8f1a138b7cfb1eb895");

        assertEquals(ProfileMsg.WRONG_PASSWORD, UserUtil.changePassword(user,
                "test", null, null));
    }

    @Test
    public void changePasswordTooShortNew() {
        User user = new User();
        user.setUsername("test");
        // Hashed value of "foobar"+"test" (the salt value)
        user.setPassword("61e38e2b77827e10777ee8f1a138b7cfb1eb895");

        assertEquals(ProfileMsg.TOO_SHORT_PASSWORD, UserUtil.changePassword(
                user, "foobar", "a", "a"));
    }

    @Test
    public void changePasswordNullNew() {
        User user = new User();
        user.setUsername("test");
        // Hashed value of "foobar"+"test" (the salt value)
        user.setPassword("61e38e2b77827e10777ee8f1a138b7cfb1eb895");

        assertEquals(ProfileMsg.TOO_SHORT_PASSWORD, UserUtil.changePassword(
                user, "foobar", null, null));
    }

    @Test
    public void changePasswordNotMatch() {
        User user = new User();
        user.setUsername("test");
        // Hashed value of "foobar"+"test" (the salt value)
        user.setPassword("61e38e2b77827e10777ee8f1a138b7cfb1eb895");

        assertEquals(ProfileMsg.PASSWORDS_DO_NOT_MATCH, UserUtil
                .changePassword(user, "foobar", "test1", "test2"));
    }

    @Test
    public void changePassword() {
        User user = new User();
        user.setUsername("test");
        // Hashed value of "foobar"+"test" (the salt value)
        user.setPassword("61e38e2b77827e10777ee8f1a138b7cfb1eb895");

        assertEquals(ProfileMsg.PASSWORD_CHANGED, UserUtil.changePassword(user,
                "foobar", "testing", "testing"));

        // Make sure the new hashed password is correct.
        // Hashed value of "testing"+"test" (the salt value)
        assertEquals("6b399df23c6b76d667f5e043d2dd13407a2245bb", user
                .getPassword());
    }

}