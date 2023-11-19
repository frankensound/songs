package com.frankensound.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Metadata extends PanacheEntity {
    @OneToOne
    @JoinColumn(name = "song_id", unique = true)
    public Song song;

    public String name;
    public String artistName;
    public String genre;

    // return name as uppercase in the model
    public String getArtistName(){
        return artistName.toUpperCase();
    }

    // store all names in lowercase in the DB
    public void setArtistName(String name){
        this.artistName = name.toLowerCase();
    }
}
