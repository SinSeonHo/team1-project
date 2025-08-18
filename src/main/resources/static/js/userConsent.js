const checkbox = document.querySelector(".checkbox");
const submitLink = document.querySelector("#submitLink");

// 체크박스 상태에 따라 버튼 활성화/비활성화
checkbox.addEventListener("change", () => {
  if (checkbox.checked) {
    submitLink.style.pointerEvents = "auto"; // 클릭 가능
    submitLink.style.opacity = "1"; // 시각적으로 활성화
  } else {
    submitLink.style.pointerEvents = "none"; // 클릭 불가
    submitLink.style.opacity = "0.6"; // 흐리게
  }
});

// 클릭 시 경고창 추가 (선택)
submitLink.addEventListener("click", (e) => {
  if (!checkbox.checked) {
    e.preventDefault(); // 링크 막기
    alert("약관에 동의해야 합니다.");
  }
});
