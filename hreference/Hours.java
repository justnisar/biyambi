package hreference;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/** Reference solution for the Time-Aware Working Hours Register mock. */
public class Hours implements hapi.Hours {

    static class Shift {
        int inTs;
        Integer outTs; // null = open (no clock-out yet)

        Shift(int inTs) {
            this.inTs = inTs;
        }
    }

    static class Employee {
        String id;
        String name;
        int hourlyRate;
        List<Shift> shifts = new ArrayList<>(); // appended in time order (timestamps strictly increase)

        Employee(String id, String name, int hourlyRate) {
            this.id = id;
            this.name = name;
            this.hourlyRate = hourlyRate;
        }
    }

    Map<String, Employee> employees = new HashMap<>();

    private Shift openShift(Employee e) {
        for (Shift s : e.shifts) {
            if (s.outTs == null) return s;
        }
        return null;
    }

    /** Overlap of one shift with [start, end); an open shift is clipped to end. */
    private int overlap(Shift s, int start, int end) {
        int effOut = s.outTs == null ? end : s.outTs;
        return Math.max(0, Math.min(effOut, end) - Math.max(s.inTs, start));
    }

    private int hoursInRange(Employee e, int start, int end) {
        int h = 0;
        for (Shift s : e.shifts) h += overlap(s, start, end);
        return h;
    }

    /** Hours worked up to timestamp: overlap with (-inf, timestamp). */
    private int hoursUpTo(Employee e, int timestamp) {
        return hoursInRange(e, Integer.MIN_VALUE, timestamp);
    }

    /** Completed shifts only; open shifts contribute 0 (no reference time to clip to). */
    private int completedHours(Employee e) {
        int h = 0;
        for (Shift s : e.shifts) {
            if (s.outTs != null) h += s.outTs - s.inTs;
        }
        return h;
    }

    private int overtime(Employee e, int start, int end, int threshold) {
        return Math.max(0, hoursInRange(e, start, end) - threshold);
    }

    private int pay(Employee e, int start, int end, int threshold) {
        int hours = hoursInRange(e, start, end);
        int ot = Math.max(0, hours - threshold);
        int regular = hours - ot;
        return regular * e.hourlyRate + (ot * e.hourlyRate * 3) / 2;
    }

    // ---------------- Level 1 ----------------

    @Override
    public void addEmployee(String id, String name, int hourlyRate) {
        employees.put(id, new Employee(id, name, hourlyRate));
    }

    @Override
    public String getEmployee(String id) {
        Employee e = employees.get(id);
        return e == null ? null : e.name + "," + completedHours(e);
    }

    @Override
    public Integer getTotalHours(String id) {
        Employee e = employees.get(id);
        return e == null ? null : completedHours(e);
    }

    @Override
    public boolean clockIn(String id, int timestamp) {
        Employee e = employees.get(id);
        if (e == null || openShift(e) != null) return false;
        e.shifts.add(new Shift(timestamp));
        return true;
    }

    @Override
    public boolean clockOut(String id, int timestamp) {
        Employee e = employees.get(id);
        Shift open = e == null ? null : openShift(e);
        if (open == null) return false;
        open.outTs = timestamp;
        return true;
    }

    @Override
    public List<String> listEmployees() {
        return employees.values().stream()
                .map(e -> e.id).sorted().collect(Collectors.toList());
    }

    @Override
    public boolean removeEmployee(String id) {
        return employees.remove(id) != null;
    }

    // ---------------- Level 2 ----------------

    @Override
    public List<String> searchByNamePrefix(String prefix) {
        return employees.values().stream()
                .filter(e -> e.name.startsWith(prefix))
                .sorted(Comparator.comparing((Employee e) -> e.name).thenComparing(e -> e.id))
                .map(e -> e.name + "(" + e.id + ")")
                .collect(Collectors.toList());
    }

