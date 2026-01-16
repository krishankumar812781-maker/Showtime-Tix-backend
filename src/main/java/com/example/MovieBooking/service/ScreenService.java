package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.RequestDto.ScreenRequestDto;
import com.example.MovieBooking.dto.ScreenResponseDto;
import com.example.MovieBooking.entity.Screen;
import com.example.MovieBooking.entity.Theater;
import com.example.MovieBooking.repository.ScreenRepository;
import com.example.MovieBooking.repository.TheaterRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScreenService {

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private ModelMapper modelMapper;


    public ScreenResponseDto addScreen(ScreenRequestDto screenRequestDto) {

        // 1. Find the parent Theater entity
        Theater theater = theaterRepository.findById(screenRequestDto.getTheaterId())
                .orElseThrow(() -> new RuntimeException("Theater not found with id: " + screenRequestDto.getTheaterId()));

        // 2. Manually create the new Screen object
        //    (This avoids the ModelMapper error of mapping theaterId to id)
        Screen screen = new Screen();
        screen.setName(screenRequestDto.getName());
        screen.setScreenType(screenRequestDto.getScreenType());
        screen.setTheater(theater);

        // 3. Save the new screen (its ID will be null, so JPA performs an INSERT)
        Screen savedScreen = screenRepository.save(screen);

        // 4. Map the saved entity to a response DTO
        return mapToScreenResponseDto(savedScreen);
    }


    public List<ScreenResponseDto> getScreensByTheater(Long theaterId) {
        // 1. Find all screens for the theater
        List<Screen> screens = screenRepository.findByTheaterId(theaterId);

        // 2. Map the list of entities to a list of DTOs
        return screens.stream()
                .map(this::mapToScreenResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map a Screen entity to its response DTO.
     * We can safely use ModelMapper here.
     */
    private ScreenResponseDto mapToScreenResponseDto(Screen screen) {
        // Use ModelMapper for the simple fields
        ScreenResponseDto dto = modelMapper.map(screen, ScreenResponseDto.class);

        // Manually set the flattened "theaterName" field
        dto.setTheaterName(screen.getTheater().getName());

        return dto;
    }
}