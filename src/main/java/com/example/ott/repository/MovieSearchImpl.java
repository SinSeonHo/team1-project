package com.example.ott.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.ott.dto.PageRequestDTO;
import com.example.ott.entity.Movie;
import com.example.ott.entity.QMovie;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MovieSearchImpl implements MovieSearch {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Movie> search(PageRequestDTO requestDTO) {
        QMovie movie = QMovie.movie;
        BooleanBuilder builder = new BooleanBuilder();

        String[] types = requestDTO.getTypes();
        String keyword = requestDTO.getKeyword();

        if (types.length > 0 && keyword != null && !keyword.isEmpty()) {
            BooleanBuilder searchBuilder = new BooleanBuilder();

            for (String type : types) {
                switch (type) {
                    case "t":
                        searchBuilder.or(movie.title.containsIgnoreCase(keyword));
                        break;
                    case "d":
                        searchBuilder.or(movie.director.containsIgnoreCase(keyword));
                        break;
                    case "g":
                        searchBuilder.or(movie.genres.containsIgnoreCase(keyword));
                        break;
                }
            }
            builder.and(searchBuilder);
        }

        Pageable pageable = requestDTO.getPageable(Sort.by("id").descending());

        JPAQuery<Movie> query = queryFactory
                .selectFrom(movie)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<Movie> result = query.fetch();
        long count = queryFactory.selectFrom(movie).where(builder).fetchCount();

        return new PageImpl<>(result, pageable, count);
    }
}
