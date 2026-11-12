#!/bin/bash
# Usage: ./run-hours.sh [1|2|3|4|all] [src|solution]
#   ./run-hours.sh 1          -> run Level 1 tests against your work implementation
#   ./run-hours.sh 2          -> runs Level 1 first; Level 2 unlocks only if Level 1 is green
#   ./run-hours.sh all        -> levels in order, stops at the first red level (like the real gating)
#   ./run-hours.sh all solution -> run everything against the reference solution
#
# Gating mirrors the real Industry Coding Assessment: a level's tests only run
# once every previous level is green.
LEVEL=${1:-all}
IMPL=${2:-src}

case "$IMPL" in
    src)      IMPL_DIR="hwork"; IMPL_CLASS="hwork.Hours" ;;
    solution) IMPL_DIR="hreference"; IMPL_CLASS="hreference.Hours" ;;
    *) echo "unknown impl '$IMPL' (use src|solution)"; exit 1 ;;
esac

rm -rf build-hours && mkdir -p build-hours
javac -encoding UTF-8 -d build-hours hapi/Hours.java "$IMPL_DIR"/*.java htest/*.java || exit 1

# Runs a level's tests; succeeds only if all tests passed.
run_level() {
    echo "=== Level $1 ==="
    local out
    out=$(java -cp build-hours "htest.Level$1Test" "$IMPL_CLASS")
    echo "$out"
    echo "$out" | grep -q "0 failed"
}

# Level N unlocks only if levels 1..N-1 are all green.
run_gated() {
    local n=$1 i
    for i in $(seq 1 $((n - 1))); do
        run_level "$i" || {
            echo ">>> Level $i is not green - Level $n stays locked."
            return 1
        }
    done
    run_level "$n"
}

if [ "$LEVEL" = "all" ]; then
    for i in 1 2 3 4; do
        if ! run_level "$i"; then
            [ "$i" -lt 4 ] && echo ">>> Level $((i + 1)) locked until Level $i is green."
            break
        fi
    done
else
    run_gated "$LEVEL"
fi
