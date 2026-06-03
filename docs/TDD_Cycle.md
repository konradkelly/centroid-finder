# TDD Cycle

## Red → Green → Refactor

| Phase | What you do | State of production code |
|---|---|---|
| **Red** | Write a test for behavior that doesn't exist yet | Missing or wrong — test must fail |
| **Green** | Write the minimum code to make the test pass | Just enough to pass — no extras |
| **Refactor** | Clean up without changing behavior | Tests stay green throughout |

Repeat the cycle for every new behavior.

---

## Regression

Once a test is Green, it becomes part of the **regression suite** — the full collection of passing tests re-run after every subsequent change to guard against accidentally breaking old behavior.

```
Write failing test  →  RED
Make it pass        →  GREEN
Make future changes →  run all prior GREEN tests as REGRESSION checks
```

Regression is not a separate phase; it is what the accumulated Green tests do on every build.

---

## Example from this project

| Step | File | What happened |
|---|---|---|
| Red | `ApiExceptionHandlerTest` | `handleFallbackLogsAtErrorLevel` failed — no logger existed |
| Red | `ThumbnailServiceTest` | `generateThumbnailLogsOriginalExceptionWhenReadFails` failed — no logger existed |
| Green | `ApiExceptionHandler.java` | Added `log.error(…)` → test passed |
| Green | `ThumbnailService.java` | Added `log.error(…)` → test passed |
| Regression | Full suite (`mvn test`) | All prior tests re-run to confirm nothing broke |

---

## Key rules

- Never write production code before a failing test exists.
- A test must fail **for the right reason** before you implement.
- Keep each test focused on **one behavior**.
- Refactor only after the suite is Green.
- The full test suite is your regression net — run it after every change.
