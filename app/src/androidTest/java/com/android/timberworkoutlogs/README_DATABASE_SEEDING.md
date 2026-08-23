# Database Seeding for Instrumentation Tests

This document explains how to use the `DatabaseSeedingRule` to ensure each test class starts with a fresh database seeded with production data.

## Overview

The `DatabaseSeedingRule` is a JUnit rule that:
- Clears all database tables before each test class runs
- Re-seeds the database with `DatabaseSeeder.seedCatalog()`
- Ensures consistent starting state for all tests in a class

## Usage

### Basic Setup

Add the `DatabaseSeedingRule` to your test class:

```kotlin
@HiltAndroidTest
class MyTestClass : TestCase() {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val databaseSeedingRule = DatabaseSeedingRule()

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        // Your setup code here
    }

    @Test
    fun myTest() = run {
        // Your test will start with fresh seedCatalog()
        // This includes default exercises and templates
    }
}
```

### Rule Order

**Important**: The rule order matters:
1. `HiltAndroidRule` (order = 0) - Sets up dependency injection
2. `DatabaseSeedingRule` (order = 1) - Clears and seeds database
3. `ComposeTestRule` (order = 2) - Sets up UI testing
4. Other rules (order = 3+) - Permission rules, etc.

### What Gets Seeded

The `DatabaseSeedingRule` calls `DatabaseSeeder.seedCatalog()` which provides:
- **Default Exercises**: All predefined exercise definitions
- **Default Templates**: Basic workout templates with exercises

This gives you a consistent, realistic starting state for testing UI interactions.

## Example Implementation

See `WorkoutTemplatesListScreenTest.kt` for a complete example of how to use the rule.

## Benefits

- **Isolation**: Each test class gets a completely fresh database
- **Consistency**: All tests start with the same `seedCatalog()` state  
- **Reliability**: No test pollution between classes
- **Realistic Data**: Tests run against actual production-like data

## Migration Guide

To add database seeding to an existing test class:

1. Import the rule:
   ```kotlin
   import com.android.timberworkoutlogs.rules.DatabaseSeedingRule
   ```

2. Add the rule with correct order:
   ```kotlin
   @get:Rule(order = 1)
   val databaseSeedingRule = DatabaseSeedingRule()
   ```

3. Update other rule orders if needed (ComposeTestRule should be order = 2)

4. Your tests will now start with fresh seeded data!

## Troubleshooting

- **Injection errors**: Ensure `HiltAndroidRule` is order = 0 and `DatabaseSeedingRule` is order = 1
- **Foreign key constraint errors**: The rule uses Room's built-in `clearAllTables()` for proper constraint handling
- **Duplicate data**: The database auto-seeding is disabled in test environments to prevent double seeding
- **Missing data**: Verify `seedCatalog()` contains the data your tests expect
- **Performance**: Database clearing and seeding happens synchronously to guarantee data consistency

## Important Notes

- **Auto-seeding disabled**: When running tests, the database will NOT auto-seed on creation - only the `DatabaseSeedingRule` will seed data
- **Single seeding**: Each test class gets exactly one seeding operation, preventing duplicate data
- **Clean state**: Tests start with a completely fresh database containing only the seeded production data
