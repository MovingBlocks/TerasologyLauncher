package org.terasology.launcher.net;

import com.softwaremill.sttp.Response;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletionStage;
import static org.junit.Assert.assertEquals;


public class ScalaNetworkTest {
    Logger logger = LoggerFactory.getLogger(ScalaNetworkTest.class);

    @Test
    public void testScalaConnect() throws URISyntaxException {
        URI uri = new URI("jenkins.terasology.org/api/json");

        final CompletionStage<Response<String>> call = Network.connect(uri);
        call.whenComplete((response, error) -> {
            assertEquals(response.code(), 201);
            logger.error("SUCCESS: {}\n{}", response.code(), response.unsafeBody());
        });
    }
}
