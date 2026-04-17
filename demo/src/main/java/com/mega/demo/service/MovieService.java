package com.mega.demo.service;

import com.mega.demo.dto.Movie;
import com.mega.demo.repository.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository repo;

    // JSON → DB
    public void loadJson() throws Exception {

        repo.createTable(); // 테이블 생성

        ObjectMapper mapper = new ObjectMapper();

        // 프로젝트 루트 기준
        File file = new File("data/movies.json");

        List<Movie> list = Arrays.asList(
                mapper.readValue(file, Movie[].class)
        );

        repo.deleteAll();
        repo.saveAll(list);
    }

    public List<Movie> getAll() {
        return repo.findAll();
    }
}