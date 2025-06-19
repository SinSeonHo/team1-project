// 댓글 수
const replyCnt = document.querySelector("#replyCnt");
// 댓글 컨테이너
const replyCon = document.querySelectorAll(".anime__review__item");
// 댓글 폼

// document.addEventListener("DOMContentLoaded", function () {
// 댓글 작성
const replyForm = document.getElementById("replyForm");

replyForm.addEventListener("submit", function (e) {
  e.preventDefault();

  const formData = new FormData(replyForm);
  const data = {
    mid: formData.get("mid"),
    replyer: formData.get("replyer"),
    text: formData.get("text"),
    ref: formData.get("ref"),
    mention: formData.get("mention"),
  };

  axios
    .post(`/replies/movie/new`, data)
    .then((res) => {
      alert("댓글이 등록되었습니다.");
      location.reload(); // 또는 동적으로 추가
    })
    .catch((err) => {
      console.error(err);
      alert("댓글 등록 실패");
    });
});

// 댓글 삭제
document.querySelectorAll(".delete-btn").forEach((btn) => {
  btn.addEventListener("click", function () {
    const rno = this.getAttribute("data-id");

    if (!confirm("정말 삭제하시겠습니까?")) return;

    axios
      .delete(`/replies/movie/${rno}`)
      .then((res) => {
        location.reload(); // 또는 DOM에서 제거
      })
      .catch((err) => {
        console.error(err);
        alert("댓글 삭제 실패");
      });
  });
});
// });

const replyList = () => {
  //리뷰 가져오기
  // axios.get(`/replies/movie/${mid}`).then((res) => {
  //   const data = res.data;
  //   // 날짜 내림차순으로 정렬
  //   const sorted = data.sort((a, b) => new Date(b.createdDate) - new Date(a.createdDate));
  //   replyCnt.innerHTML = "댓글 " + data.length + "개";
  //   // 하나씩 출력
  //   let result = ``;
  //   // sorted.forEach((dto) => {
  //   //   if (dto.ref == null) {
  //   //     result += `<div class="reply `;
  //   //   } else {
  //   //     result += `<div class="reply re-reply `;
  //   //   }
  //   //   result += `anime__review__item__text" data-id="${dto.rno}" >`;
  //   //   result += `  <h6>${dto.replyer} -  `;
  //   //   result += `<span>${formatDate(dto.createdDate)}</span></h6> 내용 : <p>`;
  //   //   if (dto.ref != null) {
  //   //     result += `<b>@${dto.mention}</b> `;
  //   //   }
  //   //   result += ` ${dto.text}</p></div>`;
  //   //   // 로그인 사용자 == 댓글작성자
  //   //   //   if (loginUser == replyer) {
  //   //   result += `<div class="mb-2"><button class="btn btn-outline-danger btn-sm">삭제</button></div>`;
  //   //   result += `<div><button class="btn btn-outline-success btn-sm">수정</button></div>`;
  //   // });
  //   // replyCon.innerHTML = result;
  // });
};

// replyList();
// 리뷰 멘션 추가
replyCon.forEach((re) => {
  re.addEventListener("click", (e) => {
    const id = e.target;
    const rno = id.closest(".anime__review__item__text").dataset.id;
    const replyer = id.closest(".anime__review__item__text").dataset.replyer;

    replyForm.mention.value = replyer;
    replyForm.ref.value = rno;
    console.log(replyForm.ref.value);
    const mention = document.querySelector(".mention");
    mention.innerHTML = "멘션: " + replyer;
    mention.addEventListener("click", (e) => {
      replyForm.mention.value = null;
      replyForm.ref.value = null;
      mention.innerHTML = "";
    });
  });
});

// 리뷰 삭제 및 수정
replyCon.addEventListener("click", (e) => {
  e.preventDefault();
  const btn = e.target;

  // rno 가져오기
  const rno = btn.closest(".reply").dataset.rno;
  console.log(rno + "수정");
  // 리뷰 작성자 가져오기
  const replyerId = btn.closest(".reply").dataset.replyer;

  // 삭제 or 수정
  if (btn.classList.contains("btn-outline-danger")) {
    // 삭제
    if (!confirm("대댓글까지 삭제됩니다. 정말로 삭제하시겠습니까?")) return;
    axios
      .delete(`/replies/movie/${rno}`, {
        headers: {
          "Content-Type": "application/json",
          "X-CSRF-TOKEN": csrf,
        },
      })
      .then((res) => {
        console.log(res.data);
        //리뷰 다시 불러오기
        // replyList();
        location.reload();
      });
  } else if (btn.classList.contains("btn-insert")) {
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
