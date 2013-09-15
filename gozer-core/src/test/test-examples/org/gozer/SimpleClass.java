package org.gozer;

import org.springframework.stereotype.Controller;

@Controller
public class SimpleClass {
    @Override
    public String toString() {
        return "it's simple!";
    }
}