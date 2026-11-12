package hwork;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * >>> THIS IS THE FILE YOU WRITE YOUR CODE IN <<<
 *
 * Muse-composed mock of the CodeSignal Industry Coding Assessment
 * "Time-Aware Working Hours Register" style problem (4 levels, ~90 minutes,
 * hard-gated). Full spec: PROBLEM-HOURS.md. This is NOT CodeSignal's
 * real spec.
 *
 * Implement level by level. Move to the next level only when the current
 * level's tests pass:  ./run-hours.sh 1   then  ./run-hours.sh 2   ...
 * (run-hours.sh enforces the real gating: a level unlocks only if all
 * previous levels are green.)
 *
 * DESIGN TIP (the whole game): levels build on the same data model.
 * From Level 1, store each employee as an OBJECT carrying its shift
 * EVENT LOG (clock-in / clock-out events), not just a total-hours field.
 * Level 3 punishes flat total-hours models with a rewrite — mid-clock,
 * that's fatal. Hours-as-of needs per-shift times, overtime needs a
 * per-period breakdown, pay needs a regular/overtime split: all derivable
 * only from the log.
 *
 * NOTE: timestamps strictly increase across calls — you never need to
 * handle out-of-order timestamps. Durations are whole time units
 * (clockOut - clockIn). Ranges are half-open [start, end).
 */
public class Hours implements hapi.Hours {

    // TODO: choose your data model here. Suggested:
    // private Map<String, Employee> employees = new HashMap<>();
    // where Employee = { String name; int hourlyRate;
    //                    List<Shift> shifts };
    // and Shift = { int inTs; Integer outTs /* null = open */ }.
    // Hours in a window = sum over shifts of the overlap with the window
    // (open shift clipped to the window end). Timeless totals count
    // completed shifts only; as-of and period queries clip open shifts
    // to the query time / period end.

    // ---------------- Level 1: basic shifts (suggested: 10 min) ----------------

    class Employee{
        String id; String name; Integer hourlyRate;

        public Employee(String id, String name, int hourlyRate) {
            this.id = id;
            this.name = name;
            this.hourlyRate = hourlyRate;
            shifts = new ArrayList<>();
        }
        List<Shift> shifts;
    }

    class Shift{
        Integer in;
        Integer out;
    }

    TreeMap<String, Employee> employeeMap = new TreeMap<>();

    @Override
    public void addEmployee(String id, String name, int hourlyRate) {
        employeeMap.put(id, new Employee(id, name, hourlyRate));
    }

    @Override
    public String getEmployee(String id) {
        if(!employeeMap.containsKey(id)){
            return null;
        }
        Employee employee = employeeMap.get(id);
        return employee.name + "," + getTotalHours(id);
    }

    @Override
    public Integer getTotalHours(String id) {
        if(!employeeMap.containsKey(id)){
            return null;
        }
        Employee employee = employeeMap.get(id);
        int total = 0;
        return employee.shifts.stream()
                .filter(s -> s.out != null)
                .mapToInt(s -> s.out - s.in)
                .sum();
    }

    @Override
    public boolean clockIn(String id, int timestamp) {
        if(!employeeMap.containsKey(id)){
            return false;
        }
        Employee employee = employeeMap.get(id);

        if(employee.shifts.isEmpty() || employee.shifts.getLast().out != null){
            Shift shift = new Shift();
            shift.in = timestamp;
            employee.shifts.add(shift);
            return true;
        }
        return false;
    }

    @Override
    public boolean clockOut(String id, int timestamp) {
        if(!employeeMap.containsKey(id)){
            return false;
        }
        Employee employee = employeeMap.get(id);
        if(employee.shifts.isEmpty() || employee.shifts.getLast().out != null){
            return false;
        }
        employee.shifts.getLast().out = timestamp;
        return true;
    }

    @Override
    public List<String> listEmployees() {
        return employeeMap.keySet().stream().collect(Collectors.toList());
    }

    @Override
    public boolean removeEmployee(String id) {
        if(!employeeMap.containsKey(id)){
            return false;
        }
        employeeMap.remove(id);
        return true;
    }

    // ---------------- Level 2: search, ranking & ranges (suggested: 15 min) ----------------

    @Override
    public List<String> searchByNamePrefix(String prefix) {
        return employeeMap.values().stream()
                .filter(e -> e.name.startsWith(prefix))
                .sorted(Comparator.comparing((Employee e) -> e.name).thenComparing(e -> e.id))
                .map(e -> e.name + "(" + e.id + ")")
                .collect(Collectors.toList());
    }

    @Override
    public List<String> topKByHours(int k) {
        return employeeMap.values().stream()
                .sorted(Comparator.comparingInt((Employee e) -> getTotalHours(e.id)).reversed().thenComparing(e -> e.id))
                .limit(k)
                .map(e -> e.id + "(" + getTotalHours(e.id) + ")")
                .collect(Collectors.toList());
    }

    @Override
    public int totalHours() {
        return employeeMap.values().stream()
                .mapToInt(e -> getTotalHours(e.id))
                .sum();
    }

