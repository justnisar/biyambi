package hapi;

import java.util.List;

/**
 * Time-Aware Working Hours Register — Muse-composed mock of the CodeSignal
 * Industry Coding Assessment style problem (4 levels, ~90 minutes,
 * hard-gated). Full spec: PROBLEM-HOURS.md. This is NOT CodeSignal's
 * real spec.
 *
 * Design note (the whole game): levels build on the same model. From
 * Level 1, store each employee as an OBJECT carrying its shift EVENT LOG
 * (clock-in / clock-out events), not just a total-hours field. A flat
 * total-hours model cannot pass Level 3: hours-as-of needs per-shift
 * times, overtime needs a per-period breakdown, pay needs a
 * regular/overtime split — all derivable only from the log.
 *
 * NOTE: timestamps strictly increase across calls — you never need to
 * handle out-of-order timestamps. Durations are whole time units
 * (clockOut - clockIn). Ranges are half-open [start, end).
 */
public interface Hours {

    // ---------------- Level 1: basic shifts (suggested: 10 min) ----------------

    /** Registers the employee, or replaces the record if the id already exists. */
    void addEmployee(String id, String name, int hourlyRate);

    /** Returns "name,totalHours" (completed shifts only), or null if the employee does not exist. */
    String getEmployee(String id);

    /** Completed-shift hours, or null if the employee does not exist. */
    Integer getTotalHours(String id);

    /** Opens a shift. False if unknown or already clocked in. */
    boolean clockIn(String id, int timestamp);

    /** Closes the open shift. False if unknown or not clocked in. */
    boolean clockOut(String id, int timestamp);

    /** Ids of all employees, sorted lexicographically. Empty if none. */
    List<String> listEmployees();

    /** Removes the employee. True iff the employee existed. */
    boolean removeEmployee(String id);

    // ---------------- Level 2: search, ranking & ranges (suggested: 15 min) ----------------

    /** "name(id)" for employees whose name starts with prefix, sorted by name then id. */
    List<String> searchByNamePrefix(String prefix);

    /** Top k by completed hours, "id(hours)", hours desc, ties by id asc. */
    List<String> topKByHours(int k);

    /** Sum of completed hours over all employees, 0 if none. */
    int totalHours();

    /** Completed-hours total / employee count, rounded down, 0 if none. */
    int averageHours();

    /** Hours of one employee within [start, end); open shift clipped to end. Null if unknown. */
    Integer hoursInRange(String id, int start, int end);

    // ---------------- Level 3: time-state, overtime & pay (suggested: 25 min) ----------------

    /** Hours worked up to timestamp (open shift clipped to timestamp). Null if unknown. */
    Integer hoursAt(String id, int timestamp);

    /** Sum of hoursAt over all employees. */
    int totalHoursAt(int timestamp);

    /** Hours in [start, end) beyond threshold, 0 if at/below. Null if unknown. */
    Integer overtimeHours(String id, int start, int end, int threshold);

    /** Regular hours at full rate + overtime hours at 1.5x, using the stored rate. Null if unknown. */
    Integer payForPeriod(String id, int start, int end, int threshold);

    // ---------------- Level 4: statistics, reports & at-time ranking (suggested: 30 min) ----------------

    /** "ts:CLOCK_IN" / "ts:CLOCK_OUT" entries in order; open shift leaves a trailing CLOCK_IN. Empty if unknown. */
    List<String> shiftHistory(String id);

    /** Number of shifts started, including open shifts. Null if unknown. */
    Integer shiftCount(String id);

    /** Longest completed shift duration, 0 if none completed. Null if unknown. */
    Integer longestShift(String id);

    /** Floor average of completed shift durations, 0 if none completed. Null if unknown. */
    Integer averageShiftDuration(String id);

    /** "id(overtime)" for overtime > 0 only, overtime desc, ties by id asc. */
    List<String> overtimeReport(int start, int end, int threshold);

    /** "id(pay)" for period hours > 0 only, pay desc, ties by id asc. */
    List<String> payrollByPeriod(int start, int end, int threshold);

    /** Like topKByHours, but hours as of timestamp. All employees included. */
    List<String> topKByHoursAt(int k, int timestamp);
}
