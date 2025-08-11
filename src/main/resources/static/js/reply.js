// 댓글 수
const replyCnt = document.querySelector("#replyCnt");
// 댓글 컨테이너
const replyCon = document.querySelectorAll(".anime__review__item");

// 댓글 작성, 수정 폼
const replyForm = document.getElementById("replyForm");
// 댓글 작성 또는 수정
replyForm.addEventListener("submit", (e) => {
  e.preventDefault();

  const data = e.target;
  // undefined 처리
  if (data.ref.value === "undefined" || data.ref.value === "") {
    data.ref.value = null;
  }
  if (data.mention.value === "undefined" || data.mention.value === "") {
    data.mention.value = null;
  }

  if (data.rno.value) {
    // 댓글 수정
    axios
      .put(`/replies/update`, data, {
        headers: {
          "Content-Type": "application/json",
          "X-CSRF-TOKEN": csrf,
        },
      })
      .then((res) => {
        alert("댓글이 수정되었습니다.");
        location.reload();
      })
      .catch(() => {
        alert("댓글 수정 실패");
      });
  } else {
    // 댓글 추가
    if (data.replyer.value === "anonymousUser") {
      alert("로그인 페이지로 이동합니다");
      location.href = "/user/login";
    } else {
      axios
        .post(`/replies/new`, data, {
          headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrf,
          },
        })
        .then((res) => {
          if (res.status == 201) {
            location.reload(); // 또는 동적으로 추가
          } else {
            alert(res.data.message);
          }
        })
        .catch((err) => {
          if (err.status == 403) {
            alert("우선 이메일 인증을 해주세요");
          } else {
            alert("댓글 등록 오류");
          }
        });
    }
  }
});

// 수정 버튼으로 정보 가져오기
document.querySelectorAll(".update-btn").forEach((e) => {
  e.addEventListener("click", (btn) => {
    // 댓글 요소 가져오기
    const reply = e.closest(".reply");
    // 데이터 가져오기
    const data = reply.dataset;

    //replyForm 안에 보여주기
    replyForm.rno.value = data.rno;
    replyForm.text.value = data.text;
    replyForm.replyer.value = data.replyer;
    // 리뷰 정보
    replyForm.mention.value = data.mention;
    replyForm.ref.value = data.ref;

    const reviewer = data.replyernickname;
    const mention = document.querySelector(".mention");
    mention.innerHTML = "멘션: " + reviewer + "<button type='button' class='btn btn-secondary btn-sm'>✖</button>";
    // 별점
    replyForm.rate.value = data.rate;
    highlightStars(data.rate);
  });
});

// 댓글 삭제
document.querySelectorAll(".delete-btn").forEach((btn) => {
  btn.addEventListener("click", () => {
    const rno = btn.getAttribute("data-rno");
    if (!confirm("정말 삭제하시겠습니까? 댓글도 삭제됩니다.")) return;

    axios
      .delete(`/replies/${rno}`, {
        headers: {
          "X-CSRF-TOKEN": csrf,
        },
      })
      .then((res) => {
        location.reload(); // 또는 DOM에서 제거
      })
      .catch((err) => {
        alert("댓글 삭제 실패");
      });
  });
});

const replyText = document.getElementById("replytext");
const currentCharCount = document.getElementById("currentCharCount");
const maxLength = 200; // 최대 글자 수
document.querySelector("#maxChar").textContent = maxLength;

// 입력 필드에 'input' 이벤트 리스너 추가
replyText.addEventListener("input", () => {
  let currentLength = replyText.value.length;
  console.log(replyText.value);

  // 글자 수가 제한을 초과하는 경우
  if (currentLength > maxLength) {
    replyText.value = replyText.value.substring(0, maxLength); // 초과된 부분 잘라내기
    currentLength = maxLength; // 현재 글자 수를 최대 길이로 설정
    currentCharCount.classList.add("exceeded"); // 초과 시 css 변경
  } else {
    currentCharCount.classList.remove("exceeded"); // 초과하지 않으면 색상 원복
  }

  // 현재 글자 수 업데이트
  currentCharCount.textContent = currentLength;
});

