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
