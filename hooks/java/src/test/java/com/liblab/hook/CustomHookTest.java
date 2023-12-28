package com.liblab.hook;
import com.liblab.hook.model.Request;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;;

public class CustomHookTest {

    @Before
    public void setUp() {
    }

    @Test
    public void testBeforeRequest() {

        CustomHook hook = new CustomHook();

        Request request = new Request("GET", "https://api.example.com", "", new HashMap<String, String>() {{
            put("Content-Type", "application/json");
        }});

        hook.beforeRequest(request);

        System.out.println(request.getHeaders());

        // Assert the header is present
        assertTrue(request.getHeaders().containsKey("Content-Type"));
        assertEquals(request.getHeaders().get("Content-Type"), "application/json");
    }
}
