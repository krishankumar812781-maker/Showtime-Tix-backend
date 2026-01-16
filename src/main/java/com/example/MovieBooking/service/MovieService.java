package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.OmdbDetailDto;
import com.example.MovieBooking.dto.OmdbSearchResponse;
import com.example.MovieBooking.dto.OmdbSearchResult;
import com.example.MovieBooking.dto.RequestDto.MovieRequestDto;
import com.example.MovieBooking.dto.MovieResponseDto;
import com.example.MovieBooking.entity.Movie;
import com.example.MovieBooking.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate;

    @Value("${omdb.api.key}")
    private String apiKey;

    public List<OmdbSearchResult> searchOmdb(String title) {
        String url = "http://www.omdbapi.com/?apikey=" + apiKey + "&s=" + title;
        OmdbSearchResponse response = restTemplate.getForObject(url, OmdbSearchResponse.class);
        return (response != null && response.getSearchResults() != null)
                ? response.getSearchResults() : List.of();
    }

    public MovieResponseDto importMovieByImdbId(String imdbId) {
        String url = "http://www.omdbapi.com/?apikey=" + apiKey + "&i=" + imdbId + "&plot=full";
        OmdbDetailDto detail = restTemplate.getForObject(url, OmdbDetailDto.class);

        if (detail == null || detail.getTitle() == null) {
            throw new RuntimeException("Movie not found on OMDb for ID: " + imdbId);
        }

        Movie movie = new Movie();
        movie.setTitle(detail.getTitle());
        movie.setPlot(detail.getPlot()); // Rich plot text
        movie.setRating(detail.getImdbRating());
        movie.setGenre(detail.getGenre());
        movie.setLanguage(detail.getLanguage());
        movie.setPosterUrl(detail.getPoster());
        movie.setDirector(detail.getDirector());
        movie.setActors(detail.getActors());
        movie.setDuration(detail.getRuntime()); // Storing as String "148 min"

        Movie savedMovie = movieRepository.save(movie);
        return convertToDto(savedMovie);
    }

    public MovieResponseDto addMovie(MovieRequestDto movieRequestDto) {
        Movie movie = modelMapper.map(movieRequestDto, Movie.class);
        Movie savedMovie = movieRepository.save(movie);
        return convertToDto(savedMovie);
    }

    public List<MovieResponseDto> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<MovieResponseDto> getMoviesByGenre(String genre) {
        List<Movie> movies = movieRepository.findByGenre(genre)
                .orElseThrow(() -> new RuntimeException("No movies found for genre: " + genre));
        return movies.stream().map(this::convertToDto).toList();
    }

    public List<MovieResponseDto> getMoviesByLanguage(String language) {
        List<Movie> movies = movieRepository.findByLanguage(language)
                .orElseThrow(() -> new RuntimeException("No movies found for language: " + language));
        return movies.stream().map(this::convertToDto).toList();
    }

    public List<MovieResponseDto> searchMovies(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }

    public MovieResponseDto getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        return convertToDto(movie);
    }

    // ⚡ CONVERTER: Maps the Entity to the Response DTO with all new fields
    private MovieResponseDto convertToDto(Movie movie) {
        MovieResponseDto dto = new MovieResponseDto();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setPlot(movie.getPlot());
        dto.setRating(movie.getRating());
        dto.setLanguage(movie.getLanguage());
        dto.setGenre(movie.getGenre());
        dto.setDuration(movie.getDuration());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setDirector(movie.getDirector());
        dto.setActors(movie.getActors());
        return dto;
    }
}