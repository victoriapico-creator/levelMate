package com.example.notesbysubject

import com.example.notesbysubject.ui.camera.calculateTiltAngle
import com.example.notesbysubject.ui.camera.applyLowPassFilter
import com.example.notesbysubject.ui.camera.isWithinTolerance
import org.junit.Assert.*
import org.junit.Test

class LevelCalculatorTest {
    @Test fun configuredToleranceChangesLevelDecision() {
        assertFalse(isWithinTolerance(1.6f))
        assertTrue(isWithinTolerance(1.6f, 2f))
        assertTrue(isWithinTolerance(-1.6f, 2f))
    }

    @Test fun uprightAndSidewaysAngles() {
        assertEquals(0f, calculateTiltAngle(0f, 9.8f), 0.001f)
        assertEquals(90f, calculateTiltAngle(9.8f, 0f), 0.001f)
    }

    @Test fun smoothingMovesPartOfTheWay() {
        assertEquals(2f, applyLowPassFilter(10f, 0f), 0.001f)
    }
}
