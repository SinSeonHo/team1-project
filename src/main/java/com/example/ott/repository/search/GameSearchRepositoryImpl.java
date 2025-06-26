package com.example.ott.repository.search;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.ott.dto.PageRequestDTO;
import com.example.ott.entity.Game;
import com.example.ott.entity.QGame;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GameSearchRepositoryImpl implements GameSearchRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Game> search(PageRequestDTO requestDTO) {
        QGame game = QGame.game;
        BooleanBuilder builder = new BooleanBuilder();

        String[] types = requestDTO.getTypes();
        String keyword = requestDTO.getKeyword();

        if (types.length > 0 && keyword != null && !keyword.isEmpty()) {
            BooleanBuilder searchBuilder = new BooleanBuilder();

            for (String type : types) {
                switch (type) {
                    case "m":
                        return null;
                    case "t":
                        searchBuilder.or(game.title.containsIgnoreCase(keyword));
                        break;
                    case "p":
                        searchBuilder.or(game.publisher.containsIgnoreCase(keyword));
                        break;
                    case "d":
                        searchBuilder.or(game.developer.containsIgnoreCase(keyword));
                        break;
                    case "g":
                        searchBuilder.or(game.genres.containsIgnoreCase(keyword));
                        break;
                }
            }
            builder.and(searchBuilder);
        }

        Pageable pageable = requestDTO.getPageable(Sort.by("rank").descending());

        JPAQuery<Game> query = queryFactory
                .selectFrom(game)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<Game> result = query.fetch();
        long count = queryFactory.selectFrom(game).where(builder).fetchCount();

        return new PageImpl<>(result, pageable, count);
    }
}
