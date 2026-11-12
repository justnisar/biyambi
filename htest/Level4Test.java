package htest;

import hapi.Hours;
import java.util.*;

public class Level4Test {
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
        h.addEmployee("e3", "Carol", 30);

        h.clockIn("e1", 10);
        h.clockOut("e1", 18);
        h.clockIn("e2", 20);
        h.clockOut("e2", 26);
        h.clockIn("e1", 30);
        h.clockOut("e1", 40);
        h.clockIn("e3", 50);
        h.clockOut("e3", 55);
        h.clockIn("e2", 60);

        check("history closed shifts", h.shiftHistory("e1"),
                List.of("10:CLOCK_IN", "18:CLOCK_OUT", "30:CLOCK_IN", "40:CLOCK_OUT"));
        check("history trailing open", h.shiftHistory("e2"),
                List.of("20:CLOCK_IN", "26:CLOCK_OUT", "60:CLOCK_IN"));
        check("history unknown", h.shiftHistory("nope"), List.of());

        check("count e1", h.shiftCount("e1"), 2);
        check("count e2 incl open", h.shiftCount("e2"), 2);
        check("count unknown", h.shiftCount("nope"), null);

        check("longest e1", h.longestShift("e1"), 10);
        check("longest e2 open excluded", h.longestShift("e2"), 6);
        check("longest e3", h.longestShift("e3"), 5);

        check("average e1", h.averageShiftDuration("e1"), 9);
        check("average e2 completed only", h.averageShiftDuration("e2"), 6);
        check("average unknown", h.averageShiftDuration("nope"), null);

        h.addEmployee("e4", "Dave", 15);
        check("count no shifts", h.shiftCount("e4"), 0);
        check("longest no shifts", h.longestShift("e4"), 0);
        check("average no shifts", h.averageShiftDuration("e4"), 0);

        check("overtime report", h.overtimeReport(0, 100, 15),
                List.of("e2(31)", "e1(3)"));
        check("payroll", h.payrollByPeriod(0, 100, 15),
                List.of("e2(615)", "e1(390)", "e3(150)"));

        check("topK at 35", h.topKByHoursAt(2, 35), List.of("e1(13)", "e2(6)"));
        check("topK at 27 tie at zero", h.topKByHoursAt(4, 27),
                List.of("e1(8)", "e2(6)", "e3(0)", "e4(0)"));
        check("topK at 100", h.topKByHoursAt(5, 100),
                List.of("e2(46)", "e1(18)", "e3(5)", "e4(0)"));

        System.out.println("Level 4: " + passed + " passed, " + failed + " failed");
    }
}
