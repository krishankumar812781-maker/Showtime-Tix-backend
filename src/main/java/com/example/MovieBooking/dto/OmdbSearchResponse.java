package com.example.MovieBooking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OmdbSearchResponse {
    @JsonProperty("Search")
    private List<OmdbSearchResult> searchResults;
}
