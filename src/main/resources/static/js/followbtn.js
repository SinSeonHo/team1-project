// 토글버튼 클릭 시 fetch경로 컨트롤러에 컨텐츠id값 넘겨줌
document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".follow-btn").forEach((button) => {
    button.addEventListener("click", async () => {
      const mid = button.dataset.mid;
      const gid = button.dataset.gid;
      const contentsId = mid ? `${mid}` : `${gid}`;

      try {
        const res = await fetch(`/favorite/toggle?contentsId=${contentsId}`);
        if (res.ok) {
          // 응답은 무시하고, 버튼 상태만 프론트에서 토글
          button.classList.toggle("follow");

          const isNowFollowed = button.classList.contains("follow");
          button.textContent = isNowFollowed ? "Followed" : "Follow";
        } else {
          alert("서버 오류 발생!");
        }
      } catch (error) {
        alert("요청 실패: " + error);
      }
    });
  });
});
