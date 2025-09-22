package com.ap4.common.utils;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.testng.AssertJUnit.assertNotNull;

public class DateTimeUtilTest {
    @Test
    public void testFormatDateTime() {
        // Arrange
        DateTimeUtil dateTimeUtil = new DateTimeUtil();
        Date date = new Date();

        // Act
        String result = dateTimeUtil.formatDateTime(date);

        // Assert
        assertNotNull(result);
    }
}
