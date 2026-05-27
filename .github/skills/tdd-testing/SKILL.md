---
name: tdd-testing
description: "Use when: implementing features with Test Driven Development, writing failing tests first, or enforcing Red-Green-Refactor before production code changes. Keywords: tdd, tests first, red green refactor, failing test first."
applyTo: src/test/**
---
# Test-First Development Skill

## TDD requirement
* Follow Test Driven Development (TDD): write or update failing tests first, then implement code changes to make them pass.
* Use the Red-Green-Refactor cycle for each change: write a failing test (Red), implement the minimum code to pass (Green), then refactor safely while keeping tests green.
* Do not implement production code for a new behavior until there is a failing test that defines that behavior.

## Test conventions
* Write clear, focused tests that verify one behavior at a time.
* Use descriptive test names that explain what is being tested and the expected outcome.
* Follow Arrange-Act-Assert (AAA) pattern: set up test data, execute the code under test, verify results.
* Keep tests independent so each test runs in isolation.
* Start with the simplest test case, then add edge cases and error conditions.
* Verify tests fail for the right reason before implementation.
* Mock external dependencies to keep tests fast and reliable.

## PR checklist
* Include test additions or updates for every new behavior.
* Confirm at least one relevant test failed before implementation and now passes.
* Keep implementation changes minimal until tests pass.
* Refactor only after green test status is established.
