// 모든 인풋에 대해 포커스/블러 이벤트 등록
document.querySelectorAll(".user-info-value").forEach(function (input) {
  input.addEventListener("focus", function () {
    this.closest(".user-info-item").classList.add("focused");
  });
  input.addEventListener("blur", function () {
    this.closest(".user-info-item").classList.remove("focused");
  });
});
