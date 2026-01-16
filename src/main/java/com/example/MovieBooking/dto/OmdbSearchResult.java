package com.example.MovieBooking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OmdbSearchResult {

    private Long id;
    @JsonProperty("Title")
    private String title;
    @JsonProperty("imdbID")
    private String imdbId;
    @JsonProperty("Poster")
    private String poster;
}