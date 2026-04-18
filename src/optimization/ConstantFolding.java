package optimization;

import java.util.*;
import ir.Instruction;

public class ConstantFolding {

    public static boolean optimize(List<Instruction> program) {

        boolean changed = false;

        for (Instruction inst : program) {

            if (inst.op != null &&
                    inst.arg1 != null && inst.arg1.matches("-?\\d+") &&
                    inst.arg2 != null && inst.arg2.matches("-?\\d+")) {

                int val1 = Integer.parseInt(inst.arg1);
                int val2 = Integer.parseInt(inst.arg2);
                int result = 0;

                switch(inst.op) {
                    case "+": result = val1 + val2; break;
                    case "-": result = val1 - val2; break;
                    case "*": result = val1 * val2; break;
                    case "/": if(val2 != 0) result = val1 / val2; else continue; break;
                    default: continue;
                }

                inst.arg1 = String.valueOf(result);
                inst.arg2 = null;
                inst.op = null;

                changed = true;
            }
        }

        return changed;
    }
}