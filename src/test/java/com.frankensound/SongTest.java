package com.frankensound;

import com.frankensound.entities.Song;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class SongTest {

    @Test
    @TestTransaction
    public void testGetByKey() {
        String key = "My song";

        Song song = new Song();
        song.key = key;

        song.persist();

        assertEquals(Song.findByKey(key), song);
    }
}
