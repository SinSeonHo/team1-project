// 날짜 포맷 함수
const formatDate = (str) => {
  const date = new Date(str);
  return (
    date.getFullYear() +
    "/" +
    (date.getMonth() + 1) +
    "/" +
    date.getDate() +
    " " +
    date.getHours() +
    ":" +
    date.getMinutes()
  );
};
// 댓글 수
const replyCnt = document.querySelector("#replyCnt");
// 댓글 컨테이너
const replyCon = document.querySelector(".anime__review__item");
// 댓글 폼
const replyForm = document.querySelector(".anime__details__form");

const replyList = () => {
  //리뷰 가져오기
  axios.get(`/replies/movie/${mid}`).then((res) => {
    const data = res.data;
    // 날짜 내림차순으로 정렬
    const sorted = data.sort((a, b) => new Date(b.createdDate) - new Date(a.createdDate));

    replyCnt.innerHTML = "댓글 " + data.length + "개";

    // 하나씩 출력
    let result = ``;
    sorted.forEach((dto) => {
      if (dto.ref == null) {
        result += `<div class="reply `;
      } else {
        result += `<div class="reply re-reply `;
      }
      result += `anime__review__item__text" data-id="${dto.rno}" >`;
      result += `  <h6>${dto.replyer} -  `;
      result += `<span>${formatDate(dto.createdDate)}</span></h6> 내용 : <p>`;

      if (dto.ref != null) {
        result += `<b>@${dto.mention}</b> `;
      }
      result += ` ${dto.text}</p></div>`;

      // 로그인 사용자 == 댓글작성자
      //   if (loginUser == replyer) {
      result += `<div class="mb-2"><button class="btn btn-outline-danger btn-sm">삭제</button></div>`;
      result += `<div><button class="btn btn-outline-success btn-sm">수정</button></div>`;
    });
    replyCon.innerHTML = result;
  });
};

replyList();

// 리뷰 삭제 및 수정
replyCon.addEventListener("click", (e) => {
  const btn = e.target;

  // rno 가져오기
  const rno = btn.closest(".reply").dataset.rno;
  console.log(rno + "수정");
  // 리뷰 작성자 가져오기
  const replyerId = btn.closest(".reply").dataset.replyer;

  // 삭제 or 수정
  if (btn.classList.contains("btn-outline-danger")) {
    // 삭제
    if (!confirm("정말로 삭제하시겠습니까?")) return;
    axios
      .delete(`/r/${rno}`, {
        headers: {
          //"Content-Type": "application/json",
          "X-CSRF-TOKEN": csrf,
        },
      })
      .then((res) => {
        console.log(res.data);
        //리뷰 다시 불러오기
        replyList();
      });
  } else if (btn.classList.contains("btn-outline-success")) {
    // 수정
    // 리뷰 하나 가져오기
    axios.get(`/re/${rno}`).then((res) => {
      console.log(res.data);
      const data = res.data;

      //replyForm 안에 보여주기
      replyForm.rno.value = data.rno;
      replyForm.replyer.value = data.replyer;
      // 멤버 아이디
      replyForm.mid.value = data.mid;
      // replyForm.querySelector(".starrr a:nth-child(" + data.grade + ")").click();
      replyForm.text.value = data.text;
    });
  }
});

// 리뷰 등록 및 수정
if (replyForm) {
  replyForm.addEventListener("submit", (e) => {
    e.preventDefault();

    const form = e.target;
    const rno = form.rno.value;

    form.grade.value = grade;

    if (rno) {
      //수정
      axios
        .put(`/reviews/${mid}/${rno}`, form, {
          headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrf,
          },
        })
        .then((res) => {
          console.log(res.data);
          alert("리뷰 수정 완료");

          // form 기존 내용 지우기
          replyForm.rno.value = "";
          replyForm.mid.value = "";
          replyForm.replyer.value = "";
          replyForm.text.value = "";
          replyForm.querySelector(".starrr a:nth-child(" + grade + ")").click();

          // 수정 내용 반영
          // reviewList();
        });
    } else {
      // 삽입
      // axios
      //   .post(`/replies/movie/${mid}`, form, {
      //     headers: {
      //       "Content-Type": "application/json",
      //       "X-CSRF-TOKEN": csrf,
      //     },
      //   })
      //   .then((res) => {
      //     alert(res.data + " 리뷰 등록");

      // form 기존 내용 지우기
      replyForm.rno.value = "";
      replyForm.text.value = "";

      // 삽입 내용 반영
      // replyList();
      // anime-review div 생성
      const animeReviewDiv = document.createElement("div");
      animeReviewDiv.className = "anime__review__item";
      animeReviewDiv.innerHTML = `<div class="anime__review__item__pic" th:classappend="${
        reply.ref != null
      } ? 're_reply'">
                <img src="img/anime/review-1.jpg" alt="" />
              </div>`;

      // review-item 클래스 중 마지막 요소 찾기
      const reviewItems = document.querySelectorAll(".anime__review__item");
      const lastReviewItem = reviewItems[reviewItems.length - 1];

      // 마지막 review-item 뒤에 삽입
      if (lastReviewItem && lastReviewItem.parentNode) {
        lastReviewItem.parentNode.insertBefore(animeReviewDiv, lastReviewItem.nextSibling);
      }
      // });
    }
  });
}
