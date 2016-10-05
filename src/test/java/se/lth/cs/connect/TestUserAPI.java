package se.lth.cs.connect;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import com.jayway.restassured.response.Response;

import ro.pippo.test.PippoRule;
import ro.pippo.test.PippoTest;

public class TestUserAPI extends PippoTest {

	// This rule ensures that we have a server running before doing the tests
	public Connect app = new Connect();
	@Rule
	public PippoRule pippoRule = new PippoRule(app);
	
	@Test
	public void testAccessDenied() {
		// These routes require an active session => requests should be denied
		// Status code 401 => all is ok (expected)
		// Any other code means that we have an error somewhere.
		when().get("/v1/account/login").then().statusCode(401);
		when().get("/v1/account/collections").then().statusCode(401);
		when().get("/v1/account/self").then().statusCode(401);
		when().post("/v1/account/logout").then().statusCode(401);
		when().post("/v1/account/delete").then().statusCode(401);
		when().post("/v1/account/change-password").then().statusCode(401);
		when().get("/v1/account/invites").then().statusCode(401);
		when().get("/v1/account/dat12asm@student.lu.se").then().statusCode(401);
	}
	
	/**
	 * Verify that 1) an email is sent after signup, and 2) that the email
	 * is sent to the correct email address.
	 */
	@Test
	public void testRegistration() {
		// Use mailbox to capture emails instead of sending them
		Mailbox mailbox = new Mailbox();
		app.useMailClient(mailbox);
		
		// Register
		Response reg = given().
				param("email", "test-reg@serptest.test").
				param("passw", "hejsanhoppsan").
			post("/v1/account/register");
		
		reg.then().
			statusCode(200);
	
		assertEquals("Must send registration email", mailbox.getInbox().size(), 1);
		assertEquals("Recipient must match registered email",
				mailbox.top().recipient, "test-reg@serptest.test");
		
		// Cleanup
		given().
			cookie("JSESSIONID", reg.getCookie("JSESSIONID")).
		when().
			post("/v1/account/delete").
		then().
			statusCode(200);
	}
	
	@Test
	public void testLifecycle() {
		// This test case is not very important, but it showcases how to 
		// (re)use a session for multiple queries and how to create post
		// requests with data.
		Response reg = given().
				param("email", "test@serptest.test").
				param("passw", "hejsanhoppsan").
			post("/v1/account/register");
		
		reg.then().
			statusCode(200);
			
		given().
			cookie("JSESSIONID", reg.getCookie("JSESSIONID")).
		when().
			post("/v1/account/logout").
		then().
			statusCode(200);
		
		Response login = given().
				param("email", "test@serptest.test").
				param("passw", "hejsanhoppsan").
			post("/v1/account/login");
		
		reg.then().
			statusCode(200);
		
		given().
			cookie("JSESSIONID", login.getCookie("JSESSIONID")).
		when().
			post("/v1/account/delete").
		then().
			statusCode(200);
	}
}