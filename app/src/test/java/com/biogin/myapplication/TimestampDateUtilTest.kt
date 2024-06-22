package com.biogin.myapplication

import com.biogin.myapplication.utils.TimestampDateUtil
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

class TimestampDateUtilTest {
    val timestampDateUtil = TimestampDateUtil()
    @Test
    fun utcTimestampToLocalStringIsCorrectTimestamp() {
        val utcExpectedDateInMilis = 1728601200000
        val date = Timestamp(Date.from(Instant.ofEpochMilli(utcExpectedDateInMilis)))
        val expectedDateString = "10-10-2024 20:00:00"
        assertEquals(expectedDateString, timestampDateUtil.utcTimestampToLocalString(date))
    }

    @Test
    fun utcTimestampToLocalStringAtStartOfDay() {
        val utcExpectedDateInMilis = 1727751600000
        val date = Timestamp(Date.from(Instant.ofEpochMilli(utcExpectedDateInMilis)))
        val expectedDateString = "01-10-2024 00:00:00"
        assertEquals(expectedDateString, timestampDateUtil.utcTimestampToLocalString(date))
    }
    @Test
    fun utcTimestampToLocalStringAtEndOfDay() {
        val utcExpectedDateInMilis = 1727837999000
        val date = Timestamp(Date.from(Instant.ofEpochMilli(utcExpectedDateInMilis)))
        val expectedDateString = "01-10-2024 23:59:59"
        assertEquals(expectedDateString, timestampDateUtil.utcTimestampToLocalString(date))
    }
    @Test
    fun utcTimestampToLocalStringIsNotTimestamp() {
        val date = Calendar.getInstance().time
        val expectedDateString = ""
        assertEquals(expectedDateString, timestampDateUtil.utcTimestampToLocalString(date))
    }
    @Test
    fun stringDateToLocalDate() {
        val dateToParse = "15/10/2024"
        val expectedLocalDate = LocalDate.of(2024, 10, 15)

        assertEquals(expectedLocalDate, timestampDateUtil.stringDateToLocalDate(dateToParse))
    }

    @Test
    fun stringDateToLocalDateAtStartOfMonth() {
        val dateToParse = "01/10/2024"
        val expectedLocalDate = LocalDate.of(2024, 10, 1)

        assertEquals(expectedLocalDate, timestampDateUtil.stringDateToLocalDate(dateToParse))
    }

    @Test
    fun stringDateToLocalDateAtEndOfMonth() {
        val dateToParse = "30/06/2024"
        val expectedLocalDate = LocalDate.of(2024, 6, 30)

        assertEquals(expectedLocalDate, timestampDateUtil.stringDateToLocalDate(dateToParse))
    }

}