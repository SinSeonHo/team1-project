const replyList = () => {
  //리뷰 가져오기
  axios.get(`/replies/movie/${mid}`).then((res) => {
    console.log(res.data);
    const data = res.data;

    reviewCnt.innerHTML = data.length;

    let result = "";
    data.forEach((dto) => {
      result += `    <input type="text" th:value=${dto.text} readonly />`;
      // 로그인 사용자 == 댓글작성자
      //   if (loginUser == replyer) {
      result += `<div class="mb-2">${dto.replyer}<button class="btn btn-outline-danger btn-sm">삭제</button></div>`;
      result += `<div><button class="btn btn-outline-success btn-sm">수정</button></div>`;

      result += `</div></div>`;
    });
  });
};

replyList();
