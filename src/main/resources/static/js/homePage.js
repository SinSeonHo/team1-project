/* Slider init */
(function () {
  document.querySelectorAll(".js-slider").forEach(setupSlider);

  function setupSlider(slider) {
    const track = slider.querySelector(".slider-track");
    const prev = slider.querySelector(".prev");
    const next = slider.querySelector(".next");

    const step = () => Math.round(track.clientWidth * 0.9);
    const canScroll = () => track.scrollWidth - track.clientWidth > 2;
    const atStart = () => Math.floor(track.scrollLeft) <= 0;
    const atEnd = () => Math.ceil(track.scrollLeft + track.clientWidth) >= track.scrollWidth - 1;

    const update = () => {
      if (!canScroll()) {
        prev.disabled = true;
        next.disabled = true;
        return;
      }
      prev.disabled = atStart();
      next.disabled = atEnd();
    };

    prev.addEventListener("click", () => track.scrollBy({ left: -step(), behavior: "smooth" }));
    next.addEventListener("click", () => track.scrollBy({ left: step(), behavior: "smooth" }));
    track.addEventListener("scroll", update, { passive: true });
    window.addEventListener("resize", update);

    /* 이미지 로드 후 재계산 */
    const imgs = track.querySelectorAll("img");
    let pending = imgs.length;
    if (pending === 0) update();
    imgs.forEach((img) => {
      if (img.complete) {
        if (--pending === 0) update();
      } else {
        img.addEventListener("load", () => {
          if (--pending === 0) update();
        });
      }
    });

    update();
  }
})();

/* Top10 탭 토글 */
(function () {
  const tabs = document.querySelectorAll(".top10-tabs .tab-btn");
  const panels = document.querySelectorAll(".tab-panel");

  tabs.forEach((btn) => {
    btn.addEventListener("click", () => {
      tabs.forEach((b) => b.classList.remove("active"));
      btn.classList.add("active");
      tabs.forEach((b) => b.setAttribute("aria-selected", b === btn ? "true" : "false"));

      const target = document.querySelector(btn.dataset.target);
      panels.forEach((p) => p.classList.remove("active"));
      if (target) target.classList.add("active");

      window.dispatchEvent(new Event("resize")); // 버튼 표시/숨김 재계산
    });
  });
})();

/* Hero CTA 스무스 스크롤(고정 헤더 보정) */
document.querySelectorAll('a[href="#top10"]').forEach((a) => {
  a.addEventListener("click", (e) => {
    const target = document.querySelector("#top10");
    if (!target) return;
    e.preventDefault();
    const offset = 80;
    const y = target.getBoundingClientRect().top + window.pageYOffset - offset;
    window.scrollTo({ top: y, behavior: "smooth" });
  });
});
