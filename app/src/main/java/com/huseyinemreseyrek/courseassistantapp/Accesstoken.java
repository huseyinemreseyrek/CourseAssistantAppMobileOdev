package com.huseyinemreseyrek.courseassistantapp;

import android.util.Log;

import com.google.api.client.util.Lists;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class Accesstoken {

    private final static String firebaseMessagingScope="https://www.googleapis.com/auth/firebase.messaging";
    public String getAccessToken(){
        try{

            String jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"mobilodev-69e86\",\n" +
                    "  \"private_key_id\": \"7bd6f54185dcab120d91705081c502548461bdcb\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDJZ0bQo5HjSC49\\n/FB3NkvDADnIziqjg2MqbR/+stjhHK4oiNtlKFREz5XSgm5tja3maWS/IK6SCv63\\n7YFE8rKgQi5+MBCNvm0qHdj51wxNWVSxmp+jjbau6uBwGr7M2TJqjrIdi0JfBahO\\noEgnRugUPiRoXi11SWkTEFOx/IyBqOs7io/yKsWThIv2RgzRxhaonuptZcqNwGiu\\nLhVJA20upKcDSQLiMWkTQ/MTyPM9tbs9c5VqBZohfQMdTNx7q2NesG6T/D5E/GTf\\nkoJ4I2pOwH73ZY08LtQWbyujbNuepZaiC3XDVwJ5y1nY5PpY3M7gW8w+RXTlRHKN\\n8xMFkfFHAgMBAAECggEAXKTbr8yWJqEE2HGQiA3bUtNvQT4I4hdljdYNsikN15tM\\nR4wQs9sVzGNazS8/Ybia5lLTy5FajAZvviaL4K3tWgengFdWvgT7DTUWonJ4D7qI\\nWXxBc8gT4Slc3WSIIQ3GAbsrFqkA+lnNXoGfPyYASpN9Rt8PdRS0N1c60LRV46wB\\nZAmgzXgjzBsY7Z0hJvOH6Wq9/6a100aAGhGSvDn2yQ8coFEFL3Ob6vVACeHQrGk5\\n1ZLuLA9W4i29twNhkF/AFG14wEhqe/zPWDh92u53eko9/qN2ULO4FbTeByEqK04U\\npHWnpi320aZo63Kb0PUVeVmT7wyRQUXkQ09IbCbvvQKBgQDxQR9S1svGlZcH2xdK\\nZDPOq5F5pU8o9lzxalJLHBue9AJIt600xRD/puQNkhUEAOrfh9o6iaNys7UhpW92\\n5Leq+nJNn8XoNu4UU0vMGJBK3kTmfRawHVZ4ebUTbqha2uyzaQ82h/ncMdROTzah\\nvuS83H73MkDjf4/c0LSzDAPh4wKBgQDVtpx3pzuKTq6cvUIFYaW0CQDlAI2kAeY0\\nE6yaJJPYrq0sM9NuWwIx0oE8UEkj7ouD7SnO2ZgVA5oIuD6+H6juzTs+hT04hWGP\\n0yerdBWFOZXULTTf8mcWMR07GFBfy43IiEaN3AZVPXrYwfcCPRX6s4QzQS5gBLkK\\n97w31q0ATQKBgFh0lSSNZuen31ldBjwbYzdqhotAhjaxpcBKSGQLqUDtnIzG87W+\\nb44cxc8H+bafE37j06rU3l3m8AmnG6hPEdyZuSEm7KxIv6AqLkdl1jntJvwPbysh\\nhpvjm/XFA+tUC6d4ZMCMuJr4liwkNRa25C0u90pxXYNMORYwW6CuThobAoGAeCPi\\nLfmbJRM6Ye6a8L+GEYGGOTZoaayPpvLwcu7hFkyMGW9BCqaqwytdb3SKyWJOl+l5\\nOUllj1qo+wHoc6UEqMpC5dCtK9r/j4Tapi3p8yz+J3sKYGK6xOTzU8dFV7TsjbDe\\nSN1Jyh13s+bGX0HL21WTmeSol13QUHQYISPTuXkCgYAp7vR42IYeHjfqb2ZkB92K\\ngdtR6fIus2xkcbwtY8hfIQ+IU+4Laq6RtlZMvau7lwTKFq1+EXmvqGURGfz6mQyV\\nDNcpQ+iwlSm3kj41qEhMgf4qP9lajplk7X/xoKZEgvDccWK5PnPDwj4dqyiNwgfh\\njvBhXCAQksmKNy4eamVpRQ==\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-uoi2c@mobilodev-69e86.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"100691366449230886321\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-uoi2c%40mobilodev-69e86.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}";

            InputStream stream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Collections.singletonList(firebaseMessagingScope));
            googleCredentials.refresh();
            return googleCredentials.getAccessToken().getTokenValue();



        } catch (IOException e) {
            Log.e("error", e.getMessage());
            return null;
        }
    }
    }
