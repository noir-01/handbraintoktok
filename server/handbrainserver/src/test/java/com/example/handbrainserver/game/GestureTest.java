package com.example.handbrainserver.game;

import com.example.handbrainserver.game.util.Gesture;
import com.example.handbrainserver.game.util.GesturePair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;

public class GestureTest {
    @Test
    public void calcTest(){
        Assertions.assertTrue(Gesture.calcHand(1,new GesturePair(Gesture.THUMB_UP,null)));
        Assertions.assertTrue(Gesture.calcHand(7,new GesturePair(Gesture.TWO,Gesture.FIVE)));
        Assertions.assertTrue(Gesture.calcHand(6,new GesturePair(Gesture.THREE,Gesture.THREE)));
    }
}
