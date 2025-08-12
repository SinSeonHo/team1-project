// CSRF 토큰과 헤더 이름을 메타태그에서 가져옴
// const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
// const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

// AJAX 요청 공통 함수
function sendAsyncRequest(endpoint, successMsg) {
  fetch(endpoint, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      [csrfHeader]: csrfToken, // 동적으로 CSRF 헤더 설정
    },
  })
    .then((res) => {
      if (!res.ok) throw new Error("서버 오류");
      return res.text();
    })
    .then((msg) => alert(successMsg + "\n" + msg))
    .catch((err) => alert("요청 실패: " + err));
}
