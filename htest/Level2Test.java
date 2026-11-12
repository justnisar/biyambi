package htest;

import hapi.Hours;
import java.util.*;

public class Level2Test {
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
        h.addEmployee("e2", "Alina", 20);
        h.addEmployee("e3", "Bob", 25);
        h.addEmployee("e4", "Alex", 30);

        check("prefix Al", h.searchByNamePrefix("Al"),
                List.of("Alex(e4)", "Alice(e1)", "Alina(e2)"));
        check("prefix Bob", h.searchByNamePrefix("Bob"), List.of("Bob(e3)"));
        check("prefix none", h.searchByNamePrefix("Z"), List.of());
        check("prefix empty = all", h.searchByNamePrefix(""),
                List.of("Alex(e4)", "Alice(e1)", "Alina(e2)", "Bob(e3)"));

        h.clockIn("e1", 10);
        h.clockOut("e1", 18);
        h.clockIn("e1", 20);
        h.clockOut("e1", 24);
        h.clockIn("e2", 30);
        h.clockOut("e2", 50);
        h.clockIn("e3", 55);
        h.clockOut("e3", 58);

        check("top2", h.topKByHours(2), List.of("e2(20)", "e1(12)"));
        check("topK larger than count", h.topKByHours(9),
                List.of("e2(20)", "e1(12)", "e3(3)", "e4(0)"));

        h.addEmployee("e5", "Ann", 20);
        h.clockIn("e5", 60);
        h.clockOut("e5", 80);
        check("topK tie by id asc", h.topKByHours(2), List.of("e2(20)", "e5(20)"));

        check("total", h.totalHours(), 55);
        check("average", h.averageHours(), 11);

        check("range full shift", h.hoursInRange("e1", 10, 20), 8);
        check("range partial overlaps", h.hoursInRange("e1", 15, 22), 5);
        check("range whole window", h.hoursInRange("e2", 0, 100), 20);
        check("range no shifts", h.hoursInRange("e4", 0, 100), 0);
        check("range unknown", h.hoursInRange("nope", 0, 100), null);

        h.clockIn("e3", 90);
        check("range open shift clipped", h.hoursInRange("e3", 0, 100), 13);
        check("range before open shift", h.hoursInRange("e3", 0, 85), 3);

        h.addEmployee("e6", "Zed", 10);
        check("average floor", h.averageHours(), 9);
        check("total unchanged", h.totalHours(), 55);

        System.out.println("Level 2: " + passed + " passed, " + failed + " failed");
    }
}
