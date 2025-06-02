package horizon.demo;

import horizon.core.conductor.ConductorMethod;
import horizon.core.parameter.ParameterInfo;
import horizon.demo.conductor.UserConductor;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Test parameter resolution functionality.
 */
public class ParameterResolutionTest {
    private static final Logger logger = LoggerFactory.getLogger(ParameterResolutionTest.class);
    
    @Test
    public void testParameterAnalysis() throws Exception {
        UserConductor conductor = new UserConductor();
        Method createMethod = UserConductor.class.getMethod("createUser", String.class, String.class);
        
        ConductorMethod conductorMethod = new ConductorMethod(conductor, createMethod, "user.create");
        
        logger.info("=== Parameter Analysis Test ===");
        logger.info("Method: {}", createMethod.getName());
        logger.info("Parameters count: {}", conductorMethod.getParameters().size());
        
        for (ParameterInfo param : conductorMethod.getParameters()) {
            logger.info("Parameter: {} (type: {}, required: {}, source: {})", 
                       param.getName(), param.getType().getSimpleName(), 
                       param.isRequired(), param.getSource());
        }
    }
    
    @Test
    public void testSimpleParameterResolution() throws Exception {
        UserConductor conductor = new UserConductor();
        Method createMethod = UserConductor.class.getMethod("createUser", String.class, String.class);
        
        ConductorMethod conductorMethod = new ConductorMethod(conductor, createMethod, "user.create");
        
        // Test HTTP-style payload
        Map<String, Object> httpPayload = new HashMap<>();
        httpPayload.put("name", "John Doe");
        httpPayload.put("email", "john@example.com");
        httpPayload.put("_method", "POST");
        
        logger.info("=== HTTP Payload Test ===");
        logger.info("Input payload: {}", httpPayload);
        
        try {
            Object result = conductorMethod.invoke(httpPayload);
            logger.info("Result: {}", result);
        } catch (Exception e) {
            logger.error("Error invoking method", e);
        }
    }
    
    @Test
    public void testComplexParameterResolution() throws Exception {
        UserConductor conductor = new UserConductor();
        Method testMethod = UserConductor.class.getMethod("testComplexParameters", 
                                                          String.class, String.class, String.class, String.class, String.class);
        
        ConductorMethod conductorMethod = new ConductorMethod(conductor, testMethod, "user.test.complex");
        
        // Test complex payload with various sources
        Map<String, Object> complexPayload = new HashMap<>();
        complexPayload.put("path.pathId", "path123");
        complexPayload.put("query.queryParam", "queryValue");
        complexPayload.put("header.headerValue", "headerData");
        complexPayload.put("body.bodyField", "bodyContent");
        complexPayload.put("anyField", "foundAnywhere");
        
        // Add body as nested map
        Map<String, Object> body = new HashMap<>();
        body.put("bodyField", "bodyContent");
        body.put("anyField", "foundInBody");
        complexPayload.put("body", body);
        
        logger.info("=== Complex Payload Test ===");
        logger.info("Input payload: {}", complexPayload);
        
        try {
            Object result = conductorMethod.invoke(complexPayload);
            logger.info("Result: {}", result);
        } catch (Exception e) {
            logger.error("Error invoking complex method", e);
        }
    }
    
    @Test
    public void testParameterDefaults() throws Exception {
        UserConductor conductor = new UserConductor();
        Method testMethod = UserConductor.class.getMethod("testParameterResolution", 
                                                          String.class, Integer.class, Boolean.class, String[].class);
        
        ConductorMethod conductorMethod = new ConductorMethod(conductor, testMethod, "user.test.params");
        
        // Test payload with minimal data (should use defaults)
        Map<String, Object> minimalPayload = new HashMap<>();
        minimalPayload.put("name", "TestUser");
        // age, active, tags should use defaults
        
        logger.info("=== Default Values Test ===");
        logger.info("Input payload: {}", minimalPayload);
        
        try {
            Object result = conductorMethod.invoke(minimalPayload);
            logger.info("Result: {}", result);
        } catch (Exception e) {
            logger.error("Error testing defaults", e);
        }
    }
}