    @Override
    public int averageHours() {
        return (int)employeeMap.values().stream()
                .mapToInt(e -> getTotalHours(e.id))
                .average().orElse(0);
    }

    @Override
    public Integer hoursInRange(String id, int start, int end) {
        if(!employeeMap.containsKey(id)){
            return null;
        }
        Employee employee = employeeMap.get(id);
        int result = 0;
        for(Shift shift : employee.shifts){
             int s = Math.max(start, shift.in);
             int e = Math.min(end, shift.out == null ? end : shift.out);
             if(e - s > 0){
                 result += e - s;
             }
        }
        return result;
    }

    // ---------------- Level 3: time-state, overtime & pay (suggested: 25 min) ----------------

    @Override
    public Integer hoursAt(String id, int timestamp) {
        return hoursInRange(id, Integer.MIN_VALUE, timestamp);
    }

    @Override
    public int totalHoursAt(int timestamp) {
        return employeeMap.values().stream()
                .mapToInt(e -> hoursAt(e.id, timestamp))
                .sum();
    }

    @Override
    public Integer overtimeHours(String id, int start, int end, int threshold) {
        /*
        if(!employeeMap.containsKey(id)){
            return null;
        }
        Employee employee = employeeMap.get(id);
        int result = 0;
        for(Shift shift : employee.shifts){
            int s = Math.max(start, shift.in);
            int e = Math.min(end, shift.out == null ? end : shift.out);
            if(e - s > 0){
                int hours = e - s;
                result += Math.max(hours - threshold, 0);
            }
        }
        return result;
        */
        Integer hours = hoursInRange(id, start, end);
        if(hours == null){
            return null;
        }
        return Math.max(0, hours - threshold);
    }

    @Override
    public Integer payForPeriod(String id, int start, int end, int threshold) {
        Integer hours = hoursInRange(id, start, end);
        if(hours == null){
            return null;
        }
        Employee employee = employeeMap.get(id);
        Integer overtime = overtimeHours(id, start, end, threshold);
        int result = 0;
        int regularHours = Math.min(hours, threshold);
        result += employee.hourlyRate * regularHours;
        result += (overtime * employee.hourlyRate * 3)/2;
        return result;
    }

    // ---------------- Level 4: statistics, reports & at-time ranking (suggested: 30 min) ----------------

    @Override
    public List<String> shiftHistory(String id) {
        if(!employeeMap.containsKey(id)){
            return new ArrayList<>();
        }
        Employee employee = employeeMap.get(id);
        List<String> result = new ArrayList<>();
        for(Shift shift : employee.shifts){
            result.add(shift.in + ":" + "CLOCK_IN");
            if(shift.out != null){
                result.add(shift.out + ":" + "CLOCK_OUT");
            }
        }
        return result;
    }

    @Override
    public Integer shiftCount(String id) {
        if(!employeeMap.containsKey(id)){
            return null;
        }
        Employee employee = employeeMap.get(id);
        return employee.shifts.size();
    }

    @Override
    public Integer longestShift(String id) {
        if(!employeeMap.containsKey(id)){
            return null;
        }
        Employee employee = employeeMap.get(id);
        return (int)employee.shifts.stream()
                .filter(s -> s.out != null)
                .mapToInt(s -> s.out - s.in)
                .max().orElse(0);
    }

    @Override
    public Integer averageShiftDuration(String id) {
        if(!employeeMap.containsKey(id)){
            return null;
        }
        Employee employee = employeeMap.get(id);
        return (int)employee.shifts.stream()
                .filter(s -> s.out != null)
                .mapToInt(s -> s.out - s.in)
                .average().orElse(0);
    }

    @Override
    public List<String> overtimeReport(int start, int end, int threshold) {
        return employeeMap.values().stream()
                .filter(e -> overtimeHours(e.id, start, end, threshold) != 0)
                .sorted(Comparator.comparingInt((Employee e) -> overtimeHours(e.id, start, end, threshold)).reversed().thenComparing(e -> e.id) )
                .map(e -> e.id + "(" + overtimeHours(e.id, start, end, threshold) + ")")
                .collect(Collectors.toList());
    }

    @Override
    public List<String> payrollByPeriod(int start, int end, int threshold) {
        return employeeMap.values().stream()
                .filter(e -> payForPeriod(e.id, start, end, threshold) != 0)
                .sorted(Comparator.comparingInt((Employee e) -> payForPeriod(e.id, start, end, threshold)).reversed().thenComparing(e -> e.id))
                .map(e -> e.id + "(" + payForPeriod(e.id, start, end, threshold) + ")")
                .collect(Collectors.toList());
    }

    @Override
    public List<String> topKByHoursAt(int k, int timestamp) {
        return employeeMap.values().stream()
                .sorted(Comparator.comparingInt((Employee e) -> hoursAt(e.id, timestamp)).reversed().thenComparing(e -> e.id))
                .limit(k)
                .map(e -> e.id + "(" + hoursAt(e.id, timestamp) + ")")
                .collect(Collectors.toList());
    }
}
