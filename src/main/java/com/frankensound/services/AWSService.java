package com.frankensound.services;

import com.frankensound.entities.Song;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AWSService {
    public Song getByKey(String key) {
        // TODO: Implement returning a link or the file from AWS
        return new Song();
    }
}
