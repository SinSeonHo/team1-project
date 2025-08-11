// 페이지가 완전히 로드 된 이후에 이벤트 생성
document.addEventListener("DOMContentLoaded", function () {
  // 링크를 설정합니다
  const loginLinks = {
    google: "/oauth2/authorization/google", // 링크1
    kakao: "/oauth2/authorization/kakao", // 링크2
    naver: "/oauth2/authorization/naver", // 링크3
  };

  // 각 링크에 클릭 이벤트 추가
  document.querySelectorAll(".login__social__links a").forEach((link) => {
    link.addEventListener("click", function (e) {
      e.preventDefault(); // 기본 a 태그 동작 막기

      if (this.classList.contains("google")) {
        window.location.href = loginLinks.google;
      } else if (this.classList.contains("kakao")) {
        window.location.href = loginLinks.kakao;
      } else if (this.classList.contains("naver")) {
        window.location.href = loginLinks.naver;
      }
    });
  });
});

document.querySelector(".registerBtn").addEventListener("click", function () {
  window.location.href = "/user/userConsent"; // 원하는 회원가입 링크로 수정
});