    @Override
    public List<String> topKByHours(int k) {
        return employees.values().stream()
                .sorted(Comparator.comparingInt((Employee e) -> completedHours(e))
                        .reversed().thenComparing(e -> e.id))
                .limit(k)
                .map(e -> e.id + "(" + completedHours(e) + ")")
                .collect(Collectors.toList());
    }

    @Override
    public int totalHours() {
        return employees.values().stream().mapToInt(e -> completedHours(e)).sum();
    }

    @Override
    public int averageHours() {
        if (employees.isEmpty()) return 0;
        return totalHours() / employees.size();
    }

    @Override
    public Integer hoursInRange(String id, int start, int end) {
        Employee e = employees.get(id);
        return e == null ? null : hoursInRange(e, start, end);
    }

    // ---------------- Level 3 ----------------

    @Override
    public Integer hoursAt(String id, int timestamp) {
        Employee e = employees.get(id);
        return e == null ? null : hoursUpTo(e, timestamp);
    }

    @Override
    public int totalHoursAt(int timestamp) {
        return employees.values().stream().mapToInt(e -> hoursUpTo(e, timestamp)).sum();
    }

    @Override
    public Integer overtimeHours(String id, int start, int end, int threshold) {
        Employee e = employees.get(id);
        return e == null ? null : overtime(e, start, end, threshold);
    }

    @Override
    public Integer payForPeriod(String id, int start, int end, int threshold) {
        Employee e = employees.get(id);
        return e == null ? null : pay(e, start, end, threshold);
    }

    // ---------------- Level 4 ----------------

    @Override
    public List<String> shiftHistory(String id) {
        Employee e = employees.get(id);
        if (e == null) return List.of();
        List<String> out = new ArrayList<>();
        for (Shift s : e.shifts) {
            out.add(s.inTs + ":CLOCK_IN");
            if (s.outTs != null) out.add(s.outTs + ":CLOCK_OUT");
        }
        return out;
    }

    @Override
    public Integer shiftCount(String id) {
        Employee e = employees.get(id);
        return e == null ? null : e.shifts.size();
    }

    @Override
    public Integer longestShift(String id) {
        Employee e = employees.get(id);
        if (e == null) return null;
        int longest = 0;
        for (Shift s : e.shifts) {
            if (s.outTs != null) longest = Math.max(longest, s.outTs - s.inTs);
        }
        return longest;
    }

    @Override
    public Integer averageShiftDuration(String id) {
        Employee e = employees.get(id);
        if (e == null) return null;
        int sum = 0, count = 0;
        for (Shift s : e.shifts) {
            if (s.outTs != null) {
                sum += s.outTs - s.inTs;
                count++;
            }
        }
        return count == 0 ? 0 : sum / count;
    }

    @Override
    public List<String> overtimeReport(int start, int end, int threshold) {
        return employees.values().stream()
                .filter(e -> overtime(e, start, end, threshold) > 0)
                .sorted(Comparator.comparingInt((Employee e) -> overtime(e, start, end, threshold))
                        .reversed().thenComparing(e -> e.id))
                .map(e -> e.id + "(" + overtime(e, start, end, threshold) + ")")
                .collect(Collectors.toList());
    }

    @Override
    public List<String> payrollByPeriod(int start, int end, int threshold) {
        return employees.values().stream()
                .filter(e -> hoursInRange(e, start, end) > 0)
                .sorted(Comparator.comparingInt((Employee e) -> pay(e, start, end, threshold))
                        .reversed().thenComparing(e -> e.id))
                .map(e -> e.id + "(" + pay(e, start, end, threshold) + ")")
                .collect(Collectors.toList());
    }

    @Override
    public List<String> topKByHoursAt(int k, int timestamp) {
        return employees.values().stream()
                .sorted(Comparator.comparingInt((Employee e) -> hoursUpTo(e, timestamp))
                        .reversed().thenComparing(e -> e.id))
                .limit(k)
                .map(e -> e.id + "(" + hoursUpTo(e, timestamp) + ")")
                .collect(Collectors.toList());
    }
}
