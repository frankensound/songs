package com.frankensound.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;

@Entity
public class Song extends PanacheEntity {
    public String key;

    @OneToOne(mappedBy = "song", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public Metadata metadata;

    public static Song findByKey(String key){
        return find("key", key).firstResult();
    }
}
