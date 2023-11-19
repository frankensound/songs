package com.frankensound.services;


import com.frankensound.entities.Metadata;
import com.frankensound.entities.Song;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class SongService {
    public Song getByKey(String key) {
        return Song.findByKey(key);
    }

    public Song getById(Long id){
        return Song.findById(id);
    }

    public List<Song> getAll() {
        return Song.listAll();
    }

    public Song create(String key) {
        Song song = new Song();
        song.key = key;

        Metadata metadata = new Metadata();

        metadata.song = song;

        // persist it
        song.persist();
        metadata.persist();

        return song;
    }

    public Song update(Long id, Song updated) {
        // finding a specific person by ID
        Song song = getById(id);

        // update values
        song.key = updated.key;

        return song;
    }

    public boolean delete(Long id) {
        // delete by id
        return Song.deleteById(id);
    }
}