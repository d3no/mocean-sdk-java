package com.mocean.modules.message;

import com.mocean.TestingUtils;
import com.mocean.exception.MoceanErrorException;
import com.mocean.modules.ResponseFactory;
import com.mocean.modules.Transmitter;
import com.mocean.modules.message.mapper.VerifyRequestResponse;
import com.mocean.system.Mocean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doAnswer;

public class VerifyRequestTest {
    private Mocean mocean;

    @BeforeEach
    public void setUp() {
        this.mocean = TestingUtils.getMoceanObj();
    }

    @Test
    public void testSetterMethod() {
        VerifyRequest verifyRequest = this.mocean.verifyRequest();

        verifyRequest.setTo("test to");
        assertNotNull(verifyRequest.getParams().get("mocean-to"));
        assertEquals("test to", verifyRequest.getParams().get("mocean-to"));

        verifyRequest.setBrand("test brand");
        assertNotNull(verifyRequest.getParams().get("mocean-brand"));
        assertEquals("test brand", verifyRequest.getParams().get("mocean-brand"));

        verifyRequest.setFrom("test from");
        assertNotNull(verifyRequest.getParams().get("mocean-from"));
        assertEquals("test from", verifyRequest.getParams().get("mocean-from"));

        verifyRequest.setCodeLength("test code length");
        assertNotNull(verifyRequest.getParams().get("mocean-code-length"));
        assertEquals("test code length", verifyRequest.getParams().get("mocean-code-length"));

        verifyRequest.setTemplate("test template");
        assertNotNull(verifyRequest.getParams().get("mocean-template"));
        assertEquals("test template", verifyRequest.getParams().get("mocean-template"));

        verifyRequest.setPinValidity("test pin validity");
        assertNotNull(verifyRequest.getParams().get("mocean-pin-validity"));
        assertEquals("test pin validity", verifyRequest.getParams().get("mocean-pin-validity"));

        verifyRequest.setNextEventWait("test next event wait");
        assertNotNull(verifyRequest.getParams().get("mocean-next-event-wait"));
        assertEquals("test next event wait", verifyRequest.getParams().get("mocean-next-event-wait"));

        verifyRequest.setRespFormat("json");
        assertNotNull(verifyRequest.getParams().get("mocean-resp-format"));
        assertEquals("json", verifyRequest.getParams().get("mocean-resp-format"));
    }

    @Test
    public void testInquiry() throws IOException, MoceanErrorException {
        Transmitter transmitterMock = spy(Transmitter.class);
        doAnswer(
                new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        assertEquals("post", invocationOnMock.getArgument(0));
                        assertEquals("/verify/req", invocationOnMock.getArgument(1));

                        return new String(Files.readAllBytes(Paths.get("src", "test", "resources", "send_code.json")), StandardCharsets.UTF_8);
                    }
                }
        ).when(transmitterMock).send(anyString(), anyString(), any());

        Mocean mocean = TestingUtils.getMoceanObj(transmitterMock);
        mocean.verifyRequest()
                .setTo("testing to")
                .setBrand("testing brand")
                .send();
    }

    @Test
    public void testSendAsCPA() {
        VerifyRequest verifyRequest = this.mocean.verifyRequest();
        assertEquals(ChargeType.CHARGE_PER_CONVERSION, verifyRequest.verifyChargeType);
        verifyRequest.sendAs(ChargeType.CHARGE_PER_ATTEMPT);
        assertEquals(ChargeType.CHARGE_PER_ATTEMPT, verifyRequest.verifyChargeType);
    }

    @Test
    public void testJsonResponseObject() {
        try {
            String jsonResponse = new String(Files.readAllBytes(Paths.get("src", "test", "resources", "send_code.json")), StandardCharsets.UTF_8);

            VerifyRequest verifyRequestMock = mock(VerifyRequest.class);
            when(verifyRequestMock.send())
                    .thenReturn(
                            ResponseFactory
                                    .createObjectFromRawResponse(jsonResponse, VerifyRequestResponse.class)
                                    .setRawResponse(jsonResponse)
                    );

            VerifyRequestResponse verifyRequestResponse = verifyRequestMock.send();
            assertEquals(verifyRequestResponse.toString(), jsonResponse);
            this.testObject(verifyRequestResponse);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testXmlResponseObject() {
        try {
            String xmlResponse = new String(Files.readAllBytes(Paths.get("src", "test", "resources", "send_code.xml")), StandardCharsets.UTF_8);

            VerifyRequest verifyRequestMock = mock(VerifyRequest.class);
            when(verifyRequestMock.send())
                    .thenReturn(
                            ResponseFactory
                                    .createObjectFromRawResponse(xmlResponse
                                                    .replaceAll("<verify_request>", "")
                                                    .replaceAll("</verify_request>", ""),
                                            VerifyRequestResponse.class
                                    ).setRawResponse(xmlResponse)
                    );

            VerifyRequestResponse verifyRequestResponse = verifyRequestMock.send();
            assertEquals(verifyRequestResponse.toString(), xmlResponse);
            this.testObject(verifyRequestResponse);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testMalformedResponse() throws IOException {
        try {
            ResponseFactory
                    .createObjectFromRawResponse("malform string", VerifyRequestResponse.class)
                    .setRawResponse("malform string");
            fail();
        } catch (MoceanErrorException ignored) {
        }
    }

    private void testObject(VerifyRequestResponse verifyRequestResponse) {
        assertEquals(verifyRequestResponse.getStatus(), "0");
        assertEquals(verifyRequestResponse.getReqId(), "CPASS_restapi_C0000002737000000.0002");
    }
}