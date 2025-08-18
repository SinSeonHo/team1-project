/**
 * 다중 슬라이더 지원 JS
 *
 * movieInfo.html용 슬라이더:
 *  - 컨테이너: .slider-container
 *  - 슬라이더: .slider
 *
 * index.html용 슬라이더:
 *  - 영화 슬라이더: .sliderContainerMovie > .sliderMovie
 *  - 게임 슬라이더: .sliderContainerGame > .sliderGame
 */
[
  // movieInfo.html 슬라이더
  { containerClass: "slider-container", sliderClass: "slider" },

  // index.html 영화 슬라이더
  { containerClass: "sliderContainerMovie", sliderClass: "sliderMovie" },

  // index.html 게임 슬라이더
  { containerClass: "sliderContainerGame", sliderClass: "sliderGame" },
].forEach(({ containerClass, sliderClass }) => {
  const containers = document.querySelectorAll(`.${containerClass}`);
  containers.forEach((container) => {
    const slider = container.querySelector(`.${sliderClass}`);
    if (!slider) {
      console.error(`슬라이더 요소를 찾을 수 없습니다: ${sliderClass}`);
      return;
    }

    // 슬라이더 내용을 두 배로 복제해 무한 스크롤 효과
    slider.innerHTML += slider.innerHTML;

    let scrollAmount = 0;
    let intervalId;

    function autoScroll() {
      scrollAmount++;
      if (scrollAmount >= slider.scrollWidth / 2) {
        scrollAmount = 0;
      }
      container.scrollLeft = scrollAmount;
    }

    function startScroll() {
      if (!intervalId) {
        intervalId = setInterval(autoScroll, 10);
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
});
