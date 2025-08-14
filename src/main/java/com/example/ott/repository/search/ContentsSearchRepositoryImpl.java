package com.example.ott.repository.search;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.ott.dto.PageRequestDTO;
import com.example.ott.entity.Contents;
import com.example.ott.entity.QContents;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContentsSearchRepositoryImpl implements ContentsSearchRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Contents> search(PageRequestDTO pagerequestDTO) {
        QContents content = QContents.contents;
        BooleanBuilder builder = new BooleanBuilder();

        String[] types = pagerequestDTO.getTypes();
        String keyword = pagerequestDTO.getKeyword();

        if (types.length > 0 && keyword != null && !keyword.isEmpty()) {
            BooleanBuilder searchBuilder = new BooleanBuilder();
            for (String type : types) {
                switch (type) {
                    case "t":
                        searchBuilder.or(content.title.containsIgnoreCase(keyword));
                        break;
                }
            }
            builder.and(searchBuilder);
        }

        Pageable pageable = pagerequestDTO.getPageable(Sort.by("title").descending());

        JPAQuery<Contents> query = queryFactory
                .selectFrom(content)
                .where(builder)
                .orderBy() // 우선 정렬 기준
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<Contents> result = query.fetch();
        long count = queryFactory.selectFrom(content).where(builder).fetchCount();

        return new PageImpl<>(result, pageable, count);
    }

}
