package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.RequestDto.MovieRequestDto;
import com.example.MovieBooking.dto.MovieResponseDto;
import com.example.MovieBooking.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    // --- Public Endpoints (Return ResponseDto) ---

    @GetMapping("/getallmovies")
    public ResponseEntity<List<MovieResponseDto>> getAllMovies(){
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/getmoviesbygenre")
    public ResponseEntity<List<MovieResponseDto>> getMoviesByGenre(@RequestParam String genre){
        return ResponseEntity.ok(movieService.getMoviesByGenre(genre));
    }

    @GetMapping("/getmoviesbylanguage")
    public ResponseEntity<List<MovieResponseDto>> getMoviesByLanguage(@RequestParam String language){
        return ResponseEntity.ok(movieService.getMoviesByLanguage(language));
    }


    @GetMapping("/{id}")
    public ResponseEntity<MovieResponseDto> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    /**
     * Naive search for customers
     */
    @GetMapping("/search")
    public ResponseEntity<List<MovieResponseDto>> searchMovies(@RequestParam String title) {
        return ResponseEntity.ok(movieService.searchMovies(title));
    }

    // --- Admin Only Operations (Use RequestDto for Input) ---

    @PostMapping("/addmovie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<MovieResponseDto> addMovie(@RequestBody MovieRequestDto movieRequestDTO) {
        return new ResponseEntity<>(movieService.addMovie(movieRequestDTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/deletemovie/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteMovie(@PathVariable Long id){
        movieService.deleteMovie(id);
        return ResponseEntity.ok("Movie deleted successfully");
    }


    // --- OMDb External API Endpoints ---

    /**
     * Search OMDb for movies by title before adding them to our DB.
     * This usually returns a list of simplified search results.
     */
    @GetMapping("/omdb/search")
    public ResponseEntity<?> searchOmdb(@RequestParam String title) {
        return ResponseEntity.ok(movieService.searchOmdb(title));
    }

    /**
     * Import a movie from OMDb using its imdbId.
     * Returns the full MovieResponseDto after saving to local DB.
     */
    @PostMapping("/omdb/import/{imdbId}")
    public ResponseEntity<MovieResponseDto> importMovie(@PathVariable String imdbId) {
        return new ResponseEntity<>(movieService.importMovieByImdbId(imdbId), HttpStatus.CREATED);
    }
}