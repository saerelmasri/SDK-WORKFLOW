package com.liblab.hook;

import com.liblab.hook.model.Hook;
import com.liblab.hook.model.Request;
import com.liblab.hook.model.Response;
import java.util.HashMap;
import java.util.Map;

public class CustomHook implements Hook {

    @Override
    public void beforeRequest(Request request) {
        if (request.getUrl().endsWith("/oauth/token")) {
            return;
        }

        // Get the client_id and client_secret from environment variables
        String clientId = System.getenv("CLIENT_ID");
        String clientSecret = System.getenv("CLIENT_SECRET");

        if (clientId == null || clientSecret == null || clientId.isEmpty() || clientSecret.isEmpty()) {
            System.err.println("Missing CLIENT_ID and/or CLIENT_SECRET environment variables");
            System.err.println("Url: " + request.getUrl());
            return;
        }

        // Check if CURRENT_TOKEN is missing or CURRENT_EXPIRY is in the past
        if (CURRENT_TOKEN == null || CURRENT_EXPIRY < System.currentTimeMillis()) {
            Celitech sdk = new Celitech(Environment.TOKEN_SERVER);

            // Prepare the request payload for fetching a fresh OAuth token
            Map<String, String> input = new HashMap<>();
            input.put("client_id", clientId);
            input.put("client_secret", clientSecret);
            input.put("grant_type", "client_credentials");

            // Fetch a fresh OAuth token
            // Retrieve the new access token and expiry, and set them to the global variables
            try {
                Request tokenRequest = new Request();
                tokenRequest.setUrl(sdk.getTokenEndpoint());
                tokenRequest.setMethod("POST");
                tokenRequest.setPayload(input);
                Response tokenResponse = doPost(tokenRequest);

                if (tokenResponse != null) {
                    Map<String, Object> tokenData = tokenResponse.getData();
                    Object expiresInObj = tokenData.get("expires_in");
                    Object accessTokenObj = tokenData.get("access_token");

                    if (expiresInObj instanceof Number && accessTokenObj instanceof String) {
                        long expires_in = ((Number) expiresInObj).longValue();
                        String access_token = (String) accessTokenObj;

                        if (expires_in > 0 && access_token != null && !access_token.isEmpty()) {
                            CURRENT_EXPIRY = System.currentTimeMillis() + expires_in * 1000;
                            CURRENT_TOKEN = access_token;
                        } else {
                            System.err.println("There is an issue with getting the OAuth token");
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in getting the OAuth token: " + e.getMessage());
                return;
            }
        }

        // Set the Bearer token in the request header
        String authorization = "Bearer " + CURRENT_TOKEN;
        request.getHeaders().put("Authorization", authorization);
    }

    private Response doPost(Request request) {
        // Implement HTTP POST logic here
        // You can use libraries like HttpClient or HttpURLConnection

        try {
            // Sample code using HttpURLConnection
            URL url = new URL(request.getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            // Write payload to the connection output stream if needed

            // Read response
            InputStream inputStream = connection.getInputStream();
            // Process input stream and convert it to Response object
            // ...

            // Close resources
            inputStream.close();
            connection.disconnect();

            // Return the Response object
            return new Response(/* fill with appropriate data */);
        } catch (IOException e) {
            System.err.println("Error in posting the request: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void afterResponse(Request request, Response response) {
        // no-op
    }

    @Override
    public void onError(Request request, Exception exception) {
        // no-op
    }
}
