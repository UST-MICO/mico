/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.ust.mico.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.ust.mico.core.service.MicoCoreBackgroundTaskFactory;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicoCoreBackgroundTaskFactoryTests {

    @Autowired
    private MicoCoreBackgroundTaskFactory factory;
    
    private CountDownLatch latch;
    private AtomicReference<AssertionError> failure = new AtomicReference<>();
    private AtomicInteger atomicInt = new AtomicInteger(0);
    

    @Test
    public void testRunAsync() throws InterruptedException {
        latch = new CountDownLatch(1);
        factory.runAsync(() -> veryLongLastingTask("MICO"), result -> {
            try {
                assertEquals("Hello MICO!", result);
            } catch (ComparisonFailure cf) {
                failure.set(cf);
            }
            latch.countDown();
        }, e -> exceptionHandler(e));
        latch.await();
        if (failure.get() != null) {
            fail();
        }
    }

    @Test(expected = ComparisonFailure.class)
    public void testRunAsyncFailure() throws InterruptedException {
        latch = new CountDownLatch(1);
        factory.runAsync(() -> veryLongLastingTask("MICO"), result -> {
            try {
                assertEquals("Bye MICO!", result);
            } catch (ComparisonFailure cf) {
                failure.set(cf);
            }
            latch.countDown();
        }, e -> exceptionHandler(e));
        latch.await();
        if (failure.get() != null) {
            throw failure.get();
        }
    }

    @Test
    public void testRunAsyncException() throws InterruptedException {
        latch = new CountDownLatch(1);
        factory.runAsync(() -> veryLongLastingTaskException(), result -> System.out.println(result), e -> {
            exceptionHandler(e);
            latch.countDown();
            return null;
        });
        latch.await();
        assertEquals(1, atomicInt.get());
    }
    
    private Void exceptionHandler(Throwable e) {
        atomicInt.incrementAndGet();
        return null;
    }
    
    private String veryLongLastingTask(String name) {
            pi_digits(100000);
            return "Hello " + name + "!";
    }
    
    private String veryLongLastingTaskException() {
        List<String> list = new ArrayList<>();
        list.get(0);
        return "This line is never executed!";
}
    
    private static final int SCALE = 10000;
    private static final int ARRINIT = 2000;
    
    // see http://www.codecodex.com/wiki/index.php?title=Digits_of_pi_calculation#Java
    private static String pi_digits(int digits){
        StringBuffer pi = new StringBuffer();
        int[] arr = new int[digits + 1];
        int carry = 0;

        for (int i = 0; i <= digits; ++i)
            arr[i] = ARRINIT;

        for (int i = digits; i > 0; i-= 14) {
            int sum = 0;
            for (int j = i; j > 0; --j) {
                sum = sum * j + SCALE * arr[j];
                arr[j] = sum % (j * 2 - 1);
                sum /= j * 2 - 1;
            }

            pi.append(String.format("%04d", carry + sum / SCALE));
            carry = sum % SCALE;
        }
        return pi.toString();
    }
    
}
