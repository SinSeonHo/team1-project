const replyCnt = document.querySelector("#replyCnt");
const replyCon = document.querySelector(".reply");
const reply = document.querySelector(".");

no = 1;

const replyList = () => {
  //리뷰 가져오기
  axios.get(`/replies/movie/m_1`).then((res) => {
    console.log(res.data);
    const data = res.data;

    replyCnt.innerHTML = "댓글 " + data.length + "개";

    let result = ``;
    data.forEach((dto) => {
      result += `<div class="d-flex justify-content-between" data-id="${dto.rno}" >`;
      result += `   ${no} 이름 : ${dto.replyer}`;
      result += `   내용 ${dto.text}</div>`;
      result += `\t${dto.createdDate} </div>`;
      // 로그인 사용자 == 댓글작성자
      //   if (loginUser == replyer) {
      //   result += `<div class="mb-2"><button class="btn btn-outline-danger btn-sm">삭제</button></div>`;
      //   result += `<div><button class="btn btn-outline-success btn-sm">수정</button></div>`;
      no += 1;
    });
    replyCon.innerHTML = result;
  });
};

replyList();

// 리뷰 삭제 및 수정
reply.addEventListener("click", (e) => {
  const btn = e.target;

  // rno 가져오기
  const rno = btn.closest(".review-row").dataset.rno;
  console.log(rno);
  // 리뷰 작성자 가져오기
  const email = btn.closest(".review-row").dataset.email;
  // 삭제 or 수정
  if (btn.classList.contains("btn-outline-danger")) {
    // 삭제
    if (!confirm("정말로 삭제하시겠습니까?")) return;

    axios
      .delete(`/reviews/${mno}/${rno}`, {
        //data: { email: email },
        headers: {
          //"Content-Type": "application/json",
          "X-CSRF-TOKEN": csrf,
        },
      })
      .then((res) => {
        console.log(res.data);
        //리뷰 다시 불러오기
        reviewList();
      });
  } else if (btn.classList.contains("btn-outline-success")) {
    // 수정
    // 리뷰 하나 가져오기
    axios.get(`/reviews/${mno}/${rno}`).then((res) => {
      console.log(res.data);
      const data = res.data;

      //reviewForm 안에 보여주기
      reviewForm.rno.value = data.rno;
      reviewForm.nickname.value = data.nickname;
      // 멤버 아이디
      reviewForm.mid.value = data.mid;
      reviewForm.querySelector(".starrr a:nth-child(" + data.grade + ")").click();
      reviewForm.text.value = data.text;
    });
  }
});
