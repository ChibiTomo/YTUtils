package net.chibidevteam.utils.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import net.chibidevteam.utils.helper.MessageUtils;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ConfigTest.class)
public class MessageUtilsTest {

    @Test
    public void messageTest() {
        System.out.println(System.getenv());
        assertEquals("???hello???", MessageUtils.get("hello"));
        assertEquals("Hello World", MessageUtils.get("hello.world", Locale.ENGLISH));
        assertEquals("Bonjour Monde", MessageUtils.get("hello.world", Locale.CANADA_FRENCH));
        assertEquals("Bien le boujour le Monde", MessageUtils.get("hello.world"));
        assertNotEquals(System.getenv("???java.home???"), MessageUtils.get("java.home"));
        assertNotEquals(System.getenv("${JAVA_HOME}"), MessageUtils.get("java.home"));
    }

}
