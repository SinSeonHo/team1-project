// 댓글 수
const replyCnt = document.querySelector("#replyCnt");
// 댓글 컨테이너
const replyCon = document.querySelectorAll(".anime__review__item");
// 댓글 폼

// document.addEventListener("DOMContentLoaded", function () {
// 댓글 작성, 수정 폼
const replyForm = document.getElementById("replyForm");
// 댓글 작성 또는 수정
replyForm.addEventListener("submit", (e) => {
  e.preventDefault();

  // const formData = new FormData(replyForm);
  // const data = {
  //   rno: formData.get("rno"),
  //   replyer: formData.get("replyer"),
  //   text: formData.get("text"),
  //   mid: formData.get("mid"),
  //   ref: formData.get("ref"),
  //   mention: formData.get("mention"),
  // };
  const data = e.target;
  console.log(data);
  // undefined 처리
  if (data.ref.value === "undefined" || data.ref.value === "") {
    data.ref.value = null;
  }

  if (data.rno.value) {
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
    if (data.replyer.value === "anonymousUser") {
      alert("로그인해 주세요");
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
          // alert("댓글이 등록되었습니다.");
          location.reload(); // 또는 동적으로 추가
        })
        .catch((err) => {
          console.error(err);
          alert("댓글 등록 실패");
        });
    }
  }
});

// 수정 버튼으로 정보 가져오기
document.querySelectorAll(".update-btn").forEach((e) => {
  e.addEventListener("click", (btn) => {
    // replyCon.forEach((e) => {
    //   e.addEventListener("click", (btn) => {
    // console.log(btn.target);

    // 댓글 요소 가져오기
    const reply = e.closest(".reply");
    // 데이터 가져오기
    const data = reply.dataset;

    // 수정 버튼 눌렀는지
    // if (e.classList.contains(".update-btn")) {
    // 수정
    //replyForm 안에 보여주기
    replyForm.rno.value = data.rno;
    replyForm.text.value = data.text;
    replyForm.replyer.value = data.replyer;
    // 컨텐츠 아이디
    replyForm.mid.value = data.id;
    replyForm.mention.value = data.mention;
    replyForm.ref.value = data.ref;
  });
});

// 댓글 삭제
document.querySelectorAll(".delete-btn").forEach((btn) => {
  btn.addEventListener("click", function () {
    const rno = this.getAttribute("data-rno");
    if (!confirm("정말 삭제하시겠습니까? 대댓글까지 삭제됩니다.")) return;

    // if (ref == null) {
    //   if (!confirm("정말 삭제하시겠습니까?")) return;
    // } else {
    //   if (!confirm("정말 삭제하시겠습니까? 대댓글까지 삭제됩니다.")) return;
    // }
    axios
      .delete(`/replies/${rno}`)
      .then((res) => {
        location.reload(); // 또는 DOM에서 제거
      })
      .catch((err) => {
        // console.error(err);
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

// 리뷰 멘션 추가
document.querySelectorAll(".mention-btn").forEach((re) => {
  re.addEventListener("click", (e) => {
    const data = e.target.closest(".anime__review__item__text").dataset;
    const rno = data.rno;
    const replyer = data.replyernickname;

    replyForm.mention.value = replyer;
    replyForm.ref.value = rno;
    const mention = document.querySelector(".mention");
    mention.innerHTML = "멘션: " + replyer + "<button type='button' class='btn btn-secondary btn-sm'>X</button>";
    mention.querySelector(".btn").addEventListener("click", (e) => {
      replyForm.mention.value = null;
      replyForm.ref.value = null;
      mention.innerHTML = "";
    });
    console.log(data);
  });
});

// 취소 버튼
replyForm.addEventListener("click", (e) => {
  const btn = e.target;
  if (btn.classList.contains("btn-cancel")) {
    replyForm.rno.value = null;
    replyForm.mention.value = null;
    replyForm.ref.value = null;
    replyForm.text.value = null;
    document.querySelector(".mention").innerHTML = "";
  }
});
