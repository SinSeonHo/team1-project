// 버튼요소가져오기
const scrollBtn = document.getElementById("scrollToTopBtn");

// 스크롤 감지해서 버튼 보이기
window.onscroll = function () {
  if (document.documentElement.scrollTop > 300) {
    scrollBtn.style.display = "block";
  } else {
    scrollBtn.style.display = "none";
  }
};

// 버튼 클릭 시 최상단으로 부드럽게 스크롤
scrollBtn.onclick = function () {
  window.scrollTo({ top: 0, behavior: "smooth" });
};
