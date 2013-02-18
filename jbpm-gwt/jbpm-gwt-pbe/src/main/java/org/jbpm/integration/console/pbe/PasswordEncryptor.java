package org.jbpm.integration.console.pbe;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PasswordEncryptor {
    public static void main(String[] args) {
        System.out.print("Enter clear text secret: ");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String cpwd = null;
        try {
            cpwd = br.readLine();
        } catch (IOException ioe) {
            System.out.println("IO error trying to read your input!");
            System.exit(1);
        }

        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("droolsGuvnor");
        encryptor.setAlgorithm("PBEWithMD5AndTripleDES");

        String encryptedPassword = encryptor.encrypt(cpwd);

        System.out.print("Encrypted secret:" + encryptedPassword);
    }
}
