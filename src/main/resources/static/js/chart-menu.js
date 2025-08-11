document.addEventListener("DOMContentLoaded", function () {
  // 모든 메뉴 항목에 클릭 이벤트 바인딩
  document.querySelectorAll(".report-link").forEach((link) => {
    link.addEventListener("click", function (e) {
      e.preventDefault(); // 기본 링크 이동 방지
      const rCode = this.dataset.rcode; // data-rcode 속성 가져오기
      changeChartReport(rCode);
    });
  });
});

function changeChartReport(rCode) {
  const iframe = document.getElementById("reportFrame");
  if (iframe) {
    iframe.src = `http://localhost:8080/report?rCode=${rCode}`;
  } else {
    console.warn('iframe with id "reportFrame" not found.');
  }
}
