package org.jbpm.integration.console.graph.test;

import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PasswordEncryptionTest {

    @Before
    public void setup() {

    }

    @After
    public void teardown() {

    }

    @Test
    public void testBasicPasswordEncryption() {
        String pwd = "admin";
        String encpwd = "droolsGuvnor";

        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(encpwd);
        String myEncryptedText = textEncryptor.encrypt(pwd);
        String plainText = textEncryptor.decrypt(myEncryptedText);

        assertEquals(plainText, pwd);


        BasicTextEncryptor secondEncryptor = new BasicTextEncryptor();
        secondEncryptor.setPassword(encpwd);
        String otherPlainText = textEncryptor.decrypt(myEncryptedText);
        assertEquals(otherPlainText, pwd);
    }
}
