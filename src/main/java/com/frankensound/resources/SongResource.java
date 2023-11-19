package com.frankensound.resources;

import com.frankensound.entities.Song;
import com.frankensound.services.AWSService;
import com.frankensound.services.SongService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@Path("/songs")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SongResource {
    @ConfigProperty(name = "quarkus.profile")
    String profile;

    @Inject
    SongService songService;

    @Inject
    AWSService awsService;

    @Inject
    MeterRegistry registry;

    @GET
    @Path("/{key}")
    public Song getOne(@Valid String key) {
        if (profile.equals("prod")) {
            return awsService.getByKey(key);
        } else {
            return songService.getByKey(key);
        }
    }

    @GET
    public List<Song> getAll() {
        registry.counter("songs.get.all", "type", "query");
        return songService.getAll();
    }

    @POST
    @Transactional
    public Song create(@Valid String key) {
        return songService.create(key);
    }

    @PUT
    @Transactional
    @Path("/{id}")
    public Song update(@Valid Long id, @Valid Song song) {

        Song entity = songService.update(id, song);
        if(entity == null) {
            throw new NotFoundException();
        }
        else {
            return entity;
        }
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    public void delete(@Valid Long id) {
        songService.delete(id);
    }
}
