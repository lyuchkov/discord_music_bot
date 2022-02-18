package ru.lyuchkov.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class TimeUtilsTest {

    @Test
    public void getValidTimeStringMethodTest() {
        Assertions.assertEquals("1:18", TimeUtils.length(78000));
    }
    @Test
    public void getValidTimeZeroStringMethodTest() {
        Assertions.assertEquals("0:0", TimeUtils.length(0));
    }
}