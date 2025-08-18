// 모든 인풋에 대해 포커스/블러 이벤트 등록
document.querySelectorAll(".user-info-value").forEach(function (input) {
  input.addEventListener("focus", function () {
    this.closest(".user-info-item").classList.add("focused");
  });
  input.addEventListener("blur", function () {
    this.closest(".user-info-item").classList.remove("focused");
  });
});

// 이미지가 변경된 경우에만 새로고침
const urlParams = new URLSearchParams(window.location.search);
if (urlParams.get("img") === "updated" && !window.location.hash.includes("#reloaded")) {
  window.location.hash = "#reloaded";
  window.location.reload();
}
