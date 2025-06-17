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
const replyCon = document.querySelector(".replyList");

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
        result += `<div class="reply re-reply`;
      }
      result += `d-flex justify-content-between" data-id="${dto.rno}" >`;
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
reply.addEventListener("click", (e) => {
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
