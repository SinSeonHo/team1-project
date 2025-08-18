package com.example.ott.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;

import com.example.ott.config.PasswordEncoderConfig;
import com.example.ott.entity.Contents;
import com.example.ott.entity.Socials;
import com.example.ott.entity.User;
import com.example.ott.entity.UserRole;
import com.example.ott.service.FollowedContentsService;
import com.example.ott.service.UserService;
import com.example.ott.type.Gender;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ContentsRepository contentsRepository;
    @Autowired
    private FollowedContentsService followedContentsService;

    // 나이, 성별 범위
    private final int[] AGE_GROUPS = { 10, 20, 30, 40, 50 };
    private final Gender[] GENDERS = { Gender.MAN, Gender.WOMAN };
    private final Socials[] SOCIALS = { Socials.KAKAO, Socials.GOOGLE, Socials.NAVER };
    private int makeUserCnt = 50; // 추가할 유저 수
    private static final int TARGET_PER_USER = 5;
    private static final int MAX_ATTEMPTS_PER_USER = 30; // 랜덤 뽑기 실패 대비

    @Transactional
    @Rollback(false)
    @Test
    public void addTestUser() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<User> createdUsers = new ArrayList<>();

        // 유저 추가
        for (int i = 0; i < makeUserCnt; i++) { // 오프바이원 수정
            int ageGroup = AGE_GROUPS[rnd.nextInt(AGE_GROUPS.length)];
            int age = ageGroup + rnd.nextInt(10);
            Socials social = SOCIALS[rnd.nextInt(SOCIALS.length)];
            String email = "test" + i + getEmailDomain(social);

            User user = User.builder()
                    .name("김분신" + i)
                    .id("test" + i) // @Id가 String이 아니라면 loginId 같은 별도 필드 권장
                    .nickname("김분신" + i)
                    .password(passwordEncoder.encode("1111"))
                    .userRole(UserRole.USER)
                    .age(age)
                    .gender(GENDERS[rnd.nextInt(GENDERS.length)])
                    .email(email)
                    .social(social)
                    .build();

            createdUsers.add(userRepository.save(user));
        }

        // 유저당 콘텐츠 랜덤 5개씩 팔로우 (방금 만든 유저만)
        for (User user : createdUsers) {
            int created = 0;
            int attempts = 0;
            Set<String> triedContentIds = new HashSet<>();

            while (created < TARGET_PER_USER && attempts < MAX_ATTEMPTS_PER_USER) {
                attempts++;

                Optional<Contents> picked = contentsRepository.pickRandom(); // ORDER BY RAND()/RANDOM() LIMIT 1
                if (picked.isEmpty())
                    break;

                Contents contents = picked.get();
                String contentsId = contents.getContentsId();

                if (!triedContentIds.add(contentsId))
                    continue; // 같은 콘텐츠 중복 팔로우 방지
                log.info("contents Id : {} \n user 정보 {}", contentsId, user);

                try {
                    followedContentsService.follow(user, contentsId); // 시그니처에 맞게 호출
                    created++;
                } catch (Exception e) {
                    log.info("되라 좀;");
                }
            }
        }
    }

    private String getEmailDomain(Socials social) {
        switch (social) {
            case KAKAO:
                return "@kakao.com";
            case GOOGLE:
                return "@gmail.com";
            case NAVER:
                return "@naver.com";
            default:
                return "@example.com";
        }
    }

}
