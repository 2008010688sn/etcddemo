package com.example.jdk11demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.servlet.http.HttpServletRequest;

@SpringBootTest
class Jdk11demoApplicationTests extends ClassLoader {

    @Test
    void contextLoads() {

    }
    public static int getValue(int i) {
        return 0;
    }
    public static void main(String[] args) {

        System.out.println(getValue(2));;

    }
    static String ss(HttpServletRequest request){
        try {
            return "a";
        }finally {
            return "b";
        }
    }
}
