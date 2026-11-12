package htest;

import hapi.Hours;
import java.util.*;

public class Level1Test {
    static int passed = 0, failed = 0;

    static void check(String name, Object actual, Object expected) {
        if (Objects.equals(actual, expected)) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL " + name + " | expected=" + expected + " actual=" + actual);
        }
    }

    public static void main(String[] args) throws Exception {
        Hours h = (Hours) Class.forName(args[0]).getDeclaredConstructor().newInstance();

        check("list empty", h.listEmployees(), List.of());

        h.addEmployee("e1", "Alice", 20);
        check("get existing", h.getEmployee("e1"), "Alice,0");
        check("total existing", h.getTotalHours("e1"), 0);
        check("get missing", h.getEmployee("nope"), null);
        check("total missing", h.getTotalHours("nope"), null);

        check("clockIn missing", h.clockIn("nope", 10), false);
        check("clockOut missing", h.clockOut("nope", 11), false);

        check("clockIn", h.clockIn("e1", 12), true);
        check("clockIn twice", h.clockIn("e1", 13), false);
        check("clockOut", h.clockOut("e1", 18), true);
        check("total after one shift", h.getTotalHours("e1"), 6);
        check("get after one shift", h.getEmployee("e1"), "Alice,6");
        check("clockOut twice", h.clockOut("e1", 19), false);

        check("clockIn again", h.clockIn("e1", 20), true);
        check("clockOut again", h.clockOut("e1", 24), true);
        check("total after two shifts", h.getTotalHours("e1"), 10);

        h.addEmployee("e2", "Bob", 25);
        check("clockIn e2", h.clockIn("e2", 25), true);
        check("clockIn e1 interleaved", h.clockIn("e1", 26), true);
        check("clockOut e2", h.clockOut("e2", 30), true);
        check("clockOut e1", h.clockOut("e1", 34), true);
        check("total e2", h.getTotalHours("e2"), 5);
        check("total e1", h.getTotalHours("e1"), 18);

        h.addEmployee("e1", "Alicia", 30);
        check("replace resets", h.getEmployee("e1"), "Alicia,0");
        check("total after replace", h.getTotalHours("e1"), 0);

        check("remove existing", h.removeEmployee("e1"), true);
        check("get after remove", h.getEmployee("e1"), null);
        check("remove again", h.removeEmployee("e1"), false);
        check("remove unknown", h.removeEmployee("nope"), false);

        h.addEmployee("e3", "Carol", 15);
        h.addEmployee("e1", "Alice", 20);
        check("list sorted", h.listEmployees(), List.of("e1", "e2", "e3"));

        System.out.println("Level 1: " + passed + " passed, " + failed + " failed");
    }
}
