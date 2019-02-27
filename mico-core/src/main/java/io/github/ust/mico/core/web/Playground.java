package io.github.ust.mico.core.web;

import java.util.ArrayList;
import java.util.List;

public class Playground {
    
    public static void main(String[] args) {
        List<A> as = new ArrayList<A>();
        as.add(new A(1));
        as.add(new A(2));
        as.add(new A(3));
        
        as.forEach(a -> System.out.println(a.x));
        
        for (A a : as) {
            if (a.x == 2) {
                a.x = 10;
            }
        }
        
        as.forEach(a -> System.out.println(a.x));
    }
    
    static class A {
        int x;
        
        A(int x) {
            this.x = x;
        }
    }
    
}