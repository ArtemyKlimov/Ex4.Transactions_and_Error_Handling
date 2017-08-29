import com.eviware.soapui.SoapUI;
import com.eviware.soapui.StandaloneSoapUICore;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStepResult;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.support.PropertiesMap;
import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.soapui.tools.SoapUITestCaseRunner;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import java.util.logging.*;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CardTransferTests {
    private String validClientId = "7";
    private String invalidClientId = "333";
    private String wrongClientId = "333";
    private String validAmount = "700";
    private String invalidAmount = "888";
    private String validCardNo = "1111222233332222";
    private String invalidCardNo = "11112";
    private String validPhoneNum = "79101785555";
    private String invalidPhoneNum = "1234567";
    private String statusSuccess = "Your request is being processed";
    private String alertStatusDone = "DONE";
    private String expectedStatusProcessed = "PROCESSED";
    private String statusError = "Error";
    private String expectedStatusRejected = "REJECTED";
    private String expectedStatusAccepted = "ACCEPTED";
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
    public void verifyValidData() throws Exception {
        project.setPropertyValue("cardNo", validCardNo);
        project.setPropertyValue("clientId", validClientId);
        project.setPropertyValue("expectedClientId", validClientId);
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
                    while (it.hasNext()) {
                        TestStepResult thisResult = (TestStepResult) it.next();

                        String[] messages = thisResult.getMessages();
                        if (messages != null && messages.length > 0) {
                            for(String message : messages) {
                                 System.out.println("SOAUP UI Message: " + message);
                                 assertTrue(runner.getReason() + message, runner.getStatus().equals(TestStepResult.TestStepStatus.FAILED));
                            }
                        }

                        System.out.println( "Result № " + thisResult.getStatus());
                        if (thisResult instanceof WsdlTestRequestStepResult) {
                            WsdlTestRequestStepResult wsdlResult = (WsdlTestRequestStepResult ) thisResult;
                            System.out.println("Response Content: \n" + wsdlResult.getResponseContent());
                        }
                    }
                }


        String guid = project.getTestSuiteList().get(0).getTestCaseList().get(0).getTestSuite().getPropertyValue("GUID");
        System.out.println("THIS IS PROPERTY_VALUE_GUID" + guid);
        try {
            Thread.sleep(7000);
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }
        String stmt = "SELECT * FROM REQUEST_STATUS where GUID = '" + guid + "'";
        ResultSet result = statement.executeQuery(stmt);
        System.out.println("Query has been executed");
        while (result.next()) {
            assertEquals(validAmount, result.getString("AMOUNT"));
            assertEquals(validClientId, result.getString("CLIENT_ID"));
            assertEquals(validPhoneNum, result.getString("PHONE_NUM"));
            assertEquals(expectedStatusProcessed, result.getString("REQUEST_STATUS"));
            //assertEquals(alertStatusDone, result.getString("ALERT_STATUS"));
        }

        System.out.print("Testing finished successfully");
    }

    @Test
    public void verifyInvalidAmount() throws Exception {
        project.setPropertyValue("cardNo", validCardNo);
        project.setPropertyValue("clientId", validClientId);
        project.setPropertyValue("expectedClientId", validClientId);
        project.setPropertyValue("amount", invalidAmount);
        project.setPropertyValue("expectedAmount", invalidAmount);
        project.setPropertyValue("phoneNum", validPhoneNum);
        project.setPropertyValue("expectedStatus", statusSuccess);
        project.setPropertyValue("expectedStatusProcessed", expectedStatusRejected);
        List<TestSuite> testSuites = project.getTestSuiteList();


        System.out.println("Running Test Suite: "+ testSuite.getName());
        List<TestCase> testCases = testSuite.getTestCaseList();
        WsdlTestCase testCase = (WsdlTestCase) testSuite.getTestCaseList().get(0);

        System.out.println("Running Test Case: " + testCase.getName());

        WsdlTestCaseRunner runner = new WsdlTestCaseRunner(testCase, new StringToObjectMap() );
         /*
        WsdlTestStep step1 = testCase.getTestStepByName("POST_REQUEST");
        WsdlTestStep step2 = testCase.getTestStepByName("getGUID");
        WsdlTestStep step3 = testCase.getTestStepByName("GET_REQUEST_STATUS");
        runner.runTestStep(step1);
        runner.runTestStep(step2);
        try {
            Thread.sleep(15000);
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }
        runner.runTestStep(step3);
        System.out.println(runner.getReason());
        */
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
        ResultSet result = statement.executeQuery(stmt_get_request_status);

        while (result.next()) {
            assertEquals(invalidAmount, result.getString("AMOUNT"));
            assertEquals(validClientId, result.getString("CLIENT_ID"));
            assertEquals(validPhoneNum, result.getString("PHONE_NUM"));
            assertEquals(expectedStatusRejected, result.getString("REQUEST_STATUS"));
            //assertEquals(alertStatusDone, result.getString("ALERT_STATUS"));
        }

        result = statement.executeQuery(stmt_get_rejected_request);
        while (result.next()) {
            assertEquals("789", result.getString("ERROR_CODE"));
            assertEquals("Amount must be multiple of 100", result.getString("ERROR_DESCR"));
        }

        System.out.print("Testing finished successfully");
    }

    @Test
    public void verifyInvalidClientId() throws Exception {
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
        List<TestSuite> testSuites = project.getTestSuiteList();


        System.out.println("Running Test Suite: "+ testSuite.getName());
        List<TestCase> testCases = testSuite.getTestCaseList();
        WsdlTestCase testCase = (WsdlTestCase) testSuite.getTestCaseByName("ErrorTestCase");

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
        ResultSet result = statement.executeQuery(stmt_get_request_status);

        while (result.next()) {
            assertEquals(validAmount, result.getString("AMOUNT"));
            assertEquals(invalidClientId, result.getString("CLIENT_ID"));
            assertEquals(validPhoneNum, result.getString("PHONE_NUM"));
            assertEquals(expectedStatusRejected, result.getString("REQUEST_STATUS"));
            //assertEquals(alertStatusDone, result.getString("ALERT_STATUS"));
        }

        result = statement.executeQuery(stmt_get_rejected_request);
        while (result.next()) {
            assertEquals("772", result.getString("ERROR_CODE"));
            assertEquals("Processing has rejected Your request", result.getString("ERROR_DESCR"));
        }

        System.out.print("Testing finished successfully");
    }


}