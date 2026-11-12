# Problem statement — Time-Aware Working Hours Register

> **This is a Muse-composed mock in ICA shape, NOT CodeSignal's real spec.**
> It was written to practise the same skills as the Industry Coding Assessment
> (4 levels, ~90 minutes, hard-gated): modelling state that later levels query
> by time. Nothing here is CodeSignal's problem, wording, or test data.

Drop-in third mock for the CodeSignal Industry Coding Assessment shape.
Extract the CONTENTS of the zip into the same project folder as the Workers
and Banking mocks (`workers-v2/`): the folders `hapi/`, `hwork/`,
`hskeleton/`, `hreference/`, `htest/` plus `run-hours.sh` / `reset-hours.sh`
sit alongside the Workers folders (`wapi/`, `work/`) and the Banking folders
(`bapi/`, `bwork/`, `bskeleton/`, `breference/`, `btest/`) with no name
collisions. No new IDE project setup needed.

Implement a time-aware working hours register. Work **level by level** —
each level unlocks only after the previous level's tests pass.
Correctness over efficiency: any code passing the tests suffices.

**Design for extension — the whole game.** Levels build on the same data
model. From Level 1, store each employee as an object carrying its SHIFT
EVENT LOG (clock-in / clock-out events), not just a total-hours field.
A flat total-hours model **cannot** pass Level 3: hours-as-of needs
per-shift times, overtime needs a per-period breakdown, and pay needs a
regular/overtime split — all derivable only from the log. Level 3 punishes
flat current-state models with a rewrite — mid-clock, that's fatal.

Timestamps **strictly increase** across calls — you never need to handle
out-of-order timestamps. Durations are whole time units: a shift's duration
is `clockOut - clockIn`. Ranges are half-open: `[start, end)` covers
`start` inclusive to `end` exclusive. There is no calendar in this problem:
a "week" or "period" is whatever timestamp range the caller passes, and the
overtime threshold is the regular-hours cap for that range.

Open (unclosed) shifts, stated explicitly: a shift with no clock-out yet
has no end time. It contributes **0** to timeless totals (Level 1–2 have no
reference time to clip it to), but in as-of and period queries it counts
as hours worked so far, clipped to the query time or period end.

---

## Level 1 — Basic shifts (suggested: 10 min)

- `void addEmployee(String id, String name, int hourlyRate)`
  Registers the employee, or replaces the record if the id already exists.
  Replacement resets the employee's shifts.
- `String getEmployee(String id)`
  Returns `"name,totalHours"`, or `null` if the employee does not exist.
  `totalHours` counts completed shifts only; open shifts contribute 0.
- `Integer getTotalHours(String id)`
  Completed-shift hours, or `null` if the employee does not exist.
- `boolean clockIn(String id, int timestamp)`
  Opens a shift. Returns `false` if the employee does not exist or is
  already clocked in (an open shift exists); `true` on success.
- `boolean clockOut(String id, int timestamp)`
  Closes the open shift. Returns `false` if the employee does not exist
  or is not clocked in; `true` on success.
- `List<String> listEmployees()`
  Ids of all employees, sorted lexicographically. Empty list if none.
- `boolean removeEmployee(String id)`
  Removes the employee. Returns `true` iff the employee existed.

---

## Level 2 — Search, ranking & ranges (suggested: 15 min)

- `List<String> searchByNamePrefix(String prefix)`
  Employees whose name starts with `prefix`, each formatted as `"name(id)"`,
  sorted by name, ties by id. (An empty prefix matches every employee.)
- `List<String> topKByHours(int k)`
  Top `k` by completed hours, each formatted as `"id(hours)"`,
  hours descending, ties by id ascending. Fewer than `k` employees → all.
- `int totalHours()`
  Sum of completed hours over all employees, `0` if none.
- `int averageHours()`
  Completed-hours total divided by employee count, rounded down (floor),
  `0` if no employees.
- `Integer hoursInRange(String id, int start, int end)`
  Hours of one employee within `[start, end)`: for each shift, the overlap
  of the shift with the window. An open shift is clipped to `end`.
  Returns `null` if the employee does not exist.

---

## Level 3 — Time-state, overtime & pay (suggested: 25 min)

- `Integer hoursAt(String id, int timestamp)`
  Hours worked up to `timestamp`: for each shift, the portion of the shift
  before `timestamp` — a completed shift contributes `min(out, ts) - in`
  if `in < ts`, else 0; an open shift contributes `ts - in` if `in < ts`,
  else 0. Returns `null` if the employee does not exist.
- `int totalHoursAt(int timestamp)`
  Sum of `hoursAt` over all employees.
- `Integer overtimeHours(String id, int start, int end, int threshold)`
  Hours in `[start, end)` beyond `threshold`: `max(0, hours - threshold)`.
  Returns `null` if the employee does not exist.
- `Integer payForPeriod(String id, int start, int end, int threshold)`
  Pay for `[start, end)` at the employee's stored hourly rate: regular
  hours (`min(hours, threshold)`) at the full rate, overtime hours at
  1.5x, computed as `regular * rate + (overtime * rate * 3) / 2`.
  Tests use values where this is exact. Returns `null` if unknown.

---

## Level 4 — Statistics, reports & at-time ranking (suggested: 30 min)

- `List<String> shiftHistory(String id)`
  Shift events in timestamp order: `"inTs:CLOCK_IN"` for each shift,
  followed by `"outTs:CLOCK_OUT"` if the shift is closed. An open shift
  leaves a trailing `CLOCK_IN` with no `CLOCK_OUT`. Empty list if unknown.
- `Integer shiftCount(String id)`
  Number of shifts started, including open shifts. `null` if unknown.
- `Integer longestShift(String id)`
  Longest completed shift duration, `0` if none completed. `null` if unknown.
- `Integer averageShiftDuration(String id)`
  Floor average of completed shift durations, `0` if none completed.
  `null` if unknown.
- `List<String> overtimeReport(int start, int end, int threshold)`
  Per-employee overtime for `[start, end)`, each formatted as
  `"id(overtime)"`, overtime descending, ties by id ascending. Only
  employees with overtime above 0 appear.
- `List<String> payrollByPeriod(int start, int end, int threshold)`
  Per-employee pay for `[start, end)` (same pay rule as Level 3), each
  formatted as `"id(pay)"`, pay descending, ties by id ascending. Only
  employees with period hours above 0 appear.
- `List<String> topKByHoursAt(int k, int timestamp)`
  Like `topKByHours`, but hours as of `timestamp`. All employees are
  included (employees exist from the beginning; only shifts have times).

---

## Scoring (mirrors the real gating)

- A level counts only if **all** its tests pass.
- Suggested budget: 10 / 15 / 25 / 30 minutes per level, 90 total, strict stop.
- Run: `./run-hours.sh 1`, `./run-hours.sh 2`, … `./run-hours.sh all`.
  Reference check: `./run-hours.sh all solution`.
  Fresh start for a timed rehearsal: `./reset-hours.sh`.