// 페이지 로드 시 초기 글자 수 표시 (만약 미리 텍스트가 있다면)
currentCharCount.textContent = replyText.value.length;

// 리뷰에 댓글 추가
document.querySelectorAll(".mention-btn").forEach((re) => {
  re.addEventListener("click", (e) => {
    // 데이터 가져와서 폼에 추가
    const data = e.target.closest(".anime__review__item__text").dataset;
    const rno = data.rno;
    const reviewer = data.replyernickname;

    replyForm.mention.value = reviewer;
    replyForm.ref.value = rno;
    // textarea 멘션 추가
    replyForm.text.value = replyForm.text.value.replace(/@\S+\s*/g, "").trim();
    replyForm.text.value = `@${reviewer} ` + (replyForm.text.value == undefined ? " " : replyForm.text.value);

    const mention = document.querySelector(".mention");
    mention.innerHTML = "멘션: " + reviewer + "<button type='button' class='btn btn-secondary btn-sm'>✖</button>";
    // 멘션 제거버튼 기능 추가
    mention.querySelector(".btn").addEventListener("click", (e) => {
      removeMention();
    });

    starsContainer.classList.add("hide");
  });
});

// 취소 버튼
replyForm.addEventListener("click", (e) => {
  const btn = e.target;
  if (btn.classList.contains("btn-cancel")) {
    // 멘션 제거
    removeMention();
    replyForm.rno.value = null;
    replyForm.text.value = null;
    starsContainer.dataset.rating = 0;
    currentCharCount.textContent = 0;

    // 별점 초기화
    document.querySelector(".rating-form").classList.remove("hide"); // 별점 보이기
    highlightStars(0);
  }
});

function removeMention() {
  const mention = document.querySelector(".mention");
  mention.innerHTML = "";
  replyForm.mention.value = null;
  replyForm.ref.value = null;
  replyForm.text.value = replyForm.text.value.replace(/@\S+\s*/g, "").trim();
  // 별점 초기화
  starsContainer.classList.remove("hide"); // 별점 보이기
  starsContainer.dataset.rating = 0; // data-rating 속성 업데이트
  replyForm.rate.value = 0;
  highlightStars(0);
}

// 별점 기능
const starsContainer = document.querySelector(".rating-form");
const allStars = document.querySelectorAll(".rating-form .fa-star");
const rateStars = document.querySelectorAll(".rating-rate .fa-star");
let currentRating = parseInt(starsContainer.dataset.rating); // 현재 선택된 별점 (초기값)

// 초기 별점 설정 (페이지 로드 시)
highlightStars(currentRating);

// 클릭 이벤트 (별점 선택)
starsContainer.addEventListener("click", (event) => {
  if (event.target.classList.contains("fa-star")) {
    currentRating = parseInt(event.target.dataset.value); // 현재 선택된 별점 업데이트
    starsContainer.dataset.rating = currentRating; // data-rating 속성 업데이트
    highlightStars(currentRating); // 별점 UI 업데이트
    replyForm.rate.value = currentRating;
  }
});

// 마우스 오버 이벤트 (미리 보기)
starsContainer.addEventListener("mouseover", (event) => {
  if (event.target.classList.contains("fa-star")) {
    highlightStars(parseInt(event.target.dataset.value)); // 선택된 별의 점수
  }
});

// 마우스 아웃 이벤트 (원래 별점으로 되돌리기)
starsContainer.addEventListener("mouseout", () => {
  highlightStars(currentRating); // 원래 선택된 별점으로 돌아가기
});

// 별들을 하이라이트하는 함수 (마우스 오버 및 클릭 시 사용)
function highlightStars(value) {
  allStars.forEach((star) => {
    const starValue = parseInt(star.dataset.value);
    if (starValue <= value) {
      star.classList.add("fa");
    } else {
      star.classList.remove("fa");
    }
  });
}
