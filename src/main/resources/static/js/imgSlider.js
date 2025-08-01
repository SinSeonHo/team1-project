document.addEventListener("DOMContentLoaded", function () {
  const container = document.getElementById("sliderContainer");
  const slider = document.getElementById("slider");

  if (!container || !slider) {
    console.error("slider 또는 sliderContainer 요소가 존재하지 않습니다.");
    return;
  }

  // 슬라이더 복제해서 이어 붙이기
  slider.innerHTML += slider.innerHTML;

  let scrollAmount = 0;
  let intervalId;

  function autoScroll() {
    scrollAmount += 1;
    if (scrollAmount >= slider.scrollWidth / 2) {
      // 절반만큼 스크롤되면 다시 처음으로
      scrollAmount = 0;
    }
    container.scrollLeft = scrollAmount;
  }

  function startScroll() {
    if (!intervalId) {
      intervalId = setInterval(autoScroll, 10); // 숫자가 작을수록 빠름
    }
  }

  function stopScroll() {
    clearInterval(intervalId);
    intervalId = null;
  }

  startScroll();

  container.addEventListener("mouseover", stopScroll);
  container.addEventListener("mouseleave", startScroll);
});
