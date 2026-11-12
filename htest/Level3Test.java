package htest;

import hapi.Hours;
import java.util.*;

public class Level3Test {
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

        h.addEmployee("e1", "Alice", 20);
        h.addEmployee("e2", "Bob", 10);

        h.clockIn("e1", 10);
        h.clockIn("e2", 12);
        h.clockOut("e2", 15);
        h.clockOut("e1", 18);
        h.clockIn("e1", 20);
        h.clockOut("e1", 30);

        check("hours before any shift", h.hoursAt("e1", 5), 0);
        check("hours at shift start", h.hoursAt("e1", 10), 0);
        check("hours partial shift", h.hoursAt("e1", 14), 4);
        check("hours at shift end", h.hoursAt("e1", 18), 8);
        check("hours partial second shift", h.hoursAt("e1", 25), 13);
        check("hours at second end", h.hoursAt("e1", 30), 18);
        check("hours unknown", h.hoursAt("nope", 20), null);
        check("hours e2 partial", h.hoursAt("e2", 14), 2);
        check("hours e2 after", h.hoursAt("e2", 20), 3);

        check("total at 14", h.totalHoursAt(14), 6);
        check("total at 30", h.totalHoursAt(30), 21);
        check("total at 5", h.totalHoursAt(5), 0);

        check("overtime above", h.overtimeHours("e1", 0, 50, 15), 3);
        check("overtime at threshold", h.overtimeHours("e1", 0, 50, 18), 0);
        check("overtime below", h.overtimeHours("e1", 0, 50, 25), 0);
        check("overtime e2", h.overtimeHours("e2", 0, 50, 2), 1);
        check("overtime unknown", h.overtimeHours("nope", 0, 50, 10), null);

        check("pay with overtime", h.payForPeriod("e1", 0, 50, 15), 390);
        check("pay no overtime", h.payForPeriod("e1", 0, 50, 18), 360);
        check("pay e2", h.payForPeriod("e2", 0, 50, 2), 35);
        check("pay unknown", h.payForPeriod("nope", 0, 50, 10), null);

        h.clockIn("e1", 40);
        check("hours before open shift", h.hoursAt("e1", 35), 18);
        check("hours at open start", h.hoursAt("e1", 40), 18);
        check("hours open clipped", h.hoursAt("e1", 45), 23);
        check("pay with open shift", h.payForPeriod("e1", 0, 50, 15), 690);

        System.out.println("Level 3: " + passed + " passed, " + failed + " failed");
    }
}
