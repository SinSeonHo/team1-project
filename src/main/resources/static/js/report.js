// report.js (요점만)
document.addEventListener("DOMContentLoaded", () => {
  const reportModal = document.getElementById("reportModal");
  const reportTarget = document.getElementById("reportTarget");
  const reportReason = document.getElementById("reportReason");
  const btnCancelReport = document.querySelector(".btn-cancel-report");
  const btnSubmitReport = document.querySelector(".btn-submit-report");
  const btnClose = document.querySelector(".report-close");

  let currentRno = null; // ← replyId로 보낼 값
  let currentNickname = ""; // ← reporterId(피신고자 닉네임)
  const currentUserId = document.querySelector('meta[name="current-user-id"]')?.getAttribute("content");
  console.log("로그인 중인 유저", currentUserId);

  // 🚨 버튼 클릭 → 모달 열기
  document.querySelectorAll(".report-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      const rno = btn.getAttribute("data-rno");
      const nickname = btn.getAttribute("data-replyer-nickname");

      currentRno = rno;
      currentNickname = nickname;

      reportTarget.textContent = nickname || "(알 수 없음)";
      reportReason.value = "";

      reportModal.setAttribute("data-open", "true");
    });
  });

  // 닫기 & 취소
  [btnCancelReport, btnClose].forEach((el) =>
    el.addEventListener("click", () => reportModal.removeAttribute("data-open"))
  );

  // 신고 제출
  btnSubmitReport.addEventListener("click", async () => {
    const reason = reportReason.value;
    if (!reason) return alert("신고 사유를 선택하세요.");
    if (!currentRno) return alert("신고 대상 댓글 번호가 없습니다.");
    if (!currentNickname) return alert("신고 대상 닉네임이 없습니다.");

    try {
      await axios.post(
        "/report", // ← 컨트롤러 매핑에 맞춤
        {
          reporterId: currentUserId, // 닉네임(피신고자)
          replyId: Number(currentRno), // ← 필수!
          reason, // INAPPROPRIATE_NICKNAME | SPOILER | PROFANITY | ADVERTISEMENT
        },
        {
          headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrf, // 전역 csrf 사용
          },
        }
      );
      alert("신고가 접수되었습니다.");
      reportModal.removeAttribute("data-open");
      location.reload();
    } catch (err) {
      console.error(err);
      alert("신고 요청 실패");
    }
  });
});
