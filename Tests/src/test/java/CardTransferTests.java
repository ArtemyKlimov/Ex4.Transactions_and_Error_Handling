import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStepResult;

import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.soapui.tools.SoapUITestCaseRunner;
import org.apache.xmlbeans.XmlException;
import org.junit.Assert;

import java.util.*;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.*;

import static org.junit.Assert.*;

public class CardTransferTests {
    private String validClientId = "17";
    private String invalidClientId = "333";
    private String wrongClientId = "abc";
    private String validAmount = "700";
    private String invalidAmount = "888";
    private String wrongCardNoFormat = "12345";
    private String tooLargeAmount = "21000";
    private String validCardNo = "1111222233331212";
    private String invalidCardNo = "11112";
    private String validPhoneNum = "79101785555";
    private String invalidPhoneNum = "1234567";
    private String statusSuccess = "Your request is being processed";
    private String alertStatusDone = "DONE";
    private String expectedStatusProcessed = "PROCESSED";
    private String statusError = "Error";
    private String expectedStatusRejected = "REJECTED";
    private SoapUITestCaseRunner runner;
    private WsdlProject project = null;
    private TestSuite testSuite = null;
    private Connection connection = null;
    Statement statement = null;


    public CardTransferTests() throws SQLException, XmlException, IOException, SoapUIException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Impossible to load JDBC Driver");
            e.printStackTrace();
            return;
        }
        System.out.println("Oracle JDBC Driver Registered!");
        try {
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:DBOne", "artemy", "artemy");
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;
        }
        statement = connection.createStatement();
        project =  new WsdlProject("C:\\LocalRepo\\EX4\\SOAP-UI\\CARD-TRANSFER-soapui-project.xml");
        testSuite = project.getTestSuiteByName("Tests");
    }

    @Test
    public void verifyValidDataCase() throws Exception {
        Random r = new Random();
        long cardNo = 1000000000000000L + r.nextInt(100000000);
        int client;
        while(true) {
            client = r.nextInt(100000);
            if (client % 3 != 0)
                break;
        }
        String validClient = String.valueOf(client);
        project.setPropertyValue("cardNo", String.valueOf(cardNo));
        project.setPropertyValue("clientId", validClient);
        project.setPropertyValue("expectedClientId", validClient);
        project.setPropertyValue("amount", validAmount);
        project.setPropertyValue("expectedAmount", validAmount);
        project.setPropertyValue("phoneNum", validPhoneNum);
        project.setPropertyValue("expectedStatus", statusError);
        project.setPropertyValue("expectedStatusProcessed", expectedStatusProcessed);

            System.out.println("Running Test Suite: "+ testSuite.getName());
            List<TestCase> testCases = testSuite.getTestCaseList();
            TestCase testCase = testCases.get(0);

                System.out.println("Running Test Case: " + testCase.getName());
                WsdlTestCaseRunner runner = new WsdlTestCaseRunner((WsdlTestCase) testCase, new StringToObjectMap(testCase.getProperties()) );

                runner.run();

                System.out.println(runner.getReason());
                List results = runner.getResults();

                if (results != null && results.size() > 0) {
                    Iterator it = results.iterator();
                    int testCaseNo = 1;
                    while (it.hasNext()) {
                        TestStepResult thisResult = (TestStepResult) it.next();

                        String[] messages = thisResult.getMessages();
                        if (messages != null && messages.length > 0) {
                            for(String message : messages) {
                                 System.out.println("SOAUP UI Message: " + message);
                                 assertTrue(runner.getReason() + message, runner.getStatus().equals(TestStepResult.TestStepStatus.FAILED));
                            }
                        }

                        System.out.println( "TestStep № " + testCaseNo + thisResult.getStatus());
                        if (thisResult instanceof WsdlTestRequestStepResult) {
                            WsdlTestRequestStepResult wsdlResult = (WsdlTestRequestStepResult ) thisResult;
                            System.out.println("Response Content: \n" + wsdlResult.getResponseContent());
                        }
                        testCaseNo++;
                    }
                }


        String guid = project.getTestSuiteList().get(0).getTestCaseList().get(0).getTestSuite().getPropertyValue("GUID");
        System.out.println("THIS IS PROPERTY_VALUE_GUID" + guid);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }
        String stmt = "SELECT * FROM REQUEST_STATUS where GUID = '" + guid + "'";
        String smsListStmt = "SELECT * FROM SMSLIST where GUID = '" + guid + "'";
        ResultSet result = statement.executeQuery(stmt);
        System.out.println("Query has been executed");
        while (result.next()) {
            assertEquals(validAmount, result.getString("AMOUNT"));
            assertEquals(validClient, result.getString("CLIENT_ID"));
            assertEquals(validPhoneNum, result.getString("PHONE_NUM"));
            assertEquals(expectedStatusProcessed, result.getString("REQUEST_STATUS"));
            assertEquals(alertStatusDone, result.getString("ALERT_STATUS"));
        }

        result = statement.executeQuery(smsListStmt);
        while (result.next()) {
            assertEquals("Your request was successfully completed", result.getString("TEXT"));
            assertEquals("DONE", result.getString("STATUS"));
        }

        System.out.print("Testing finished successfully");
    }

    @Test
    public void verifyAmountNotMultipleOf100Case() throws Exception {
        Random r = new Random();
        long cardNo = 1000000000000000L + r.nextInt(100000000);
        int client;
        while(true) {
            client = r.nextInt(100000);
            if (client % 3 != 0)
                break;
        }
        String validClient  =String.valueOf(client);
        project.setPropertyValue("cardNo", String.valueOf(cardNo));
        project.setPropertyValue("clientId", validClient);
        project.setPropertyValue("expectedClientId", validClient);
        project.setPropertyValue("amount", invalidAmount);
        project.setPropertyValue("expectedAmount", invalidAmount);
        project.setPropertyValue("phoneNum", validPhoneNum);
        project.setPropertyValue("expectedStatus", statusSuccess);
        project.setPropertyValue("expectedStatusProcessed", expectedStatusRejected);

        System.out.println("Running Test Suite: "+ testSuite.getName());
        List<TestCase> testCases = testSuite.getTestCaseList();
        WsdlTestCase testCase = (WsdlTestCase) testSuite.getTestCaseList().get(0);

        System.out.println("Running Test Case: " + testCase.getName());

        WsdlTestCaseRunner runner = new WsdlTestCaseRunner(testCase, new StringToObjectMap() );
        runner.run();
        List results = runner.getResults();

        if (results != null && results.size() > 0) {
            Iterator it = results.iterator();
            int testStepNo = 1;
            while (it.hasNext()) {
                TestStepResult thisResult = (TestStepResult) it.next();
                System.out.println( "TestStep № " + testStepNo + " : " + thisResult.getStatus());
                String[] messages = thisResult.getMessages();
                if (thisResult instanceof WsdlTestRequestStepResult) {
                    WsdlTestRequestStepResult wsdlResult = (WsdlTestRequestStepResult ) thisResult;
                    System.out.println("Response Content: \n" + wsdlResult.getResponseContent());
                }
                if (messages != null && messages.length > 0) {
                    for(String message : messages) {
                        System.out.println("SOAUP UI Message: " + message);
                        assertTrue(runner.getReason() + message, runner.getStatus().equals(TestStepResult.TestStepStatus.FAILED));
                    }
                }
                testStepNo++;
            }
        }

        String guid = project.getTestSuiteList().get(0).getTestCaseList().get(0).getTestSuite().getPropertyValue("GUID");
        System.out.println("THIS IS PROPERTY_VALUE_GUID" + guid);

        String stmt_get_request_status = "SELECT * FROM REQUEST_STATUS where GUID = '" + guid + "'";
        String stmt_get_rejected_request = "SELECT * FROM REQUEST_REJECTED WHERE GUID = '" + guid + "'";
        String smsListStmt = "SELECT * FROM SMSLIST where GUID = '" + guid + "'";
        ResultSet result = statement.executeQuery(stmt_get_request_status);

        while (result.next()) {
            assertEquals(invalidAmount, result.getString("AMOUNT"));
            assertEquals(validClient, result.getString("CLIENT_ID"));
            assertEquals(validPhoneNum, result.getString("PHONE_NUM"));
            assertEquals(expectedStatusRejected, result.getString("REQUEST_STATUS"));
           // assertEquals(alertStatusDone, result.getString("ALERT_STATUS"));
        }

        result = statement.executeQuery(stmt_get_rejected_request);
        while (result.next()) {
            assertEquals("789", result.getString("ERROR_CODE"));
            assertEquals("Amount must be multiple of 100", result.getString("ERROR_DESCR"));
        }

        result = statement.executeQuery(smsListStmt);

        while (result.next()) {
            assertTrue(result.getString("TEXT").contains("Amount must be multiple of 100"));
            assertEquals("DONE", result.getString("STATUS"));
        }

        System.out.print("Testing finished successfully");
    }

    @Test
    public void verifyInvalidClientIdCase() throws Exception {
        project.setPropertyValue("cardNo", validCardNo);
        project.setPropertyValue("clientId", invalidClientId);
        project.setPropertyValue("expectedClientId", "");
        project.setPropertyValue("amount", validAmount);
        project.setPropertyValue("expectedAmount", validAmount);
        project.setPropertyValue("phoneNum", validPhoneNum);
        project.setPropertyValue("statusError", statusError);
        project.setPropertyValue("errorDescr", "Processing has rejected Your request");
        project.setPropertyValue("erorCode", "772");
        project.setPropertyValue("expectedStatusRejected", expectedStatusRejected);

        System.out.println("Running Test Suite: " + testSuite.getName());
        List<TestCase> testCases = testSuite.getTestCaseList();
        WsdlTestCase testCase = (WsdlTestCase) testSuite.getTestCaseByName("ErrorTestCase");

        System.out.println("Running Test Case: " + testCase.getName());

        WsdlTestCaseRunner runner = new WsdlTestCaseRunner(testCase, new StringToObjectMap());

        runner.run();
        List results = runner.getResults();

        if (results != null && results.size() > 0) {
            Iterator it = results.iterator();
            int testStepNo = 1;
            while (it.hasNext()) {
                TestStepResult thisResult = (TestStepResult) it.next();
                System.out.println("TestStep № " + testStepNo + " : " + thisResult.getStatus());
                String[] messages = thisResult.getMessages();
                if (thisResult instanceof WsdlTestRequestStepResult) {
                    WsdlTestRequestStepResult wsdlResult = (WsdlTestRequestStepResult) thisResult;
                    System.out.println("Response Content: \n" + wsdlResult.getResponseContent());
                }
                if (messages != null && messages.length > 0) {
                    for (String message : messages) {
                        System.out.println("SOAUP UI Message: " + message);
                        assertTrue(runner.getReason() + message, runner.getStatus().equals(TestStepResult.TestStepStatus.FAILED));
                    }
                }
                testStepNo++;
            }
        }

        String guid = project.getTestSuiteList().get(0).getTestCaseList().get(0).getTestSuite().getPropertyValue("GUID");
        System.out.println("THIS IS PROPERTY_VALUE_GUID: " + guid);

        String stmt_get_request_status = "SELECT * FROM REQUEST_STATUS where GUID = '" + guid + "'";
        String stmt_get_rejected_request = "SELECT * FROM REQUEST_REJECTED WHERE GUID = '" + guid + "'";
        ResultSet result = statement.executeQuery(stmt_get_request_status);

        while (result.next()) {
            assertEquals(validAmount, result.getString("AMOUNT"));
            assertEquals(invalidClientId, result.getString("CLIENT_ID"));
            assertEquals(validPhoneNum, result.getString("PHONE_NUM"));
            assertEquals(expectedStatusRejected, result.getString("REQUEST_STATUS"));
        }

        result = statement.executeQuery(stmt_get_rejected_request);
        while (result.next()) {
            assertEquals("772", result.getString("ERROR_CODE"));
            assertEquals("Processing has rejected Your request", result.getString("ERROR_DESCR"));
        }

        System.out.print("Testing finished successfully");
    }

    @Test
    public void verifyTooLargeAmountCase() throws Exception {
        project.setPropertyValue("cardNo", validCardNo);
        project.setPropertyValue("clientId", validClientId);
        project.setPropertyValue("amount", tooLargeAmount);
        project.setPropertyValue("phoneNum", validPhoneNum);
        project.setPropertyValue("statusError", statusError);
        project.setPropertyValue("errorDescr", "amount can not be more than 20000");
        project.setPropertyValue("erorCode", "776");
        project.setPropertyValue("expectedStatusRejected", expectedStatusRejected);

        System.out.println("Running Test Suite: " + testSuite.getName());

        WsdlTestCase testCase = (WsdlTestCase) testSuite.getTestCaseByName("ErrorTestCase");

        System.out.println("Running Test Case: " + testCase.getName());

        WsdlTestCaseRunner runner = new WsdlTestCaseRunner(testCase, new StringToObjectMap());

        runner.run();
        List results = runner.getResults();

        if (results != null && results.size() > 0) {
            Iterator it = results.iterator();
            int testStepNo = 1;
            while (it.hasNext()) {
                TestStepResult thisResult = (TestStepResult) it.next();
                System.out.println("TestStep № " + testStepNo + " : " + thisResult.getStatus());
                String[] messages = thisResult.getMessages();
                if (thisResult instanceof WsdlTestRequestStepResult) {
                    WsdlTestRequestStepResult wsdlResult = (WsdlTestRequestStepResult) thisResult;
                    System.out.println("Response Content: \n" + wsdlResult.getResponseContent());

                }
                if (messages != null && messages.length > 0) {
                    for (String message : messages) {
                        System.out.println("SOAUP UI Message: " + message);
                        assertTrue(runner.getReason() + message, runner.getStatus().equals(TestStepResult.TestStepStatus.FAILED));
                    }
                }
                testStepNo++;
            }
        }

        String guid = project.getTestSuiteList().get(0).getTestCaseList().get(0).getTestSuite().getPropertyValue("GUID");
        System.out.println("THIS IS PROPERTY_VALUE_GUID: " + guid);

        String stmt_get_request_status = "SELECT * FROM REQUEST_STATUS where GUID = '" + guid + "'";
        String stmt_get_rejected_request = "SELECT * FROM REQUEST_REJECTED WHERE GUID = '" + guid + "'";
        ResultSet result = statement.executeQuery(stmt_get_request_status);

        while (result.next()) {
            assertEquals(tooLargeAmount, result.getString("AMOUNT"));
            assertEquals(validClientId, result.getString("CLIENT_ID"));
            assertEquals(validPhoneNum, result.getString("PHONE_NUM"));
            assertEquals(expectedStatusRejected, result.getString("REQUEST_STATUS"));
            //assertEquals(alertStatusDone, result.getString("ALERT_STATUS"));
        }

        result = statement.executeQuery(stmt_get_rejected_request);
        while (result.next()) {
            assertEquals("776", result.getString("ERROR_CODE"));
            assertEquals("amount can not be more than 20000", result.getString("ERROR_DESCR"));
        }
        System.out.print("Testing finished successfully");
    }

    @Test
    public void verifyInvalidInputMessageFormat() throws Exception {
        project.setPropertyValue("cardNo", wrongCardNoFormat);
        project.setPropertyValue("clientId", validClientId);
        project.setPropertyValue("amount", validAmount);
        project.setPropertyValue("phoneNum", validPhoneNum);
        project.setPropertyValue("statusError", statusError);
        project.setPropertyValue("errorDescr", "A schema validation error has occurred while validating the message tree");
        project.setPropertyValue("erorCode", "5026");
        project.setPropertyValue("expectedStatusRejected", expectedStatusRejected);

        System.out.println("Running Test Suite: " + testSuite.getName());

        WsdlTestCase testCase = (WsdlTestCase) testSuite.getTestCaseByName("ErrorTestCase");

        System.out.println("Running Test Case: " + testCase.getName());

        WsdlTestCaseRunner runner = new WsdlTestCaseRunner(testCase, new StringToObjectMap());
        runner.runTestStepByName("POST_REQUEST");


        List results = runner.getResults();

        if (results != null && results.size() > 0) {
            Iterator it = results.iterator();
            int testStepNo = 1;
            while (it.hasNext()) {
                TestStepResult thisResult = (TestStepResult) it.next();
                System.out.println("TestStep № " + testStepNo + " : " + thisResult.getStatus());
                String[] messages = thisResult.getMessages();
                if (thisResult instanceof WsdlTestRequestStepResult) {
                    WsdlTestRequestStepResult wsdlResult = (WsdlTestRequestStepResult) thisResult;
                    System.out.println("Response Content: \n" + wsdlResult.getResponseContent());
                }
                if (messages != null && messages.length > 0) {
                    for (String message : messages) {
                        System.out.println("SOAUP UI Message: " + message);
                        assertTrue(runner.getReason() + message, runner.getStatus().equals(TestStepResult.TestStepStatus.FAILED));
                    }
                }
                testStepNo++;
            }
        }
        System.out.print("verifyInvalidInputMessageFormat Test has successfully finished");
    }

    @Test
    public void verifyTooManyRequestsFromClientCase() throws Exception {

        Random r = new Random();
        long cardOne = 1000000000000000L + r.nextInt(100000);
        long cardTwo = 1000000000000000L + r.nextInt(100000);
        long cardThree = 1000000000000000L + r.nextInt(100000);
        int clientId;
        while(true) {
            clientId = r.nextInt(100000);
            if(clientId % 3 != 0)
                break;
        }
        System.out.println("CLIENT_ID is: " + clientId);
        project.setPropertyValue("cardOne", String.valueOf(cardOne));
        project.setPropertyValue("cardTwo", String.valueOf(cardTwo));
        project.setPropertyValue("cardThree", String.valueOf(cardThree));
        project.setPropertyValue("clientId", String.valueOf(clientId));
        project.setPropertyValue("phoneNum", validPhoneNum);
        project.setPropertyValue("statusError", statusError);
        project.setPropertyValue("errorDescr", "You have exceeded maximum transaction amount through one day");
        project.setPropertyValue("erorCode", "778");
        project.setPropertyValue("expectedStatusRejected", expectedStatusRejected);

        System.out.println("Running Test Suite: " + testSuite.getName());

        WsdlTestCase testCase = (WsdlTestCase) testSuite.getTestCaseByName("TooManyRequestsFromClient");

        System.out.println("Running Test Case: " + testCase.getName());

        WsdlTestCaseRunner runner = new WsdlTestCaseRunner(testCase, new StringToObjectMap());

        runner.run();
        List results = runner.getResults();

        if (results != null && results.size() > 0) {
            Iterator it = results.iterator();
            int testStepNo = 1;
            while (it.hasNext()) {
                TestStepResult thisResult = (TestStepResult) it.next();
                System.out.println("TestStep № " + testStepNo + " : " + thisResult.getStatus());
                String[] messages = thisResult.getMessages();
                if (thisResult instanceof WsdlTestRequestStepResult) {
                    WsdlTestRequestStepResult wsdlResult = (WsdlTestRequestStepResult) thisResult;
                    System.out.println("Response Content: \n" + wsdlResult.getResponseContent());
                }
                if (messages != null && messages.length > 0) {
                    for (String message : messages) {
                        System.out.println("SOAUP UI Message: " + message);
                        assertTrue(runner.getReason() + message, runner.getStatus().equals(TestStepResult.TestStepStatus.FAILED));
                    }
                }
                testStepNo++;
            }
        }

        String guid = project.getTestSuiteList().get(0).getTestCaseList().get(0).getTestSuite().getPropertyValue("GUID");
        System.out.println("THIS IS PROPERTY_VALUE_GUID: " + guid);

        String stmt_get_request_status = "SELECT * FROM REQUEST_STATUS where GUID = '" + guid + "'";
        String stmt_get_rejected_request = "SELECT * FROM REQUEST_REJECTED WHERE GUID = '" + guid + "'";
        ResultSet result = statement.executeQuery(stmt_get_request_status);

        while (result.next()) {
            assertEquals(String.valueOf(clientId), result.getString("CLIENT_ID"));
            assertEquals(validPhoneNum, result.getString("PHONE_NUM"));
            assertEquals(expectedStatusRejected, result.getString("REQUEST_STATUS"));
            //assertEquals(alertStatusDone, result.getString("ALERT_STATUS"));
        }

        result = statement.executeQuery(stmt_get_rejected_request);
        while (result.next()) {
            assertEquals("778", result.getString("ERROR_CODE"));
            assertTrue(result.getString("ERROR_DESCR").contains("You have exceeded maximum transaction amount through one day"));
        }
        System.out.print("Testing finished successfully");
    }

    @Test
    public void verifyTooManyRequestsWithOneCard() throws Exception {
        Random r = new Random();
        long cardNo = 1000000000000000L + r.nextInt(10000);
        int clientId = 3;
        while(true) {
            clientId = r.nextInt(100000);
            if(clientId % 3 != 0)
                break;
        }
        project.setPropertyValue("cardNo", String.valueOf(cardNo));
        project.setPropertyValue("clientId", String.valueOf(clientId));
        project.setPropertyValue("phoneNum", validPhoneNum);
        project.setPropertyValue("amount", "20000");
        project.setPropertyValue("statusError", statusError);
        project.setPropertyValue("errorDescr", "Exceeded limit of one-card transactions through one day");
        project.setPropertyValue("erorCode", "779");
        project.setPropertyValue("expectedStatusRejected", expectedStatusRejected);

        System.out.println("Running Test Suite: " + testSuite.getName());

        WsdlTestCase testCase = (WsdlTestCase) testSuite.getTestCaseByName("ErrorTestCase");

        System.out.println("Running Test Case: " + testCase.getName());

        WsdlTestCaseRunner runner = new WsdlTestCaseRunner(testCase, new StringToObjectMap());
        runner.runTestStepByName("POST_REQUEST");
        runner.runTestStepByName("POST_REQUEST");
        runner.run();
        List results = runner.getResults();

        if (results != null && results.size() > 0) {
            Iterator it = results.iterator();
            int testStepNo = 1;
            while (it.hasNext()) {
                TestStepResult thisResult = (TestStepResult) it.next();
                System.out.println("TestStep № " + testStepNo + " : " + thisResult.getStatus());
                String[] messages = thisResult.getMessages();
                if (thisResult instanceof WsdlTestRequestStepResult) {
                    WsdlTestRequestStepResult wsdlResult = (WsdlTestRequestStepResult) thisResult;
                    System.out.println("Response Content: \n" + wsdlResult.getResponseContent());
                }
                if (messages != null && messages.length > 0) {
                    for (String message : messages) {
                        System.out.println("SOAUP UI Message: " + message);
                        assertTrue(runner.getReason() + message, runner.getStatus().equals(TestStepResult.TestStepStatus.FAILED));
                    }
                }
                testStepNo++;
            }
        }

        String guid = project.getTestSuiteList().get(0).getTestCaseList().get(0).getTestSuite().getPropertyValue("GUID");
        System.out.println("THIS IS PROPERTY_VALUE_GUID: " + guid);

        String stmt_get_request_status = "SELECT * FROM REQUEST_STATUS where GUID = '" + guid + "'";
        String stmt_get_rejected_request = "SELECT * FROM REQUEST_REJECTED WHERE GUID = '" + guid + "'";
        ResultSet result = statement.executeQuery(stmt_get_request_status);

        while (result.next()) {
            assertEquals(String.valueOf(clientId), result.getString("CLIENT_ID"));
            assertEquals(validPhoneNum, result.getString("PHONE_NUM"));
            assertEquals(String.valueOf(cardNo), result.getString("CARD_NO"));
            assertEquals(expectedStatusRejected, result.getString("REQUEST_STATUS"));
            //assertEquals(alertStatusDone, result.getString("ALERT_STATUS"));
        }

        result = statement.executeQuery(stmt_get_rejected_request);
        while (result.next()) {
            assertEquals("779", result.getString("ERROR_CODE"));
            assertTrue(result.getString("ERROR_DESCR").contains("Exceeded limit of one-card transactions through one day"));
        }
        System.out.print("verifyTooManyRequestsWithOneCard finished successfully");
    }
}
