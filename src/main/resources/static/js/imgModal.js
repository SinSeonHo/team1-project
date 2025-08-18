document.addEventListener("DOMContentLoaded", function () {
  const container = document.getElementById("sliderContainer");
  const slider = document.getElementById("slider");

  // 모달 관련 요소
  const modal = document.getElementById("modal");
  const modalImg = document.getElementById("modalImg");
  const modalClose = document.getElementById("modalClose");
  const prevBtn = document.getElementById("prevBtn");
  const nextBtn = document.getElementById("nextBtn");

  if (!container || !slider) {
    console.error("slider 또는 sliderContainer 요소가 존재하지 않습니다.");
    return;
  }

  // 슬라이더 이미지들을 배열로 저장 (중복 복제된 이미지 제외하려면 실제 이미지 수만 저장)
  // 예를 들어 슬라이더 내부 이미지 원본 개수 = n (복제 전)
  const images = Array.from(slider.querySelectorAll("img")).slice(0, slider.children.length / 2);

  let currentIndex = 0;

  // 이미지 클릭 시 모달 열기
  slider.querySelectorAll("img").forEach((img, idx) => {
    img.addEventListener("click", () => {
      currentIndex = idx % images.length; // 복제 이미지 고려
      modal.style.display = "flex";
      modalImg.src = images[currentIndex].src;
    });
  });

  // 모달 닫기
  modalClose.addEventListener("click", () => {
    modal.style.display = "none";
  });

  // 이전 이미지 보기
  prevBtn.addEventListener("click", () => {
    currentIndex = (currentIndex - 1 + images.length) % images.length;
    modalImg.src = images[currentIndex].src;
  });

  // 다음 이미지 보기
  nextBtn.addEventListener("click", () => {
    currentIndex = (currentIndex + 1) % images.length;
    modalImg.src = images[currentIndex].src;
  });

  // 모달 외부 클릭 시 닫기 (선택사항)
  modal.addEventListener("click", (e) => {
    if (e.target === modal) {
      modal.style.display = "none";
    }
  });
});
