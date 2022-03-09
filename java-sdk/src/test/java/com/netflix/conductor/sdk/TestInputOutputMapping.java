package com.netflix.conductor.sdk;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.reflect.TypeToken;
import com.netflix.conductor.common.config.ObjectMapperProvider;
import com.netflix.conductor.sdk.task.OutputParam;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import groovyjarjarasm.asm.util.TraceClassVisitor;
import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
import net.sf.cglib.proxy.MethodInterceptor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.context.annotation.Conditional;

import java.awt.*;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.asm.Opcodes.ASM7;

public class TestInputOutputMapping {


    @WorkflowTask("get_credit_scores")
    public CreditProcessingResult getCreditScores(CustomerInfo customerInfo) {
        CustomerInfo ci = null;
        return new CreditProcessingResult(customerInfo);
    }

    @WorkflowTask("get_loan_amount")
    public @OutputParam("loanAmount") double getLoanAmount(int ficoScore) {
        return 1.0;
    }

    public void main2(String[] args) throws JsonProcessingException {


        SimpleTask<CreditProcessingResult> getCreditScores =
                new SimpleTask("fooBarTask", "fooBarTask");

        getCreditScores.input( (input) -> {
            return new CustomerInfo();
        });
        CustomerInfo workflowInput = newInstance("workflow", CustomerInfo.class);

        CustomerInfo customerInfo = newInstance("task1", CustomerInfo.class);
        customerInfo.setName(workflowInput.getName());
        customerInfo.setBirthYear(workflowInput.getBirthYear());
        customerInfo.setPrefix(workflowInput.getPrefix());

        //customerInfo.setSsn("111-11-1111");
        //System.out.println("here: " + customerInfo.getBirthYear());
        //getCreditScores.input(customerInfo);

        /*
        getCreditScores.input(
                "name", ConductorWorkflow.input.get("name"),
                "birthYear", ConductorWorkflow.input.get("birthYear"),
                "ssn", ConductorWorkflow.input.get("ssn")
        );

         */

        CreditProcessingResult result = null;
        MyWorkflowInput input = null;

        SimpleTask getLoanAmount =
                new SimpleTask("get_loan_amount", "get_loan_amount");




        ConductorWorkflow workflowDef = new WorkflowBuilder(null)
                .name("chain_task_input_outputs")
                .add(getCreditScores)
                //.add("refName", task(getLoanAmount(anyInt())))
                .add(getLoanAmount)
                .build();

        System.out.println(inputMap);
        System.out.println(outputMap);
    }

    private <T>T task(Object methodCall) {
        return null;
    }

    private <T>T setInput() {
        return null;
    }

    private static Map<String, Map<String, Object>> inputMap = new HashMap<>();
    private static Map<String, Map<String, Object>> outputMap = new HashMap<>();

    private static Object dumdumdum() {
        Enhancer enhancer = new Enhancer();
        //enhancer.setSuperclass(t);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            return null;
        });
        return enhancer.create();
    }
    private static <T>T newInstance(String refName, Class<T> t) {
        if(inputMap.get(refName) == null) inputMap.put(refName, new HashMap<>());
        if(outputMap.get(refName) == null) outputMap.put(refName, new HashMap<>());

        final Map<String, Object> map = inputMap.get(refName);
        final Map<String, Object> opMap = outputMap.get(refName);

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(t);
        enhancer.setInterceptDuringConstruction(true);


        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            if(isSetter(method)) {
                System.out.println("\t\t-->Setter method " + method.getName() + " with args " + args[0]);
                //proxy.invokeSuper(obj, args);
                map.put(method.getName(), args[0]);
                return null;
            } else if(isGetter(method)) {
                //System.out.println("NOT a  Setter method " + method.getName());
                String value =  refName + ".output." + method.getName();
                opMap.put(method.getName(), value);
                //System.out.println("value: " + value);
                //return value;
                return proxy.invokeSuper(obj, args);
                //return null;
            }
            return null;
        });



        /*
        Method[] methods = t.getMethods();

        for(Method method : methods){
            if(isGetter(method)) System.out.println("getter: " + method);
            if(isSetter(method)) System.out.println("setter: " + method);
        }

         */
        return (T) enhancer.create();
    }



    private static <T>T newInstance2(String refName, Class<T> t) {

        if(inputMap.get(refName) == null) inputMap.put(refName, new HashMap<>());
        if(outputMap.get(refName) == null) outputMap.put(refName, new HashMap<>());

        final Map<String, Object> map = inputMap.get(refName);
        final Map<String, Object> opMap = outputMap.get(refName);

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(t);

        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            if(isSetter(method)) {
                System.out.println("\t\t-->Setter method " + method.getName() + " with args " + args[0]);
                //proxy.invokeSuper(obj, args);
                map.put(method.getName(), args[0]);
                return null;
            } else if(isGetter(method)) {
                //System.out.println("NOT a  Setter method " + method.getName());
                String value =  refName + ".output." + method.getName();
                opMap.put(method.getName(), value);
                //System.out.println("value: " + value);
                //return value;
                return proxy.invokeSuper(obj, args);
                //return null;
            }
            return null;
        });



        /*
        Method[] methods = t.getMethods();

        for(Method method : methods){
            if(isGetter(method)) System.out.println("getter: " + method);
            if(isSetter(method)) System.out.println("setter: " + method);
        }

         */
        return (T) enhancer.create();
    }

    public static abstract class GenericClass<T> {
        private final TypeToken<T> typeToken = new TypeToken<T>(getClass()) { };
        private final Type type = typeToken.getType(); // or getRawType() to return Class<? super T>

        public Type getType() {
            return type;
        }
    }

    public static boolean isGetter(Method method){
        if(!method.getName().startsWith("get"))      return false;
        if(method.getParameterTypes().length != 0)   return false;
        if(void.class.equals(method.getReturnType())) return false;
        return true;
    }

    public static boolean isSetter(Method method){
        if(!method.getName().startsWith("set")) return false;
        if(method.getParameterTypes().length != 1) return false;
        return true;
    }

    public static void main(String[] args) throws JsonProcessingException {
        //new TestInputOutputMapping().main2(null);
        SimpleTask<CreditProcessingResult> example = new SimpleTask<>("aa","aa");
        System.out.println("example: " + example.getTypeX()); // => class java.lang.String

        System.out.println("\n\n\n");
        SimpleTask<CreditProcessingResult> example2 = new SimpleTask<>("aa","aa"){};
        System.out.println("example2: " + example2.getTypeX()); // => class java.lang.String

        System.out.println("\n\n\n");
        SimpleTask<CustomerInfo> simpleTask = SimpleTask.newInstance(null);
        System.out.println("here: " + simpleTask.getTypeX());
    }
}
