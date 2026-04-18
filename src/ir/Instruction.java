package ir;

import java.util.*;

public class Instruction {
    public String result;
    public String arg1;
    public String arg2;
    public String op;

    public boolean isLabel = false;
    public boolean isGoto = false;
    public boolean isIfGoto = false;
    public boolean isReturn = false;

    public Set<String> use = new HashSet<>();
    public Set<String> def = new HashSet<>();

    public Instruction(String r, String a1, String a2, String o) {
        result = r;
        arg1 = a1;
        arg2 = a2;
        op = o;

        if (r != null && r.endsWith(":")) {
            isLabel = true;
            result = r.substring(0, r.length() - 1);
            return;
        }

        if (o != null) {
            if (o.equals("goto")) {
                isGoto = true;
                return;
            }
            if (o.equals("ifgoto")) {
                isIfGoto = true;
                if (a1 != null && Character.isLetter(a1.charAt(0)) && !a1.matches("\\d+")) use.add(a1);
                return;
            }
        }

        if (o == null && r != null && r.equals("return")) {
            isReturn = true;
            if (a1 != null && !a1.isEmpty() && Character.isLetter(a1.charAt(0)) && !a1.matches("\\d+")) use.add(a1);
            return;
        }

        // Normal assignment instruction
        if (a1 != null && !a1.isEmpty() && Character.isLetter(a1.charAt(0)) && !a1.matches("\\d+")) use.add(a1);
        if (a2 != null && !a2.isEmpty() && Character.isLetter(a2.charAt(0)) && !a2.matches("\\d+")) use.add(a2);
        if (r != null && !r.isEmpty() && Character.isLetter(r.charAt(0))) def.add(r);
    }

    public String toString() {
        if (isLabel) return result + ":";
        if (isGoto) return "goto " + arg1;
        if (isIfGoto) return "if " + arg1 + " goto " + arg2;
        if (isReturn) return "return " + (arg1 != null ? arg1 : "");

        if (arg2 != null) return result + " = " + arg1 + " " + op + " " + arg2;
        return result + " = " + arg1;
    }
}