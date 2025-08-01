package com.example.ott.repository.search;

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
                    case "a":
                        searchBuilder.or(movie.actors.containsIgnoreCase(keyword));
                        break;
                    case "c":
                        searchBuilder.or(movie.nationNm.containsIgnoreCase(keyword));
                        break;
                }
            }
            builder.and(searchBuilder);
        }
        Pageable pageable = requestDTO.getPageable(Sort.by("rank").descending());

        JPAQuery<Movie> query = queryFactory
                .selectFrom(movie)
                .where(builder)
                .orderBy(movie.rank.asc()) // 우선 랭크 기준으로 정렬!
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<Movie> result = query.fetch();
        long count = queryFactory.selectFrom(movie).where(builder).fetchCount();

        return new PageImpl<>(result, pageable, count);
    }
}
