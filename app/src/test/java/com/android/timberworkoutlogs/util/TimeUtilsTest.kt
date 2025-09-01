package com.android.timberworkoutlogs.util

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test
import java.util.Calendar

class TimeUtilsTest {

    @Test
    fun `getGreetingByTime returns Good morning`() {
        // 1. Arrange: Create a mock Calendar
        val mockCalendar = mockk<Calendar>()
        // 2. Stub the method call to simulate 9 AM
        every { mockCalendar.get(Calendar.HOUR_OF_DAY)} returns 9
        // 3. Act: Call the function with the mock
        val greeting = getGreetingByTime(mockCalendar)
        // 4. Assert: Verify the result
        Assert.assertEquals("Good morning!", greeting)
    }
    @Test
    fun `getGreetingByTime returns Good evening`() {
        // 1. Arrange: Create a mock Calendar
        val mockCalendar = mockk<Calendar>()
        // 2. Stub the method call to simulate 9 AM
        every { mockCalendar.get(Calendar.HOUR_OF_DAY)} returns 19
        // 3. Act: Call the function with the mock
        val greeting = getGreetingByTime(mockCalendar)
        // 4. Assert: Verify the result
        Assert.assertEquals("Good evening!", greeting)
    }
    @Test
    fun `getGreetingByTime returns Good afternoon`() {
        // 1. Arrange: Create a mock Calendar
        val mockCalendar = mockk<Calendar>()
        // 2. Stub the method call to simulate 9 AM
        every { mockCalendar.get(Calendar.HOUR_OF_DAY)} returns 15
        // 3. Act: Call the function with the mock
        val greeting = getGreetingByTime(mockCalendar)
        // 4. Assert: Verify the result
        Assert.assertEquals("Good afternoon!", greeting)
    }
    @Test
    fun `getGreetingByTime returns Good evening lower bound`() {
        // 1. Arrange: Create a mock Calendar
        val mockCalendar = mockk<Calendar>()
        // 2. Stub the method call to simulate 9 AM
        every { mockCalendar.get(Calendar.HOUR_OF_DAY)} returns 16
        // 3. Act: Call the function with the mock
        val greeting = getGreetingByTime(mockCalendar)
        // 4. Assert: Verify the result
        Assert.assertEquals("Good afternoon!", greeting)
    }
    @Test
    fun `getGreetingByTime returns Good morning midnight`() {
        // 1. Arrange: Create a mock Calendar
        val mockCalendar = mockk<Calendar>()
        // 2. Stub the method call to simulate 9 AM
        every { mockCalendar.get(Calendar.HOUR_OF_DAY)} returns 0
        // 3. Act: Call the function with the mock
        val greeting = getGreetingByTime(mockCalendar)
        // 4. Assert: Verify the result
        Assert.assertEquals("Good morning!", greeting)
    }
    @Test
    fun `getGreetingByTime returns Good upper  bound`() {
        // 1. Arrange: Create a mock Calendar
        val mockCalendar = mockk<Calendar>()
        // 2. Stub the method call to simulate 9 AM
        every { mockCalendar.get(Calendar.HOUR_OF_DAY)} returns 23
        // 3. Act: Call the function with the mock
        val greeting = getGreetingByTime(mockCalendar)
        // 4. Assert: Verify the result
        Assert.assertEquals("Good evening!", greeting)
    }
}