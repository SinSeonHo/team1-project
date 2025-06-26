const btn = document.querySelector(".follow-btn");

btn.addEventListener("click", () => {
  const isFollow = btn.classList.toggle("follow");
  btn.textContent = isFollow ? "Follow" : "Followed";

  // mid 또는 gid 추출
  const mid = btn.dataset.mid;
  const gid = btn.dataset.gid;

  // 하나만 존재하므로 null 아님
  const contentId = mid || gid;

  // 여기서 리턴하거나 콘솔 출력
  console.log("선택된 ID:", contentId);

  // contentId를 다른 함수에 넘겨주거나 반환
  return contentId;
});

// 토글버튼 클릭 시 fetch경로 컨트롤러에 컨텐츠id값 넘겨줌
function toggleFavorite(button) {
  const contentsId = button.getAttribute("data-mid");

  fetch(`/favorite/toggle?contentsId=${contentsId}`)
    .then((response) => {
      if (response.ok) {
        alert("즐겨찾기 토글 완료!");
        // 필요시 버튼 스타일 변경 등 추가 처리 가능
      } else {
        alert("요청 실패");
      }
    })
    .catch((error) => {
      console.error("에러 발생:", error);
      alert("에러 발생");
    });
}
